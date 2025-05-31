/**
 * @file: AuthRepository.kt
 * @description: Интерфейс репозитория для аутентификации
 * @dependencies: Domain entities
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.repositories

import com.pizzanat.app.domain.entities.AuthResponse
import com.pizzanat.app.domain.entities.User

interface AuthRepository {
    
    /**
     * Регистрация нового пользователя
     */
    suspend fun register(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Result<AuthResponse>
    
    /**
     * Вход в систему
     */
    suspend fun login(
        email: String,
        password: String
    ): Result<AuthResponse>
    
    /**
     * Сохранение JWT токена
     */
    suspend fun saveToken(token: String)
    
    /**
     * Получение сохраненного токена
     */
    suspend fun getToken(): String?
    
    /**
     * Проверка валидности токена
     */
    suspend fun isTokenValid(): Boolean
    
    /**
     * Очистка токена (выход)
     */
    suspend fun clearToken()
    
    /**
     * Получение текущего пользователя
     */
    suspend fun getCurrentUser(): User?
    
    /**
     * Сохранение данных пользователя
     */
    suspend fun saveUser(user: User)
} 