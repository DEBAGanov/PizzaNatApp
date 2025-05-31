/**
 * @file: AuthInterceptor.kt
 * @description: Interceptor для автоматического добавления JWT токенов к HTTP запросам
 * @dependencies: OkHttp, TokenManager, Coroutines
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.network.interceptors

import android.util.Log
import com.pizzanat.app.data.repositories.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Пропускаем запросы аутентификации
        val isAuthRequest = originalRequest.url.encodedPath.contains("/auth/")
        
        if (isAuthRequest) {
            Log.d("AuthInterceptor", "Пропускаем auth запрос: ${originalRequest.url}")
            return chain.proceed(originalRequest)
        }
        
        // Добавляем токен к остальным запросам
        val token = runBlocking { tokenManager.getToken() }
        
        Log.d("AuthInterceptor", "URL: ${originalRequest.url}")
        Log.d("AuthInterceptor", "Токен найден: ${!token.isNullOrBlank()}")
        if (!token.isNullOrBlank()) {
            Log.d("AuthInterceptor", "Токен (первые 20 символов): ${token.take(20)}...")
        }
        
        val newRequest = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            Log.w("AuthInterceptor", "Токен отсутствует! Запрос без авторизации.")
            originalRequest
        }
        
        val response = chain.proceed(newRequest)
        
        // Обработка ответа 401 (неавторизован)
        if (response.code == 401) {
            Log.w("AuthInterceptor", "Получен 401 ответ, очищаем токен")
            // Очищаем недействительный токен
            runBlocking { tokenManager.clearToken() }
        }
        
        return response
    }
} 