/**
 * @file: SmsCodeViewModel.kt
 * @description: ViewModel для ввода и проверки SMS кода
 * @dependencies: Hilt, ViewModel, StateFlow, Timer
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
import kotlinx.coroutines.delay
import javax.inject.Inject

data class SmsCodeUiState(
    val phoneNumber: String = "",
    val smsCode: String = "",
    val codeError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthSuccessful: Boolean = false,
    val resendCountdown: Int = 0
)

@HiltViewModel
class SmsCodeViewModel @Inject constructor(
    // TODO: Inject VerifySmsCodeUseCase when backend is ready
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmsCodeUiState())
    val uiState: StateFlow<SmsCodeUiState> = _uiState.asStateFlow()

    companion object {
        private const val RESEND_COUNTDOWN_SECONDS = 60
    }

    init {
        startResendCountdown()
    }

    fun setPhoneNumber(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phoneNumber)
    }

    fun onSmsCodeChanged(smsCode: String) {
        _uiState.value = _uiState.value.copy(
            smsCode = smsCode,
            codeError = null,
            error = null
        )
        
        // Автоматическая проверка кода при вводе 4 цифр
        if (smsCode.length == 4) {
            verifySmsCode()
        }
    }

    fun verifySmsCode() {
        val currentState = _uiState.value
        
        // Валидация SMS кода
        val codeError = validateSmsCode(currentState.smsCode)
        if (codeError != null) {
            _uiState.value = currentState.copy(codeError = codeError)
            return
        }

        // Проверка SMS кода
        performVerifySmsCode(currentState.phoneNumber, currentState.smsCode)
    }

    private fun performVerifySmsCode(phoneNumber: String, smsCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                codeError = null
            )

            try {
                // TODO: Implement actual SMS code verification when backend is ready
                // val result = verifySmsCodeUseCase(phoneNumber, smsCode)
                
                // Симуляция проверки кода (удалить когда будет готов backend)
                kotlinx.coroutines.delay(1500)
                
                // Симуляция: код "1234" считается правильным
                if (smsCode == "1234") {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthSuccessful = true,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        codeError = "Неверный код. Попробуйте еще раз",
                        smsCode = "" // Очищаем поле при ошибке
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка проверки кода"
                )
            }
        }
    }

    fun resendSmsCode() {
        val currentState = _uiState.value
        
        if (currentState.resendCountdown > 0) {
            return // Еще рано для повторной отправки
        }

        performResendSmsCode(currentState.phoneNumber)
    }

    private fun performResendSmsCode(phoneNumber: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                // TODO: Implement actual SMS resending when backend is ready
                // val result = sendSmsCodeUseCase(phoneNumber)
                
                // Симуляция повторной отправки SMS
                kotlinx.coroutines.delay(1000)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    smsCode = "" // Очищаем поле при повторной отправке
                )
                
                // Запускаем обратный отсчет заново
                startResendCountdown()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка повторной отправки SMS"
                )
            }
        }
    }

    private fun startResendCountdown() {
        viewModelScope.launch {
            for (countdown in RESEND_COUNTDOWN_SECONDS downTo 0) {
                _uiState.value = _uiState.value.copy(resendCountdown = countdown)
                delay(1000)
            }
        }
    }

    private fun validateSmsCode(smsCode: String): String? {
        return when {
            smsCode.isBlank() -> "Введите код из SMS"
            smsCode.length != 4 -> "Код должен содержать 4 цифры"
            !smsCode.all { it.isDigit() } -> "Код должен содержать только цифры"
            else -> null
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, codeError = null)
    }
} 