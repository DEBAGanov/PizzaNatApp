/**
 * @file: AdminLoginViewModel.kt
 * @description: ViewModel для экрана входа администратора
 * @dependencies: AdminLoginUseCase, StateFlow
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.admin.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.usecases.admin.AdminLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminLoginUiState(
    val username: String = "",
    val password: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false
)

@HiltViewModel
class AdminLoginViewModel @Inject constructor(
    private val adminLoginUseCase: AdminLoginUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminLoginUiState())
    val uiState: StateFlow<AdminLoginUiState> = _uiState.asStateFlow()
    
    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            usernameError = null,
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
        if (!validateInput()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                generalError = null
            )
            
            try {
                val result = adminLoginUseCase(
                    username = _uiState.value.username.trim(),
                    password = _uiState.value.password
                )
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generalError = result.exceptionOrNull()?.message ?: "Ошибка входа"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generalError = e.message ?: "Неизвестная ошибка"
                )
            }
        }
    }
    
    private fun validateInput(): Boolean {
        val currentState = _uiState.value
        var hasErrors = false
        
        val usernameError = when {
            currentState.username.isBlank() -> {
                hasErrors = true
                "Введите имя пользователя"
            }
            currentState.username.length < 3 -> {
                hasErrors = true
                "Имя пользователя должно содержать минимум 3 символа"
            }
            else -> null
        }
        
        val passwordError = when {
            currentState.password.isBlank() -> {
                hasErrors = true
                "Введите пароль"
            }
            currentState.password.length < 6 -> {
                hasErrors = true
                "Пароль должен содержать минимум 6 символов"
            }
            else -> null
        }
        
        _uiState.value = currentState.copy(
            usernameError = usernameError,
            passwordError = passwordError
        )
        
        return !hasErrors
    }
    
    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            usernameError = null,
            passwordError = null,
            generalError = null
        )
    }
} 