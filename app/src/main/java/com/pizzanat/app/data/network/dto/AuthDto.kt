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
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("phone")
    val phone: String? = null
)

data class ErrorResponseDto(
    @SerializedName("status")
    val status: Int? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("timestamp")
    val timestamp: Long? = null,
    @SerializedName("error")
    val error: String? = null
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
    val phone: String? = null
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
    val expiresAt: String,
    @SerializedName("message")
    val message: String? = null
)

data class TelegramAuthStatusDto(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("status")
    val status: String, // PENDING, CONFIRMED, EXPIRED
    @SerializedName("message")
    val message: String?,
    @SerializedName("authData")
    val authData: TelegramAuthDataDto?
)

data class TelegramAuthDataDto(
    @SerializedName("token")
    val token: String,
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String?,
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("lastName")
    val lastName: String?
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
    val codeLength: Int,
    @SerializedName("maskedPhoneNumber")
    val maskedPhoneNumber: String? = null
)

data class SmsCodeVerifyRequestDto(
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("code")
    val code: String
)

data class SmsCodeVerifyResponseDto(
    @SerializedName("token")
    val token: String,
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String?,
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("lastName")
    val lastName: String?
) 