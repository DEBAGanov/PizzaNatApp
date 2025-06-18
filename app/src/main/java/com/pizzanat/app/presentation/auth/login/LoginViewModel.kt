/**
 * @file: LoginViewModel.kt
 * @description: ViewModel для экрана входа с поддержкой email и username
 * @dependencies: Hilt, ViewModel, Use Cases, Coroutines
 * @created: 2024-12-19
 * @updated: 2024-12-20 - Добавлена поддержка авторизации по email и username
 */
package com.pizzanat.app.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.usecases.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val usernameOrEmail: String = "",
    val password: String = "",
    val usernameOrEmailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun onUsernameOrEmailChanged(usernameOrEmail: String) {
        _uiState.value = _uiState.value.copy(
            usernameOrEmail = usernameOrEmail,
            usernameOrEmailError = null,
            generalError = null
        )
    }
    
    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null,
            generalError = null
        )
    }
    
    fun onLoginClicked() {
        val currentState = _uiState.value
        
        // Валидация формы
        val usernameOrEmailError = validateUsernameOrEmail(currentState.usernameOrEmail)
        val passwordError = validatePassword(currentState.password)
        
        if (usernameOrEmailError != null || passwordError != null) {
            _uiState.value = currentState.copy(
                usernameOrEmailError = usernameOrEmailError,
                passwordError = passwordError
            )
            return
        }
        
        // Выполнение входа
        performLogin(currentState.usernameOrEmail, currentState.password)
    }
    
    private fun validateUsernameOrEmail(usernameOrEmail: String): String? {
        return when {
            usernameOrEmail.isBlank() -> "Email или имя пользователя не может быть пустым"
            usernameOrEmail.length < 3 -> "Минимум 3 символа"
            else -> null
        }
    }
    
    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Пароль не может быть пустым"
            password.length < 6 -> "Пароль должен содержать минимум 6 символов"
            else -> null
        }
    }
    
    private fun performLogin(usernameOrEmail: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                generalError = null
            )
            
            try {
                val result = loginUseCase(usernameOrEmail, password)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generalError = getLocalizedError(error)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generalError = getLocalizedError(e.message ?: "Произошла ошибка")
                )
            }
        }
    }
    
    private fun getLocalizedError(error: String): String {
        return when {
            error.contains("401") || error.contains("Неверное имя пользователя или пароль") -> 
                "Неверный email/логин или пароль.\nПопробуйте ввести логин вместо email или наоборот."
            error.contains("Network") || error.contains("timeout") -> 
                "Проблема с подключением к интернету"
            error.contains("Server") || error.contains("500") -> 
                "Проблема на сервере, попробуйте позже"
            else -> error
        }
    }
} 