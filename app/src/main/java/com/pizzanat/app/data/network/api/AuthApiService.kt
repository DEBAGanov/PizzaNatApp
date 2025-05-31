/**
 * @file: AuthApiService.kt
 * @description: Retrofit интерфейс для API аутентификации
 * @dependencies: Retrofit, DTO classes
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.network.api

import com.pizzanat.app.data.network.dto.AuthResponseDto
import com.pizzanat.app.data.network.dto.LoginRequestDto
import com.pizzanat.app.data.network.dto.RegisterRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): Response<AuthResponseDto>
    
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<AuthResponseDto>
} 