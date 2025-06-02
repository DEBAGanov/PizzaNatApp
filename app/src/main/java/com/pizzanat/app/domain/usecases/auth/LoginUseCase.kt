/**
 * @file: LoginUseCase.kt
 * @description: Use case для входа в систему
 * @dependencies: AuthRepository
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.auth

import com.pizzanat.app.domain.entities.AuthResponse
import com.pizzanat.app.domain.repositories.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    suspend operator fun invoke(
        username: String,
        password: String
    ): Result<AuthResponse> {
        // Валидация входных данных
        if (username.isBlank()) {
            return Result.failure(IllegalArgumentException("Имя пользователя не может быть пустым"))
        }
        
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Пароль не может быть пустым"))
        }
        
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Пароль должен содержать минимум 6 символов"))
        }
        
        // Выполнение запроса к API
        return try {
            val result = authRepository.login(username, password)
            if (result.isSuccess) {
                val authResponse = result.getOrThrow()
                // Сохраняем токен и данные пользователя
                authRepository.saveToken(authResponse.token)
                authRepository.saveUser(authResponse.user)
            }
            result
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
} 