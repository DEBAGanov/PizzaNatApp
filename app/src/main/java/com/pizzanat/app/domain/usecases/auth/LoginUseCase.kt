/**
 * @file: LoginUseCase.kt
 * @description: Use case для входа в систему с поддержкой email и username
 * @dependencies: AuthRepository
 * @created: 2024-12-19
 * @updated: 2024-12-20 - Добавлена поддержка авторизации по email и username
 */
package com.pizzanat.app.domain.usecases.auth

import android.util.Log
import com.pizzanat.app.domain.entities.AuthResponse
import com.pizzanat.app.domain.repositories.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    suspend operator fun invoke(
        usernameOrEmail: String,
        password: String
    ): Result<AuthResponse> {
        // Валидация входных данных
        if (usernameOrEmail.isBlank()) {
            return Result.failure(IllegalArgumentException("Имя пользователя или email не может быть пустым"))
        }
        
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Пароль не может быть пустым"))
        }
        
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Пароль должен содержать минимум 6 символов"))
        }
        
        // Выполнение запроса к API
        return try {
            Log.d("LoginUseCase", "🔐 Попытка авторизации: $usernameOrEmail")
            
            // Определяем, что ввел пользователь - email или username
            val isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()
            
            var result: Result<AuthResponse>
            
            if (isEmail) {
                Log.d("LoginUseCase", "📧 Обнаружен email, пробуем авторизацию с email")
                // Сначала пробуем с email
                result = authRepository.login(usernameOrEmail, password)
                
                if (!result.isSuccess) {
                    // Если не удалось с email, пробуем извлечь username из email
                    val extractedUsername = usernameOrEmail.substringBefore("@")
                    Log.d("LoginUseCase", "👤 Email не сработал, пробуем с извлеченным username: $extractedUsername")
                    result = authRepository.login(extractedUsername, password)
                }
            } else {
                Log.d("LoginUseCase", "👤 Обнаружен username, пробуем авторизацию с username")
                // Если это не email, используем как username
                result = authRepository.login(usernameOrEmail, password)
            }
            
            if (result.isSuccess) {
                val authResponse = result.getOrThrow()
                Log.d("LoginUseCase", "✅ Авторизация успешна для пользователя: ${authResponse.user.username}")
                // Сохраняем токен и данные пользователя
                authRepository.saveToken(authResponse.token)
                authRepository.saveUser(authResponse.user)
            } else {
                Log.e("LoginUseCase", "❌ Авторизация не удалась: ${result.exceptionOrNull()?.message}")
            }
            
            result
        } catch (exception: Exception) {
            Log.e("LoginUseCase", "❌ Исключение при авторизации: ${exception.message}")
            Result.failure(exception)
        }
    }
} 