/**
 * @file: TelegramAuthViewModel.kt
 * @description: ViewModel для авторизации через Telegram с реальным API
 * @dependencies: Hilt, ViewModel, StateFlow, Telegram Use Cases
 * @created: 2024-12-20
 * @updated: 2025-01-23 - Добавлено детальное логирование для диагностики
 */
package com.pizzanat.app.presentation.auth.telegram

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.entities.TelegramAuthStatus
import com.pizzanat.app.domain.usecases.auth.CheckTelegramAuthStatusUseCase
import com.pizzanat.app.domain.usecases.auth.InitTelegramAuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TelegramAuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthSuccessful: Boolean = false,
    val telegramAuthUrl: String? = null,
    val authToken: String? = null,
    val isPolling: Boolean = false,
    val isWaitingForAuth: Boolean = false // Флаг ожидания авторизации в Telegram
)

@HiltViewModel
class TelegramAuthViewModel @Inject constructor(
    private val initTelegramAuthUseCase: InitTelegramAuthUseCase,
    private val checkTelegramAuthStatusUseCase: CheckTelegramAuthStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TelegramAuthUiState())
    val uiState: StateFlow<TelegramAuthUiState> = _uiState.asStateFlow()
    
    private var pollingJob: Job? = null

    companion object {
        private const val TAG = "TelegramAuthViewModel"
    }

    fun startTelegramAuth() {
        viewModelScope.launch {
            Log.d(TAG, "🚀 Запуск Telegram авторизации")
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                // Получение device ID (можно расширить для реального device ID)
                val deviceId = "android_${System.currentTimeMillis()}"
                Log.d(TAG, "📱 Device ID: $deviceId")
                
                val result = initTelegramAuthUseCase(deviceId)
                
                if (result.isSuccess) {
                    val authInitResponse = result.getOrThrow()
                    Log.d(TAG, "✅ Telegram авторизация инициализирована успешно:")
                    Log.d(TAG, "  - Auth Token: ${authInitResponse.authToken}")
                    Log.d(TAG, "  - Bot URL: ${authInitResponse.telegramBotUrl}")
                    Log.d(TAG, "  - Expires At: ${authInitResponse.expiresAt}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        telegramAuthUrl = authInitResponse.telegramBotUrl,
                        authToken = authInitResponse.authToken,
                        error = null
                    )
                } else {
                    val exception = result.exceptionOrNull()
                    Log.e(TAG, "❌ Ошибка инициализации Telegram авторизации: ${exception?.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception?.message ?: "Ошибка инициализации Telegram авторизации"
                    )
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "💥 Исключение при создании Telegram авторизации", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Неожиданная ошибка при создании Telegram авторизации"
                )
            }
        }
    }

    /**
     * Объединенная функция: инициализация + открытие Telegram одним кликом
     */
    fun startTelegramAuthAndOpen(onOpenTelegram: (String) -> Unit) {
        viewModelScope.launch {
            Log.d(TAG, "🚀 Запуск Telegram авторизации с автоматическим открытием")
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                // Получение device ID
                val deviceId = "android_${System.currentTimeMillis()}"
                Log.d(TAG, "📱 Device ID: $deviceId")
                
                val result = initTelegramAuthUseCase(deviceId)
                
                if (result.isSuccess) {
                    val authInitResponse = result.getOrThrow()
                    Log.d(TAG, "✅ Telegram авторизация инициализирована успешно:")
                    Log.d(TAG, "  - Auth Token: ${authInitResponse.authToken}")
                    Log.d(TAG, "  - Bot URL: ${authInitResponse.telegramBotUrl}")
                    Log.d(TAG, "  - Expires At: ${authInitResponse.expiresAt}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        telegramAuthUrl = authInitResponse.telegramBotUrl,
                        authToken = authInitResponse.authToken,
                        error = null,
                        isWaitingForAuth = true
                    )
                    
                    // Автоматически открываем Telegram
                    Log.d(TAG, "📱 Автоматическое открытие Telegram...")
                    onOpenTelegram(authInitResponse.telegramBotUrl)
                    startPollingAuthStatus()
                    
                } else {
                    val exception = result.exceptionOrNull()
                    Log.e(TAG, "❌ Ошибка инициализации Telegram авторизации: ${exception?.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception?.message ?: "Ошибка инициализации Telegram авторизации"
                    )
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "💥 Исключение при создании Telegram авторизации", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Неожиданная ошибка при создании Telegram авторизации"
                )
            }
        }
    }

    fun openTelegramAuth() {
        Log.d(TAG, "📱 Запуск Telegram app/web")
        // TODO: Реализовать открытие Telegram app/web с auth URL через Intent
        // В данный момент просто запускаем polling
        startPollingAuthStatus()
    }

    fun checkAuthStatus() {
        val currentAuthToken = _uiState.value.authToken
        Log.d(TAG, "🔍 Проверка статуса авторизации для токена: ${currentAuthToken?.take(20)}...")
        
        if (currentAuthToken == null) {
            Log.w(TAG, "⚠️ Auth token отсутствует")
            _uiState.value = _uiState.value.copy(
                error = "Auth token отсутствует. Попробуйте перезапустить авторизацию."
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val result = checkTelegramAuthStatusUseCase(currentAuthToken)
                
                if (result.isSuccess) {
                    val statusResponse = result.getOrThrow()
                    Log.d(TAG, "📊 Статус ответ получен:")
                    Log.d(TAG, "  - Success: ${statusResponse.success}")
                    Log.d(TAG, "  - Status: ${statusResponse.status}")
                    Log.d(TAG, "  - Message: ${statusResponse.message}")
                    Log.d(TAG, "  - Has AuthResponse: ${statusResponse.authResponse != null}")
                    
                    if (statusResponse.authResponse != null) {
                        Log.d(TAG, "👤 Данные пользователя в ответе:")
                        Log.d(TAG, "  - Token: ${statusResponse.authResponse.token.take(20)}...")
                        Log.d(TAG, "  - User ID: ${statusResponse.authResponse.user.id}")
                        Log.d(TAG, "  - Username: ${statusResponse.authResponse.user.username}")
                        Log.d(TAG, "  - Email: ${statusResponse.authResponse.user.email}")
                        Log.d(TAG, "  - Name: ${statusResponse.authResponse.user.firstName} ${statusResponse.authResponse.user.lastName}")
                    }
                    
                    when (statusResponse.status) {
                        TelegramAuthStatus.CONFIRMED -> {
                            Log.d(TAG, "🎉 Авторизация подтверждена! Останавливаем polling...")
                            stopPolling()
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isAuthSuccessful = true,
                                isWaitingForAuth = false,
                                error = null
                            )
                            Log.d(TAG, "✅ UI состояние обновлено: isAuthSuccessful = true")
                        }
                        TelegramAuthStatus.PENDING -> {
                            Log.d(TAG, "⏳ Авторизация в ожидании...")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = statusResponse.message ?: "Ожидание подтверждения в Telegram"
                            )
                        }
                        TelegramAuthStatus.EXPIRED -> {
                            Log.w(TAG, "⏰ Время авторизации истекло")
                            stopPolling()
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Время авторизации истекло. Попробуйте еще раз."
                            )
                        }
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    Log.e(TAG, "❌ Ошибка проверки статуса: ${exception?.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception?.message ?: "Ошибка проверки статуса авторизации"
                    )
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "💥 Исключение при проверке статуса", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Неожиданная ошибка при проверке статуса"
                )
            }
        }
    }

    private fun startPollingAuthStatus() {
        // Останавливаем предыдущий polling, если есть
        stopPolling()
        
        Log.d(TAG, "🔄 Запуск polling статуса авторизации (12 попыток по 5 секунд)")
        _uiState.value = _uiState.value.copy(isPolling = true)
        
        pollingJob = viewModelScope.launch {
            // Автоматическая проверка статуса авторизации каждые 5 секунд
            repeat(12) { attempt -> // 12 попыток = 1 минута
                Log.d(TAG, "🔍 Polling попытка ${attempt + 1}/12")
                delay(5000) // 5 секунд между проверками
                
                // Проверяем, не завершилась ли авторизация
                if (_uiState.value.isAuthSuccessful) {
                    Log.d(TAG, "🎉 Авторизация завершена успешно, выходим из polling")
                    return@repeat // Выходим из цикла при успешной авторизации
                }
                
                // Проверяем статус
                checkAuthStatus()
                
                // Если это последняя попытка и авторизация не прошла
                if (attempt == 11 && !_uiState.value.isAuthSuccessful) {
                    Log.w(TAG, "⏰ Время ожидания polling истекло")
                    _uiState.value = _uiState.value.copy(
                        isPolling = false,
                        error = "Время ожидания авторизации истекло (1 минута). Попробуйте еще раз."
                    )
                }
            }
            
            _uiState.value = _uiState.value.copy(isPolling = false)
            Log.d(TAG, "🔄 Polling завершен")
        }
    }
    
    private fun stopPolling() {
        Log.d(TAG, "🛑 Остановка polling")
        pollingJob?.cancel()
        pollingJob = null
        _uiState.value = _uiState.value.copy(isPolling = false)
    }

    fun clearError() {
        Log.d(TAG, "🗑️ Очистка ошибки")
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun setError(message: String) {
        Log.w(TAG, "⚠️ Установка ошибки: $message")
        _uiState.value = _uiState.value.copy(error = message)
    }

    fun resetAuth() {
        Log.d(TAG, "🔄 Сброс авторизации")
        stopPolling()
        _uiState.value = TelegramAuthUiState()
    }
    
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 ViewModel очищен, останавливаем polling")
        stopPolling()
    }
} 