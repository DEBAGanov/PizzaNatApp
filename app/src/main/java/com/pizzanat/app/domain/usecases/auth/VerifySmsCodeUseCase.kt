/**
 * @file: VerifySmsCodeUseCase.kt
 * @description: Use case для проверки SMS кода и завершения авторизации
 * @dependencies: AuthRepository
 * @created: 2025-01-23
 */
package com.pizzanat.app.domain.usecases.auth

import com.pizzanat.app.domain.entities.SmsCodeVerifyResponse
import com.pizzanat.app.domain.repositories.AuthRepository
import javax.inject.Inject

class VerifySmsCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(phoneNumber: String, code: String): Result<SmsCodeVerifyResponse> {
        // Валидация входных данных
        if (phoneNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Номер телефона не может быть пустым"))
        }

        if (code.isBlank()) {
            return Result.failure(IllegalArgumentException("SMS код не может быть пустым"))
        }

        // Валидация формата кода
        if (!code.matches(Regex("^\\d{4}$"))) {
            return Result.failure(IllegalArgumentException("SMS код должен содержать 4 цифры"))
        }

        // Валидация формата номера телефона
        val cleanedPhone = phoneNumber.replace(Regex("[^+\\d]"), "")
        if (!cleanedPhone.matches(Regex("^\\+7\\d{10}$"))) {
            return Result.failure(IllegalArgumentException("Неверный формат номера телефона"))
        }

        return try {
            authRepository.verifySmsCode(cleanedPhone, code)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
} 