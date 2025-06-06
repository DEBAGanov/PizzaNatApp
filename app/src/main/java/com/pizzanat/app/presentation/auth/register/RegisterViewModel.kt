/**
 * @file: RegisterViewModel.kt
 * @description: ViewModel для экрана регистрации с управлением состоянием и валидацией
 * @dependencies: Hilt, ViewModel, Use Cases, Coroutines
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.usecases.auth.RegisterUseCase
import com.pizzanat.app.presentation.components.isValidPhoneNumber
import com.pizzanat.app.presentation.components.normalizePhoneForApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "+7",
    val password: String = "",
    val confirmPassword: String = "",
    val usernameError: String? = null,
    val emailError: String? = null,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val isRegisterSuccessful: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            usernameError = null,
            generalError = null
        )
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null,
            generalError = null
        )
    }

    fun onFirstNameChanged(firstName: String) {
        _uiState.value = _uiState.value.copy(
            firstName = firstName,
            firstNameError = null,
            generalError = null
        )
    }

    fun onLastNameChanged(lastName: String) {
        _uiState.value = _uiState.value.copy(
            lastName = lastName,
            lastNameError = null,
            generalError = null
        )
    }

    fun onPhoneChanged(phone: String) {
        _uiState.value = _uiState.value.copy(
            phone = phone,
            phoneError = null,
            generalError = null
        )
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null,
            confirmPasswordError = null,
            generalError = null
        )
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null,
            generalError = null
        )
    }

    fun onRegisterClicked() {
        val currentState = _uiState.value

        // Валидация всех полей
        val usernameError = validateUsername(currentState.username)
        val emailError = validateEmail(currentState.email)
        val firstNameError = validateFirstName(currentState.firstName)
        val lastNameError = validateLastName(currentState.lastName)
        val phoneError = validatePhone(currentState.phone)
        val passwordError = validatePassword(currentState.password)
        val confirmPasswordError = validateConfirmPassword(currentState.password, currentState.confirmPassword)

        // Если есть ошибки валидации
        if (usernameError != null || emailError != null || firstNameError != null ||
            lastNameError != null || phoneError != null || passwordError != null || confirmPasswordError != null) {
            _uiState.value = currentState.copy(
                usernameError = usernameError,
                emailError = emailError,
                firstNameError = firstNameError,
                lastNameError = lastNameError,
                phoneError = phoneError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
            return
        }

        // Выполнение регистрации
        performRegister(
            username = currentState.username,
            email = currentState.email,
            firstName = currentState.firstName,
            lastName = currentState.lastName,
            phone = currentState.phone,
            password = currentState.password
        )
    }

    private fun validateUsername(username: String): String? {
        return when {
            username.isBlank() -> "Имя пользователя не может быть пустым"
            username.length < 3 -> "Имя пользователя должно содержать минимум 3 символа"
            username.length > 20 -> "Имя пользователя не должно превышать 20 символов"
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> "Имя пользователя может содержать только буквы, цифры и underscore"
            else -> null
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email не может быть пустым"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Неверный формат email"
            else -> null
        }
    }

    private fun validateFirstName(firstName: String): String? {
        return when {
            firstName.isBlank() -> "Имя не может быть пустым"
            firstName.length < 2 -> "Имя должно содержать минимум 2 символа"
            firstName.length > 50 -> "Имя не должно превышать 50 символов"
            else -> null
        }
    }

    private fun validateLastName(lastName: String): String? {
        return when {
            lastName.isBlank() -> "Фамилия не может быть пустой"
            lastName.length < 2 -> "Фамилия должна содержать минимум 2 символа"
            lastName.length > 50 -> "Фамилия не должна превышать 50 символов"
            else -> null
        }
    }

    private fun validatePhone(phone: String): String? {
        return when {
            phone.isBlank() || phone == "+7" -> "Введите номер телефона"
            !isValidPhoneNumber(phone) -> "Введите корректный номер телефона"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Пароль не может быть пустым"
            password.length < 6 -> "Пароль должен содержать минимум 6 символов"
            password.length > 100 -> "Пароль не должен превышать 100 символов"
            else -> null
        }
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Подтверждение пароля не может быть пустым"
            password != confirmPassword -> "Пароли не совпадают"
            else -> null
        }
    }

    private fun performRegister(
        username: String,
        email: String,
        firstName: String,
        lastName: String,
        phone: String,
        password: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, generalError = null)

            // Нормализуем номер телефона для API
            val normalizedPhone = normalizePhoneForApi(phone)

            val result = registerUseCase(
                username = username,
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                phone = normalizedPhone
            )

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRegisterSuccessful = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generalError = result.exceptionOrNull()?.message ?: "Ошибка регистрации"
                )
            }
        }
    }
}