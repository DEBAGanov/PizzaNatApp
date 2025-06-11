/**
 * @file: AuthDto.kt
 * @description: DTO классы для API аутентификации
 * @dependencies: Gson
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.network.dto

import com.google.gson.annotations.SerializedName

data class AuthResponseDto(
    @SerializedName("token")
    val token: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("id")
    val id: Long? = null
)

data class ErrorResponseDto(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("timestamp")
    val timestamp: Long
)

data class UserDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("phone")
    val phone: String
)

data class LoginRequestDto(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)

data class RegisterRequestDto(
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("phone")
    val phone: String
)

// Telegram Auth DTOs
data class TelegramAuthRequestDto(
    @SerializedName("deviceId")
    val deviceId: String?
)

data class TelegramAuthResponseDto(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("authToken")
    val authToken: String,
    @SerializedName("telegramBotUrl")
    val telegramBotUrl: String,
    @SerializedName("expiresAt")
    val expiresAt: String
)

data class TelegramAuthStatusDto(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("status")
    val status: String, // PENDING, CONFIRMED, EXPIRED
    @SerializedName("message")
    val message: String?,
    @SerializedName("token")
    val token: String?,
    @SerializedName("user")
    val user: UserDto?
)

// SMS Auth DTOs
data class SmsAuthRequestDto(
    @SerializedName("phoneNumber")
    val phoneNumber: String
)

data class SmsAuthResponseDto(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("expiresAt")
    val expiresAt: String,
    @SerializedName("codeLength")
    val codeLength: Int
)

data class SmsCodeVerifyRequestDto(
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("code")
    val code: String
)

data class SmsCodeVerifyResponseDto(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("token")
    val token: String?,
    @SerializedName("user")
    val user: UserDto?,
    @SerializedName("error")
    val error: String?,
    @SerializedName("message")
    val message: String?
) 