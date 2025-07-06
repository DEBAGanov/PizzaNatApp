/**
 * @file: AuthResponse.kt
 * @description: Доменные модели ответов аутентификации (включая Telegram и SMS)
 * @dependencies: User entity
 * @created: 2024-12-19
 * @updated: 2024-12-20 - Добавлены Telegram модели
 */
package com.pizzanat.app.domain.entities

data class AuthResponse(
    val token: String,
    val user: User
)

// Telegram Auth Models
data class TelegramAuthInitResponse(
    val success: Boolean,
    val authToken: String,
    val telegramBotUrl: String,
    val expiresAt: String,
    val message: String? = null
)

enum class TelegramAuthStatus {
    PENDING,
    CONFIRMED,
    EXPIRED
}

data class TelegramAuthStatusResponse(
    val success: Boolean,
    val status: TelegramAuthStatus,
    val message: String?,
    val authResponse: AuthResponse? // JWT token и user данные при CONFIRMED
)

// SMS Auth Models
data class SmsAuthResponse(
    val success: Boolean,
    val message: String,
    val expiresAt: String,
    val codeLength: Int,
    val maskedPhoneNumber: String? = null
)

data class SmsCodeVerifyResponse(
    val success: Boolean,
    val authResponse: AuthResponse?,
    val error: String?,
    val message: String?
)