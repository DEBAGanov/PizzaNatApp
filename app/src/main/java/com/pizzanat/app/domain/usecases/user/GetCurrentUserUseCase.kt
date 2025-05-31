/**
 * @file: GetCurrentUserUseCase.kt
 * @description: Use case для получения данных текущего пользователя
 * @dependencies: AuthRepository
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.user

import com.pizzanat.app.domain.entities.User
import com.pizzanat.app.domain.repositories.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    suspend operator fun invoke(): Result<User?> {
        return try {
            val user = authRepository.getCurrentUser()
            Result.success(user)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
} 