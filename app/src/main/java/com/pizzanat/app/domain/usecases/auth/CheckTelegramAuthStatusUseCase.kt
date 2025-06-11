/**
 * @file: CheckTelegramAuthStatusUseCase.kt
 * @description: Use case для проверки статуса Telegram авторизации
 * @dependencies: AuthRepository
 * @created: 2024-12-20
 */
package com.pizzanat.app.domain.usecases.auth

import com.pizzanat.app.domain.entities.TelegramAuthStatusResponse
import com.pizzanat.app.domain.repositories.AuthRepository
import javax.inject.Inject

class CheckTelegramAuthStatusUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(authToken: String): Result<TelegramAuthStatusResponse> {
        // Валидация входных данных
        if (authToken.isBlank()) {
            return Result.failure(IllegalArgumentException("Auth token не может быть пустым"))
        }

        return try {
            authRepository.checkTelegramAuthStatus(authToken)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}