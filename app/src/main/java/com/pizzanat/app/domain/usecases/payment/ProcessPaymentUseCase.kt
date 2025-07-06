/**
 * @file: ProcessPaymentUseCase.kt
 * @description: Use Case для обработки платежа через backend API
 * @dependencies: PaymentRepository
 * @created: 2025-01-23
 */
package com.pizzanat.app.domain.usecases.payment

import com.pizzanat.app.domain.entities.PaymentInfo
import com.pizzanat.app.domain.repositories.PaymentRepository
import javax.inject.Inject

class ProcessPaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    /**
     * Подтверждение платежа через backend API
     */
    suspend fun confirmPayment(paymentId: String, paymentToken: String): Result<PaymentInfo> {
        return paymentRepository.confirmPayment(paymentId, paymentToken)
    }
    
    /**
     * Отмена платежа
     */
    suspend fun cancelPayment(paymentId: String): Result<PaymentInfo> {
        return paymentRepository.cancelPayment(paymentId)
    }
    
    /**
     * Получение статуса платежа
     */
    suspend fun getPaymentStatus(paymentId: String): Result<PaymentInfo> {
        return paymentRepository.getPayment(paymentId)
    }
} 