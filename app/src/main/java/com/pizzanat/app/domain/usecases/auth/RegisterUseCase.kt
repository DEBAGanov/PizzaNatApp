/**
 * @file: RegisterUseCase.kt
 * @description: Use case для регистрации нового пользователя
 * @dependencies: AuthRepository
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.auth

import com.pizzanat.app.domain.entities.AuthResponse
import com.pizzanat.app.domain.repositories.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Result<AuthResponse> {
        // Валидация входных данных
        if (username.isBlank()) {
            return Result.failure(IllegalArgumentException("Имя пользователя не может быть пустым"))
        }
        
        if (username.length < 3) {
            return Result.failure(IllegalArgumentException("Имя пользователя должно содержать минимум 3 символа"))
        }
        
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email не может быть пустым"))
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(IllegalArgumentException("Неверный формат email"))
        }
        
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Пароль не может быть пустым"))
        }
        
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Пароль должен содержать минимум 6 символов"))
        }
        
        if (firstName.isBlank()) {
            return Result.failure(IllegalArgumentException("Имя не может быть пустым"))
        }
        
        if (lastName.isBlank()) {
            return Result.failure(IllegalArgumentException("Фамилия не может быть пустой"))
        }
        
        if (phone.isBlank()) {
            return Result.failure(IllegalArgumentException("Телефон не может быть пустым"))
        }
        
        // Выполнение запроса к API
        return try {
            val result = authRepository.register(
                username = username,
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                phone = phone
            )
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