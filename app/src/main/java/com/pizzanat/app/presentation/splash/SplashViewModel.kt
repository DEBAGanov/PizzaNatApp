/**
 * @file: SplashViewModel.kt
 * @description: ViewModel для экрана загрузки с проверкой авторизации
 * @dependencies: Hilt, AuthRepository, TokenManager
 * @created: 2024-12-20
 */
package com.pizzanat.app.presentation.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplashUiState(
    val isCheckingAuth: Boolean = false,
    val isAuthenticated: Boolean = false,
    val authCheckCompleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()
    
    init {
        checkAuthenticationStatus()
    }
    
    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            try {
                Log.d("SplashViewModel", "🔍 Начинаем проверку авторизации...")
                
                _uiState.value = _uiState.value.copy(
                    isCheckingAuth = true,
                    error = null
                )
                
                // Минимальная задержка для показа splash screen
                delay(1500)
                
                // Проверяем наличие и валидность токена
                val hasValidToken = authRepository.isTokenValid()
                val currentUser = authRepository.getCurrentUser()
                
                Log.d("SplashViewModel", "📊 Результат проверки авторизации:")
                Log.d("SplashViewModel", "   - Токен валиден: $hasValidToken")
                Log.d("SplashViewModel", "   - Пользователь: ${currentUser?.username ?: "отсутствует"}")
                
                val isAuthenticated = hasValidToken && currentUser != null
                
                _uiState.value = _uiState.value.copy(
                    isCheckingAuth = false,
                    isAuthenticated = isAuthenticated,
                    authCheckCompleted = true
                )
                
                if (isAuthenticated) {
                    Log.d("SplashViewModel", "✅ Пользователь авторизован, переходим на главный экран")
                } else {
                    Log.d("SplashViewModel", "❌ Пользователь не авторизован, переходим к входу")
                    // Очищаем невалидные данные
                    if (!hasValidToken) {
                        authRepository.clearToken()
                    }
                }
                
            } catch (e: Exception) {
                Log.e("SplashViewModel", "💥 Ошибка при проверке авторизации: ${e.message}", e)
                
                _uiState.value = _uiState.value.copy(
                    isCheckingAuth = false,
                    isAuthenticated = false,
                    authCheckCompleted = true,
                    error = "Ошибка проверки авторизации: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Повторная проверка авторизации (для случаев ошибок)
     */
    fun retryAuthCheck() {
        _uiState.value = SplashUiState()
        checkAuthenticationStatus()
    }
} 