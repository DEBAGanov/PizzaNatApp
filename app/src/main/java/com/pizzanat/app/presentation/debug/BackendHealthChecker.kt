/**
 * @file: BackendHealthChecker.kt
 * @description: Проверка доступности Backend API для Telegram авторизации
 * @dependencies: Coroutines, OkHttp, BuildConfig
 * @created: 2024-12-20
 */
package com.pizzanat.app.presentation.debug

import android.util.Log
import com.pizzanat.app.BuildConfig
import com.pizzanat.app.utils.BuildConfigUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

data class BackendHealthStatus(
    val isAvailable: Boolean = false,
    val responseTime: Long? = null,
    val httpStatus: Int? = null,
    val error: String? = null,
    val lastChecked: Long = System.currentTimeMillis(),
    val telegramEndpointsAvailable: Boolean = false
)

class BackendHealthChecker {
    companion object {
        private const val TAG = "BackendHealthChecker"
        private const val HEALTH_CHECK_INTERVAL = 30_000L // 30 секунд
        private const val REQUEST_TIMEOUT = 10_000L // 10 секунд
    }
    
    private val _healthStatus = MutableStateFlow(BackendHealthStatus())
    val healthStatus: StateFlow<BackendHealthStatus> = _healthStatus.asStateFlow()
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
        .readTimeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
        .writeTimeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
        .build()
    
    private var healthCheckJob: Job? = null
    
    /**
     * Запуск периодической проверки здоровья Backend
     */
    fun startPeriodicHealthCheck() {
        if (!BuildConfig.DEBUG) return
        
        stopPeriodicHealthCheck()
        
        healthCheckJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    checkBackendHealth()
                    delay(HEALTH_CHECK_INTERVAL)
                } catch (e: CancellationException) {
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка в периодической проверке здоровья: ${e.message}")
                    delay(HEALTH_CHECK_INTERVAL)
                }
            }
        }
        
        Log.d(TAG, "Запущена периодическая проверка Backend API каждые ${HEALTH_CHECK_INTERVAL / 1000} секунд")
    }
    
    /**
     * Остановка периодической проверки
     */
    fun stopPeriodicHealthCheck() {
        healthCheckJob?.cancel()
        healthCheckJob = null
        Log.d(TAG, "Остановлена периодическая проверка Backend API")
    }
    
    /**
     * Единоразовая проверка здоровья Backend
     */
    suspend fun checkBackendHealth(): BackendHealthStatus {
        return withContext(Dispatchers.IO) {
            val baseUrl = BuildConfigUtils.getBaseUrl()
            val startTime = System.currentTimeMillis()
            
            try {
                Log.d(TAG, "Проверка доступности Backend API: $baseUrl")
                
                // Проверяем основной health endpoint
                val healthEndpoint = baseUrl.removeSuffix("/") + "/health"
                val generalHealthStatus = checkEndpoint(healthEndpoint)
                
                // Проверяем специфичные Telegram эндпоинты
                val telegramEndpointsStatus = checkTelegramEndpoints(baseUrl)
                
                val responseTime = System.currentTimeMillis() - startTime
                
                val status = BackendHealthStatus(
                    isAvailable = generalHealthStatus.success,
                    responseTime = responseTime,
                    httpStatus = generalHealthStatus.httpStatus,
                    error = generalHealthStatus.error,
                    lastChecked = System.currentTimeMillis(),
                    telegramEndpointsAvailable = telegramEndpointsStatus
                )
                
                _healthStatus.value = status
                
                if (status.isAvailable) {
                    Log.d(TAG, "✅ Backend API доступен (${responseTime}ms, HTTP ${status.httpStatus})")
                } else {
                    Log.w(TAG, "❌ Backend API недоступен: ${status.error}")
                }
                
                return@withContext status
                
            } catch (e: Exception) {
                val errorStatus = BackendHealthStatus(
                    isAvailable = false,
                    responseTime = System.currentTimeMillis() - startTime,
                    error = "Критическая ошибка: ${e.message}",
                    lastChecked = System.currentTimeMillis()
                )
                
                _healthStatus.value = errorStatus
                Log.e(TAG, "❌ Критическая ошибка проверки Backend: ${e.message}", e)
                
                return@withContext errorStatus
            }
        }
    }
    
    /**
     * Проверка конкретного эндпоинта
     */
    private suspend fun checkEndpoint(url: String): EndpointCheckResult {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", BuildConfigUtils.getUserAgent())
                    .get()
                    .build()
                
                val response = httpClient.newCall(request).execute()
                val httpStatus = response.code
                
                response.use {
                    EndpointCheckResult(
                        success = httpStatus in 200..299,
                        httpStatus = httpStatus,
                        error = if (httpStatus in 200..299) null else "HTTP $httpStatus"
                    )
                }
                
            } catch (e: IOException) {
                EndpointCheckResult(
                    success = false,
                    error = "Network error: ${e.message}"
                )
            } catch (e: Exception) {
                EndpointCheckResult(
                    success = false,
                    error = "Exception: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Проверка доступности Telegram эндпоинтов
     */
    private suspend fun checkTelegramEndpoints(baseUrl: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Проверяем эндпоинт инициализации Telegram авторизации с пустым телом
                val initEndpoint = baseUrl.removeSuffix("/") + "/auth/telegram/init"
                val initResult = checkEndpoint(initEndpoint)
                
                // Статус 400 (Bad Request) ожидаем для пустого запроса - это означает что эндпоинт существует
                // Статус 404 означает что эндпоинт не реализован
                // Статус 200 означает что эндпоинт работает (хотя с пустым телом вряд ли)
                val telegramInitAvailable = when (initResult.httpStatus) {
                    400, 422 -> true // Bad Request - эндпоинт существует, но нужно тело запроса
                    200, 201 -> true // OK - эндпоинт работает
                    404 -> false // Not Found - эндпоинт не реализован
                    500 -> true // Internal Server Error - эндпоинт существует, но есть проблемы на сервере
                    else -> false
                }
                
                Log.d(TAG, "Telegram init endpoint check: HTTP ${initResult.httpStatus} -> Available: $telegramInitAvailable")
                
                return@withContext telegramInitAvailable
                
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка проверки Telegram эндпоинтов: ${e.message}")
                return@withContext false
            }
        }
    }
    
    /**
     * Получение текущего статуса без новой проверки
     */
    fun getCurrentStatus(): BackendHealthStatus {
        return _healthStatus.value
    }
    
    /**
     * Проверка готовности для E2E тестирования
     */
    fun isReadyForE2ETesting(): Boolean {
        val status = getCurrentStatus()
        return status.isAvailable && status.telegramEndpointsAvailable
    }
    
    fun cleanup() {
        stopPeriodicHealthCheck()
    }
    
    private data class EndpointCheckResult(
        val success: Boolean,
        val httpStatus: Int? = null,
        val error: String? = null
    )
} 