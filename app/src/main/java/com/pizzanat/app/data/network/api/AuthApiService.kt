/**
 * @file: AuthApiService.kt
 * @description: Retrofit интерфейс для API аутентификации (включая Telegram и SMS)
 * @dependencies: Retrofit, DTO classes
 * @created: 2024-12-19
 * @updated: 2024-12-20 - Добавлены Telegram и SMS эндпоинты
 */
package com.pizzanat.app.data.network.api

import com.pizzanat.app.data.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): Response<AuthResponseDto>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<AuthResponseDto>

    // Telegram Authentication
    @POST("auth/telegram/init")
    suspend fun initTelegramAuth(
        @Body request: TelegramAuthRequestDto
    ): Response<TelegramAuthResponseDto>

    @GET("auth/telegram/status/{authToken}")
    suspend fun checkTelegramAuthStatus(
        @Path("authToken") authToken: String
    ): Response<TelegramAuthStatusDto>

    // SMS Authentication
    @POST("auth/sms/send-code")
    suspend fun sendSmsCode(
        @Body request: SmsAuthRequestDto
    ): Response<SmsAuthResponseDto>

    @POST("auth/sms/verify-code")
    suspend fun verifySmsCode(
        @Body request: SmsCodeVerifyRequestDto
    ): Response<SmsCodeVerifyResponseDto>
}