/**
 * @file: PhoneAuthViewModel.kt
 * @description: ViewModel для аутентификации через номер телефона
 * @dependencies: Hilt, ViewModel, StateFlow
 * @created: 2024-12-20
 */
package com.pizzanat.app.presentation.auth.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhoneAuthUiState(
    val phoneNumber: String = "+7",
    val phoneError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSmsSent: Boolean = false,
    val isAuthSuccessful: Boolean = false
)

@HiltViewModel
class PhoneAuthViewModel @Inject constructor(
    // TODO: Inject PhoneAuthUseCase when backend is ready
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhoneAuthUiState())
    val uiState: StateFlow<PhoneAuthUiState> = _uiState.asStateFlow()

    fun onPhoneNumberChanged(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(
            phoneNumber = phoneNumber,
            phoneError = null,
            error = null
        )
    }

    fun sendSmsCode() {
        val currentState = _uiState.value
        
        // Валидация номера телефона
        val phoneError = validatePhoneNumber(currentState.phoneNumber)
        if (phoneError != null) {
            _uiState.value = currentState.copy(phoneError = phoneError)
            return
        }

        // Отправка SMS кода
        performSendSmsCode(currentState.phoneNumber)
    }

    private fun performSendSmsCode(phoneNumber: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                isSmsSent = false
            )

            try {
                // TODO: Implement actual SMS sending when backend is ready
                // val result = sendSmsCodeUseCase(phoneNumber)
                
                // Симуляция отправки SMS (удалить когда будет готов backend)
                kotlinx.coroutines.delay(2000)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSmsSent = true,
                    error = null
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка отправки SMS"
                )
            }
        }
    }

    private fun validatePhoneNumber(phoneNumber: String): String? {
        // Извлекаем только цифры из номера
        val digits = phoneNumber.filter { it.isDigit() }
        
        return when {
            phoneNumber.isBlank() -> "Введите номер телефона"
            digits.length < 11 -> "Номер телефона должен содержать 11 цифр"
            digits.length > 11 -> "Номер телефона не должен превышать 11 цифр"
            !digits.startsWith("7") -> "Номер должен начинаться с +7"
            else -> null
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 