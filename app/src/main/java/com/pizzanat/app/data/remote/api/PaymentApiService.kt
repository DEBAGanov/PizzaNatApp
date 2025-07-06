/**
 * @file: PaymentApiService.kt
 * @description: API сервис для работы с платежами через ЮКасса Backend
 * @dependencies: Retrofit, PaymentInfo domain models, YooKassa integration
 * @created: 2025-01-23
 * @updated: 2025-01-04 - Обновлено для соответствия Backend API ЮКасса
 */
package com.pizzanat.app.data.remote.api

import com.pizzanat.app.data.remote.dto.PaymentDto
import retrofit2.Response
import retrofit2.http.*

interface PaymentApiService {
    
    /**
     * Создание платежа через ЮКасса
     */
    @POST("payments/yookassa/create")
    suspend fun createPayment(
        @Body request: CreatePaymentRequestDto
    ): Response<PaymentDto>
    
    /**
     * Получение информации о платеже
     */
    @GET("payments/yookassa/{paymentId}")
    suspend fun getPayment(
        @Path("paymentId") paymentId: String
    ): Response<PaymentDto>
    
    /**
     * Получение списка платежей заказа
     */
    @GET("payments/yookassa/order/{orderId}")
    suspend fun getOrderPayments(
        @Path("orderId") orderId: Long
    ): Response<List<PaymentDto>>
    
    /**
     * Получение списка банков СБП
     */
    @GET("payments/yookassa/sbp/banks")
    suspend fun getSbpBanks(): Response<List<SbpBankDto>>
    
    /**
     * Health check ЮКасса
     */
    @GET("payments/yookassa/health")
    suspend fun getYookassaHealth(): Response<HealthCheckDto>
    
    /**
     * Подтверждение платежа (если нужно)
     */
    @POST("payments/{paymentId}/confirm")
    suspend fun confirmPayment(
        @Path("paymentId") paymentId: String,
        @Body request: ConfirmPaymentRequestDto
    ): Response<PaymentDto>
    
    /**
     * Отмена платежа (если нужно)
     */
    @POST("payments/{paymentId}/cancel")
    suspend fun cancelPayment(
        @Path("paymentId") paymentId: String
    ): Response<PaymentDto>
}

/**
 * DTO для создания платежа ЮКасса
 */
data class CreatePaymentRequestDto(
    val orderId: Long,
    val amount: Double, // Сумма платежа (товары + доставка)
    val currency: String = "RUB",
    val method: String, // "BANK_CARD" или "SBP"
    val description: String,
    val bankId: String? = null, // Для СБП платежей
    val customerEmail: String? = null,
    val customerPhone: String? = null,
    val returnUrl: String? = null
)

/**
 * DTO для банка СБП
 */
data class SbpBankDto(
    val id: String,
    val name: String,
    val bic: String? = null
)

/**
 * DTO для health check
 */
data class HealthCheckDto(
    val status: String,
    val timestamp: String
)

/**
 * DTO для подтверждения платежа
 */
data class ConfirmPaymentRequestDto(
    val paymentToken: String
) 