/**
 * @file: SendSmsCodeUseCase.kt
 * @description: Use case для отправки SMS кода на номер телефона
 * @dependencies: AuthRepository
 * @created: 2025-01-23
 */
package com.pizzanat.app.domain.usecases.auth

import com.pizzanat.app.domain.entities.SmsAuthResponse
import com.pizzanat.app.domain.repositories.AuthRepository
import javax.inject.Inject

class SendSmsCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(phoneNumber: String): Result<SmsAuthResponse> {
        // Валидация входных данных
        if (phoneNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Номер телефона не может быть пустым"))
        }

        // Валидация формата номера телефона
        val cleanedPhone = phoneNumber.replace(Regex("[^+\\d]"), "")
        if (!cleanedPhone.matches(Regex("^\\+7\\d{10}$"))) {
            return Result.failure(IllegalArgumentException("Неверный формат номера телефона. Используйте формат +7XXXXXXXXXX"))
        }

        return try {
            authRepository.sendSmsCode(cleanedPhone)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
} 