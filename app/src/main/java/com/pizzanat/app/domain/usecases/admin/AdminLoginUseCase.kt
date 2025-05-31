/**
 * @file: AdminLoginUseCase.kt
 * @description: Use case для входа администратора в систему
 * @dependencies: AdminRepository
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.admin

import com.pizzanat.app.domain.entities.AdminUser
import com.pizzanat.app.domain.repositories.AdminRepository
import javax.inject.Inject

class AdminLoginUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<AdminUser> {
        if (username.isBlank()) {
            return Result.failure(Exception("Имя пользователя не может быть пустым"))
        }
        
        if (password.isBlank()) {
            return Result.failure(Exception("Пароль не может быть пустым"))
        }
        
        if (password.length < 6) {
            return Result.failure(Exception("Пароль должен содержать минимум 6 символов"))
        }
        
        return try {
            adminRepository.loginAdmin(username, password)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 