/**
 * @file: AuthRepository.kt
 * @description: Интерфейс репозитория для аутентификации (включая Telegram и SMS)
 * @dependencies: Domain entities
 * @created: 2024-12-19
 * @updated: 2024-12-20 - Добавлены Telegram и SMS методы
 */
package com.pizzanat.app.domain.repositories

import com.pizzanat.app.domain.entities.*

interface AuthRepository {

    /**
     * Регистрация нового пользователя
     */
    suspend fun register(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Result<AuthResponse>

    /**
     * Вход в систему
     */
    suspend fun login(
        username: String,
        password: String
    ): Result<AuthResponse>

    // Telegram Authentication
    /**
     * Инициализация Telegram авторизации
     */
    suspend fun initTelegramAuth(deviceId: String? = null): Result<TelegramAuthInitResponse>

    /**
     * Проверка статуса Telegram авторизации
     */
    suspend fun checkTelegramAuthStatus(authToken: String): Result<TelegramAuthStatusResponse>

    // SMS Authentication
    /**
     * Отправка SMS кода
     */
    suspend fun sendSmsCode(phoneNumber: String): Result<SmsAuthResponse>

    /**
     * Проверка SMS кода
     */
    suspend fun verifySmsCode(phoneNumber: String, code: String): Result<SmsCodeVerifyResponse>

    /**
     * Сохранение JWT токена
     */
    suspend fun saveToken(token: String)

    /**
     * Получение сохраненного токена
     */
    suspend fun getToken(): String?

    /**
     * Проверка валидности токена
     */
    suspend fun isTokenValid(): Boolean

    /**
     * Очистка токена (выход)
     */
    suspend fun clearToken()

    /**
     * Получение текущего пользователя
     */
    suspend fun getCurrentUser(): User?

    /**
     * Сохранение данных пользователя
     */
    suspend fun saveUser(user: User)
}