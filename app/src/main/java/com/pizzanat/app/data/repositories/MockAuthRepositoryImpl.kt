/**
 * @file: MockAuthRepositoryImpl.kt
 * @description: Мок-реализация репозитория аутентификации для разработки (включая Telegram и SMS)
 * @dependencies: TokenManager, UserManager
 * @created: 2024-12-19
 * @updated: 2024-12-20 - Добавлены Telegram и SMS методы
 */
package com.pizzanat.app.data.repositories

import com.pizzanat.app.domain.entities.*
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

    // Telegram Authentication Mock Implementation
    override suspend fun initTelegramAuth(deviceId: String?): Result<TelegramAuthInitResponse> = withContext(Dispatchers.IO) {
        try {
            delay(1000) // Симуляция сетевой задержки

            val mockAuthToken = "mock_tg_auth_${System.currentTimeMillis()}"
            val mockBotUrl = "https://t.me/pizzanat_bot?start=$mockAuthToken"

            val response = TelegramAuthInitResponse(
                success = true,
                authToken = mockAuthToken,
                telegramBotUrl = mockBotUrl,
                expiresAt = "2024-12-20T15:30:00Z"
            )

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkTelegramAuthStatus(authToken: String): Result<TelegramAuthStatusResponse> = withContext(Dispatchers.IO) {
        try {
            delay(1000) // Симуляция сетевой задержки

            // Для демонстрации: 30% вероятность успешной авторизации
            val isConfirmed = (1..10).random() <= 3

            if (isConfirmed) {
                // Создаем mock пользователя Telegram
                val mockUser = User(
                    id = System.currentTimeMillis(),
                    username = "telegram_user",
                    email = "telegram@pizzanat.com",
                    firstName = "Telegram",
                    lastName = "User",
                    phone = "+79001234567"
                )

                val authResponse = AuthResponse(
                    token = "mock_tg_token_${System.currentTimeMillis()}",
                    user = mockUser
                )

                val response = TelegramAuthStatusResponse(
                    success = true,
                    status = TelegramAuthStatus.CONFIRMED,
                    message = "Авторизация подтверждена в Telegram",
                    authResponse = authResponse
                )

                Result.success(response)
            } else {
                val response = TelegramAuthStatusResponse(
                    success = true,
                    status = TelegramAuthStatus.PENDING,
                    message = "Ожидание подтверждения в Telegram",
                    authResponse = null
                )

                Result.success(response)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // SMS Authentication Mock Implementation
    override suspend fun sendSmsCode(phoneNumber: String): Result<SmsAuthResponse> = withContext(Dispatchers.IO) {
        try {
            delay(1500) // Симуляция отправки SMS

            val response = SmsAuthResponse(
                success = true,
                message = "SMS код отправлен на номер $phoneNumber",
                expiresAt = "2024-12-20T15:40:00Z",
                codeLength = 4
            )

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifySmsCode(phoneNumber: String, code: String): Result<SmsCodeVerifyResponse> = withContext(Dispatchers.IO) {
        try {
            delay(1000) // Симуляция проверки кода

            // Для демонстрации: код "1234" всегда правильный
            if (code == "1234") {
                val mockUser = User(
                    id = System.currentTimeMillis(),
                    username = phoneNumber.replace("+", "").replace("-", ""),
                    email = "$phoneNumber@pizzanat.com",
                    firstName = "SMS",
                    lastName = "User",
                    phone = phoneNumber
                )

                val authResponse = AuthResponse(
                    token = "mock_sms_token_${System.currentTimeMillis()}",
                    user = mockUser
                )

                val response = SmsCodeVerifyResponse(
                    success = true,
                    authResponse = authResponse,
                    error = null,
                    message = "Код подтвержден, вход выполнен"
                )

                Result.success(response)
            } else {
                val response = SmsCodeVerifyResponse(
                    success = false,
                    authResponse = null,
                    error = "INVALID_CODE",
                    message = "Неверный код или код истек"
                )

                Result.success(response)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 