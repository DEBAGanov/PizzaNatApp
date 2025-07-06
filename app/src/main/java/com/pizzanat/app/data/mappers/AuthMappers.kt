/**
 * @file: AuthMappers.kt
 * @description: Маппер функции для преобразования DTO в доменные модели аутентификации
 * @dependencies: Domain entities, DTO classes
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.mappers

import com.pizzanat.app.data.network.dto.*
import com.pizzanat.app.domain.entities.*

/**
 * Преобразование UserDto в User
 */
fun UserDto.toDomain(): User {
    return User(
        id = this.id,
        username = this.username,
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        phone = this.phone
    )
}

/**
 * Генерация уникального ID на основе email
 */
private fun generateUserIdFromEmail(email: String): Long {
    return email.hashCode().toLong().let { 
        if (it < 0) -it else it // Делаем положительным числом
    }
}

/**
 * Преобразование AuthResponseDto в AuthResponse
 */
fun AuthResponseDto.toDomain(): AuthResponse {
    return AuthResponse(
        token = this.token,
        user = User(
            id = this.userId, // Используем userId из Backend API
            username = this.username,
            email = this.email,
            firstName = this.firstName,
            lastName = this.lastName,
            phone = this.phone ?: "" // phone может быть null
        )
    )
}

/**
 * Преобразование User в UserDto
 */
fun User.toDto(): UserDto {
    return UserDto(
        id = this.id,
        username = this.username,
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        phone = this.phone
    )
}

// Telegram Auth Mappers
/**
 * Преобразование TelegramAuthResponseDto в TelegramAuthInitResponse
 */
fun TelegramAuthResponseDto.toDomain(): TelegramAuthInitResponse {
    return TelegramAuthInitResponse(
        success = this.success,
        authToken = this.authToken,
        telegramBotUrl = this.telegramBotUrl,
        expiresAt = this.expiresAt,
        message = this.message
    )
}

/**
 * Преобразование строки статуса в enum
 */
private fun String.toTelegramAuthStatus(): TelegramAuthStatus {
    return when (this.uppercase()) {
        "PENDING" -> TelegramAuthStatus.PENDING
        "CONFIRMED" -> TelegramAuthStatus.CONFIRMED
        "EXPIRED" -> TelegramAuthStatus.EXPIRED
        else -> TelegramAuthStatus.EXPIRED
    }
}

/**
 * Преобразование TelegramAuthStatusDto в TelegramAuthStatusResponse
 */
fun TelegramAuthStatusDto.toDomain(): TelegramAuthStatusResponse {
    val authResponse = if (this.authData != null) {
        AuthResponse(
            token = this.authData.token,
            user = User(
                id = this.authData.userId,
                username = this.authData.username,
                email = this.authData.email ?: "",
                firstName = this.authData.firstName ?: "",
                lastName = this.authData.lastName ?: "",
                phone = "" // Telegram авторизация не предоставляет номер телефона
            )
        )
    } else null
    
    return TelegramAuthStatusResponse(
        success = this.success,
        status = this.status.toTelegramAuthStatus(),
        message = this.message,
        authResponse = authResponse
    )
}

// SMS Auth Mappers
/**
 * Преобразование SmsAuthResponseDto в SmsAuthResponse
 */
fun SmsAuthResponseDto.toDomain(): SmsAuthResponse {
    return SmsAuthResponse(
        success = this.success,
        message = this.message,
        expiresAt = this.expiresAt,
        codeLength = this.codeLength,
        maskedPhoneNumber = this.maskedPhoneNumber
    )
}

/**
 * Преобразование SmsCodeVerifyResponseDto в SmsCodeVerifyResponse
 */
fun SmsCodeVerifyResponseDto.toDomain(): SmsCodeVerifyResponse {
    val authResponse = AuthResponse(
        token = this.token,
        user = User(
            id = this.userId,
            username = this.username,
            email = this.email ?: "",
            firstName = this.firstName ?: "",
            lastName = this.lastName ?: "",
            phone = this.username // Backend возвращает номер телефона в username
        )
    )
    
    return SmsCodeVerifyResponse(
        success = true, // Если получили ответ с токеном - значит успешно
        authResponse = authResponse,
        error = null,
        message = "Авторизация успешна"
    )
} 