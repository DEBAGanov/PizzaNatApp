/**
 * @file: CreatePaymentUseCase.kt
 * @description: Use Case для создания платежа через ЮКасса
 * @dependencies: PaymentRepository
 * @created: 2025-01-23
 */
package com.pizzanat.app.domain.usecases.payment

import com.pizzanat.app.domain.entities.CreatePaymentRequest
import com.pizzanat.app.domain.entities.PaymentInfo
import com.pizzanat.app.domain.repositories.PaymentRepository
import javax.inject.Inject

class CreatePaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(request: CreatePaymentRequest): Result<PaymentInfo> {
        return paymentRepository.createPayment(request)
    }
} 