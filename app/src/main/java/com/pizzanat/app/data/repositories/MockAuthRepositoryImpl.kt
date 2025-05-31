/**
 * @file: MockAuthRepositoryImpl.kt
 * @description: Мок-реализация репозитория аутентификации для разработки
 * @dependencies: TokenManager, UserManager
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.repositories

import com.pizzanat.app.domain.entities.AuthResponse
import com.pizzanat.app.domain.entities.User
import com.pizzanat.app.domain.repositories.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAuthRepositoryImpl @Inject constructor(
    private val tokenManager: TokenManager,
    private val userManager: UserManager
) : AuthRepository {
    
    // Симуляция базы данных пользователей
    private val mockUsers = mutableSetOf<MockUser>()
    
    data class MockUser(
        val username: String,
        val email: String,
        val password: String,
        val firstName: String,
        val lastName: String,
        val phone: String
    )
    
    override suspend fun register(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            // Симуляция задержки сети
            delay(1000)
            
            // Проверка на существующего пользователя
            val existingUser = mockUsers.find { it.email == email || it.username == username }
            if (existingUser != null) {
                return@withContext Result.failure(Exception("Пользователь с таким email или именем уже существует"))
            }
            
            // Создание нового пользователя
            val mockUser = MockUser(
                username = username,
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                phone = phone
            )
            mockUsers.add(mockUser)
            
            // Создание ответа
            val user = User(
                id = mockUsers.size.toLong(),
                username = username,
                email = email,
                firstName = firstName,
                lastName = lastName,
                phone = phone
            )
            
            val authResponse = AuthResponse(
                token = "mock_token_${System.currentTimeMillis()}",
                user = user
            )
            
            Result.success(authResponse)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            // Симуляция задержки сети
            delay(1000)
            
            // Поиск пользователя
            val mockUser = mockUsers.find { it.email == email }
            if (mockUser == null) {
                return@withContext Result.failure(Exception("Пользователь с таким email не найден"))
            }
            
            if (mockUser.password != password) {
                return@withContext Result.failure(Exception("Неверный пароль"))
            }
            
            // Создание ответа
            val user = User(
                id = 1L,
                username = mockUser.username,
                email = mockUser.email,
                firstName = mockUser.firstName,
                lastName = mockUser.lastName,
                phone = mockUser.phone
            )
            
            val authResponse = AuthResponse(
                token = "mock_token_${System.currentTimeMillis()}",
                user = user
            )
            
            Result.success(authResponse)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun saveToken(token: String) {
        tokenManager.saveToken(token)
    }
    
    override suspend fun getToken(): String? {
        return tokenManager.getToken()
    }
    
    override suspend fun isTokenValid(): Boolean {
        return tokenManager.isTokenValid()
    }
    
    override suspend fun clearToken() {
        tokenManager.clearToken()
        userManager.clearUser()
    }
    
    override suspend fun getCurrentUser(): User? {
        return userManager.getUser()
    }
    
    override suspend fun saveUser(user: User) {
        userManager.saveUser(user)
    }
} 