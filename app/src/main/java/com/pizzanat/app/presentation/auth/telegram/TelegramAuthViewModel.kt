/**
 * @file: TelegramAuthViewModel.kt
 * @description: ViewModel для авторизации через Telegram с реальным API
 * @dependencies: Hilt, ViewModel, StateFlow, Telegram Use Cases
 * @created: 2024-12-20
 * @updated: 2024-12-20 - Интеграция с реальным API
 */
package com.pizzanat.app.presentation.auth.telegram

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
    val isPolling: Boolean = false
)

@HiltViewModel
class TelegramAuthViewModel @Inject constructor(
    private val initTelegramAuthUseCase: InitTelegramAuthUseCase,
    private val checkTelegramAuthStatusUseCase: CheckTelegramAuthStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TelegramAuthUiState())
    val uiState: StateFlow<TelegramAuthUiState> = _uiState.asStateFlow()
    
    private var pollingJob: Job? = null

    fun startTelegramAuth() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                // Получение device ID (можно расширить для реального device ID)
                val deviceId = "android_${System.currentTimeMillis()}"
                
                val result = initTelegramAuthUseCase(deviceId)
                
                if (result.isSuccess) {
                    val authInitResponse = result.getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        telegramAuthUrl = authInitResponse.telegramBotUrl,
                        authToken = authInitResponse.authToken,
                        error = null
                    )
                } else {
                    val exception = result.exceptionOrNull()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception?.message ?: "Ошибка инициализации Telegram авторизации"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Неожиданная ошибка при создании Telegram авторизации"
                )
            }
        }
    }

    fun openTelegramAuth() {
        // TODO: Реализовать открытие Telegram app/web с auth URL через Intent
        // В данный момент просто запускаем polling
        startPollingAuthStatus()
    }

    fun checkAuthStatus() {
        val currentAuthToken = _uiState.value.authToken
        if (currentAuthToken == null) {
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
                    
                    when (statusResponse.status) {
                        TelegramAuthStatus.CONFIRMED -> {
                            stopPolling()
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isAuthSuccessful = true,
                                error = null
                            )
                        }
                        TelegramAuthStatus.PENDING -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = statusResponse.message ?: "Ожидание подтверждения в Telegram"
                            )
                        }
                        TelegramAuthStatus.EXPIRED -> {
                            stopPolling()
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Время авторизации истекло. Попробуйте еще раз."
                            )
                        }
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception?.message ?: "Ошибка проверки статуса авторизации"
                    )
                }
                
            } catch (e: Exception) {
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
        
        _uiState.value = _uiState.value.copy(isPolling = true)
        
        pollingJob = viewModelScope.launch {
            // Автоматическая проверка статуса авторизации каждые 5 секунд
            repeat(12) { attempt -> // 12 попыток = 1 минута
                delay(5000) // 5 секунд между проверками
                
                // Проверяем, не завершилась ли авторизация
                if (_uiState.value.isAuthSuccessful) {
                    return@repeat // Выходим из цикла при успешной авторизации
                }
                
                // Проверяем статус
                checkAuthStatus()
                
                // Если это последняя попытка и авторизация не прошла
                if (attempt == 11 && !_uiState.value.isAuthSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isPolling = false,
                        error = "Время ожидания авторизации истекло (1 минута). Попробуйте еще раз."
                    )
                }
            }
            
            _uiState.value = _uiState.value.copy(isPolling = false)
        }
    }
    
    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
        _uiState.value = _uiState.value.copy(isPolling = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }

    fun resetAuth() {
        stopPolling()
        _uiState.value = TelegramAuthUiState()
    }
    
    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
} 