/**
 * @file: SmsCodeViewModel.kt
 * @description: ViewModel для ввода и проверки SMS кода
 * @dependencies: Hilt, ViewModel, StateFlow, SMS Use Cases
 * @created: 2024-12-20
 * @updated: 2025-01-23 - Добавлена реальная интеграция с API и детальное логирование
 */
package com.pizzanat.app.presentation.auth.phone

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.usecases.auth.SendSmsCodeUseCase
import com.pizzanat.app.domain.usecases.auth.VerifySmsCodeUseCase
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
    private val verifySmsCodeUseCase: VerifySmsCodeUseCase,
    private val sendSmsCodeUseCase: SendSmsCodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmsCodeUiState())
    val uiState: StateFlow<SmsCodeUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "SmsCodeViewModel"
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

    /**
     * Автоматическое заполнение SMS кода (для SMS Retriever API)
     */
    fun onSmsCodeAutoFilled(smsCode: String) {
        Log.d(TAG, "📱 Автоматическое заполнение SMS кода: $smsCode")
        
        // Валидируем полученный код
        if (smsCode.matches("\\d{4}".toRegex())) {
            _uiState.value = _uiState.value.copy(
                smsCode = smsCode,
                codeError = null,
                error = null
            )
            
            // Автоматически проверяем код
            verifySmsCode()
        } else {
            Log.w(TAG, "⚠️ Некорректный автоматически заполненный код: $smsCode")
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
            Log.d(TAG, "🚀 Начинаем проверку SMS кода:")
            Log.d(TAG, "  📱 Номер телефона: $phoneNumber")
            Log.d(TAG, "  🔢 SMS код: $smsCode")
            
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                codeError = null
            )

            try {
                val result = verifySmsCodeUseCase(phoneNumber, smsCode)
                
                Log.d(TAG, "📋 Результат проверки:")
                Log.d(TAG, "  ✅ Success: ${result.isSuccess}")
                
                if (result.isSuccess) {
                    val response = result.getOrThrow()
                    
                    Log.d(TAG, "📊 Детали ответа:")
                    Log.d(TAG, "  🎯 success: ${response.success}")
                    Log.d(TAG, "  👤 authResponse: ${response.authResponse != null}")
                    Log.d(TAG, "  💬 message: ${response.message}")
                    Log.d(TAG, "  ❌ error: ${response.error}")
                    
                    if (response.authResponse != null) {
                        Log.d(TAG, "🔑 Данные авторизации:")
                        Log.d(TAG, "  🎫 Token: ${response.authResponse.token.take(20)}...")
                        Log.d(TAG, "  👤 User ID: ${response.authResponse.user.id}")
                        Log.d(TAG, "  📧 Username: ${response.authResponse.user.username}")
                    }
                    
                    if (response.success && response.authResponse != null) {
                        Log.d(TAG, "🎉 УСПЕШНАЯ АВТОРИЗАЦИЯ! Устанавливаем isAuthSuccessful = true")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAuthSuccessful = true,
                            error = null
                        )
                        Log.d(TAG, "✅ UI состояние обновлено, isAuthSuccessful = ${_uiState.value.isAuthSuccessful}")
                    } else {
                        Log.w(TAG, "⚠️ Ответ получен, но авторизация не успешна")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            codeError = response.message ?: response.error ?: "Неверный код. Попробуйте еще раз",
                            smsCode = "" // Очищаем поле при ошибке
                        )
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    Log.e(TAG, "❌ Ошибка при проверке кода: ${exception?.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        codeError = exception?.message ?: "Ошибка проверки кода",
                        smsCode = "" // Очищаем поле при ошибке
                    )
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "💥 Исключение при проверке кода", e)
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
                val result = sendSmsCodeUseCase(phoneNumber)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        smsCode = "" // Очищаем поле при повторной отправке
                    )
                    
                    // Запускаем обратный отсчет заново
                    startResendCountdown()
                } else {
                    val exception = result.exceptionOrNull()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception?.message ?: "Ошибка повторной отправки SMS"
                    )
                }
                
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