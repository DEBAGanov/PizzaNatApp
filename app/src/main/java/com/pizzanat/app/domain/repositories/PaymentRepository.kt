/**
 * @file: PaymentRepository.kt
 * @description: Репозиторий для работы с платежами
 * @dependencies: Domain entities
 * @created: 2025-01-23
 */
package com.pizzanat.app.domain.repositories

import com.pizzanat.app.domain.entities.PaymentInfo
import com.pizzanat.app.domain.entities.CreatePaymentRequest

interface PaymentRepository {
    
    /**
     * Создание платежа
     */
    suspend fun createPayment(request: CreatePaymentRequest): Result<PaymentInfo>
    
    /**
     * Получение информации о платеже
     */
    suspend fun getPayment(paymentId: String): Result<PaymentInfo>
    
    /**
     * Подтверждение платежа
     */
    suspend fun confirmPayment(paymentId: String, paymentToken: String): Result<PaymentInfo>
    
    /**
     * Отмена платежа
     */
    suspend fun cancelPayment(paymentId: String): Result<PaymentInfo>
    
    /**
     * Получение списка платежей заказа
     */
    suspend fun getOrderPayments(orderId: Long): Result<List<PaymentInfo>>
} 