/**
 * @file: InitTelegramAuthUseCase.kt
 * @description: Use case для инициализации Telegram авторизации
 * @dependencies: AuthRepository
 * @created: 2024-12-20
 */
package com.pizzanat.app.domain.usecases.auth

import com.pizzanat.app.domain.entities.TelegramAuthInitResponse
import com.pizzanat.app.domain.repositories.AuthRepository
import javax.inject.Inject

class InitTelegramAuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    suspend operator fun invoke(deviceId: String? = null): Result<TelegramAuthInitResponse> {
        return try {
            authRepository.initTelegramAuth(deviceId)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
} 