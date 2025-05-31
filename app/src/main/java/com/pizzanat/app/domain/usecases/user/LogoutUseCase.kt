/**
 * @file: LogoutUseCase.kt
 * @description: Use case для выхода из аккаунта
 * @dependencies: AuthRepository
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.user

import com.pizzanat.app.domain.repositories.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    suspend operator fun invoke(): Result<Unit> {
        return try {
            authRepository.clearToken()
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
} 