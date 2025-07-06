/**
 * @file: PhoneAuthViewModel.kt
 * @description: ViewModel для аутентификации через номер телефона
 * @dependencies: Hilt, ViewModel, StateFlow, SMS Use Cases
 * @created: 2024-12-20
 * @updated: 2025-01-23 - Добавлена реальная интеграция с API
 */
package com.pizzanat.app.presentation.auth.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.usecases.auth.SendSmsCodeUseCase
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
    private val sendSmsCodeUseCase: SendSmsCodeUseCase
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
                val result = sendSmsCodeUseCase(phoneNumber)
                
                if (result.isSuccess) {
                    val response = result.getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSmsSent = true,
                        error = null
                    )
                } else {
                    val exception = result.exceptionOrNull()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception?.message ?: "Ошибка отправки SMS"
                    )
                }
                
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