/**
 * @file: ApiLogger.kt
 * @description: Детальное логирование API запросов для E2E тестирования
 * @dependencies: Android Log, BuildConfig
 * @created: 2024-12-20
 */
package com.pizzanat.app.utils

import android.util.Log
import com.pizzanat.app.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class ApiLogEntry(
    val timestamp: String,
    val method: String,
    val url: String,
    val statusCode: Int?,
    val requestBody: String?,
    val responseBody: String?,
    val error: String?,
    val duration: Long? = null
) {
    val isSuccess: Boolean get() = statusCode in 200..299
    val formattedDuration: String get() = duration?.let { "${it}ms" } ?: "N/A"
}

object ApiLogger {
    
    private const val TAG = "ApiLogger"
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    
    private val _logs = MutableStateFlow<List<ApiLogEntry>>(emptyList())
    val logs: StateFlow<List<ApiLogEntry>> = _logs.asStateFlow()
    
    fun logRequest(
        method: String,
        url: String,
        requestBody: String? = null
    ): RequestLogger {
        val startTime = System.currentTimeMillis()
        val timestamp = dateFormat.format(Date(startTime))
        
        if (BuildConfigUtils.isVerboseLoggingEnabled()) {
            Log.d(TAG, "🚀 REQUEST [$method] $url")
            if (requestBody != null) {
                Log.d(TAG, "📤 REQUEST BODY: $requestBody")
            }
        }
        
        return RequestLogger(
            method = method,
            url = url,
            requestBody = requestBody,
            startTime = startTime,
            timestamp = timestamp
        )
    }
    
    fun clearLogs() {
        _logs.value = emptyList()
    }
    
    fun getLogsForUrl(url: String): List<ApiLogEntry> {
        return _logs.value.filter { it.url.contains(url) }
    }
    
    fun getLatestLog(): ApiLogEntry? {
        return _logs.value.lastOrNull()
    }
    
    private fun addLog(entry: ApiLogEntry) {
        _logs.value = (_logs.value + entry).takeLast(100) // Храним последние 100 логов
    }
    
    class RequestLogger(
        private val method: String,
        private val url: String,
        private val requestBody: String?,
        private val startTime: Long,
        private val timestamp: String
    ) {
        
        fun logSuccess(statusCode: Int, responseBody: String?) {
            val duration = System.currentTimeMillis() - startTime
            
            if (BuildConfigUtils.isVerboseLoggingEnabled()) {
                Log.d(TAG, "✅ RESPONSE [$statusCode] $url (${duration}ms)")
                if (responseBody != null) {
                    Log.d(TAG, "📥 RESPONSE BODY: $responseBody")
                }
            }
            
            val entry = ApiLogEntry(
                timestamp = timestamp,
                method = method,
                url = url,
                statusCode = statusCode,
                requestBody = requestBody,
                responseBody = responseBody,
                error = null,
                duration = duration
            )
            
            ApiLogger.addLog(entry)
        }
        
        fun logError(statusCode: Int?, error: String, responseBody: String? = null) {
            val duration = System.currentTimeMillis() - startTime
            
            if (BuildConfigUtils.isVerboseLoggingEnabled()) {
                Log.e(TAG, "❌ ERROR [${statusCode ?: "N/A"}] $url (${duration}ms)")
                Log.e(TAG, "💥 ERROR MESSAGE: $error")
                if (responseBody != null) {
                    Log.e(TAG, "📥 ERROR RESPONSE: $responseBody")
                }
            }
            
            val entry = ApiLogEntry(
                timestamp = timestamp,
                method = method,
                url = url,
                statusCode = statusCode,
                requestBody = requestBody,
                responseBody = responseBody,
                error = error,
                duration = duration
            )
            
            ApiLogger.addLog(entry)
        }
        
        fun logNetworkError(error: String) {
            val duration = System.currentTimeMillis() - startTime
            
            if (BuildConfigUtils.isVerboseLoggingEnabled()) {
                Log.e(TAG, "🔌 NETWORK ERROR $url (${duration}ms)")
                Log.e(TAG, "💥 NETWORK ERROR: $error")
            }
            
            val entry = ApiLogEntry(
                timestamp = timestamp,
                method = method,
                url = url,
                statusCode = null,
                requestBody = requestBody,
                responseBody = null,
                error = "Network Error: $error",
                duration = duration
            )
            
            ApiLogger.addLog(entry)
        }
    }
} 