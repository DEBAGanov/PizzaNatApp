/**
 * @file: PaymentApiService.kt
 * @description: API сервис для работы с платежами через ЮКасса Backend
 * @dependencies: Retrofit, PaymentInfo domain models, YooKassa integration
 * @created: 2025-01-23
 * @updated: 2025-01-04 - Обновлено для соответствия Backend API ЮКасса
 */
package com.pizzanat.app.data.remote.api

import com.pizzanat.app.data.remote.dto.PaymentDto
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface PaymentApiService {
    
    /**
     * Создание платежа через ЮКасса
     * Соответствует: POST /api/v1/payments/yookassa/create
     */
    @POST("payments/yookassa/create")
    suspend fun createPayment(
        @Body request: CreatePaymentRequestDto
    ): Response<PaymentDto>
    
    /**
     * Получение информации о платеже
     * Соответствует: GET /api/v1/payments/yookassa/{paymentId}
     */
    @GET("payments/yookassa/{paymentId}")
    suspend fun getPayment(
        @Path("paymentId") paymentId: String
    ): Response<PaymentDto>
    
    /**
     * Получение списка платежей заказа
     * Соответствует: GET /api/v1/payments/yookassa/{orderId}
     */
    @GET("payments/yookassa/order/{orderId}")
    suspend fun getOrderPayments(
        @Path("orderId") orderId: Long
    ): Response<List<PaymentDto>>
    
    /**
     * Получение списка банков СБП
     * Соответствует: GET /api/v1/payments/yookassa/sbp-banks (ИСПРАВЛЕНО: было sbp/banks)
     */
    @GET("payments/yookassa/sbp-banks")
    suspend fun getSbpBanks(): Response<List<SbpBankDto>>
    
    /**
     * Упрощенный API для мобильных приложений
     * Соответствует: POST /api/v1/mobile/payments/create
     */
    @POST("mobile/payments/create")
    suspend fun createMobilePayment(
        @Body request: CreatePaymentRequestDto
    ): Response<PaymentDto>
    
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
    @SerializedName("orderId") val orderId: Long,
    @SerializedName("amount") val amount: Double, // Сумма платежа (товары + доставка)
    @SerializedName("currency") val currency: String = "RUB",
    @SerializedName("method") val method: String, // "BANK_CARD" или "SBP"
    @SerializedName("description") val description: String,
    @SerializedName("bankId") val bankId: String? = null, // Для СБП платежей
    @SerializedName("customerEmail") val customerEmail: String? = null,
    @SerializedName("customerPhone") val customerPhone: String? = null,
    @SerializedName("returnUrl") val returnUrl: String? = null
)

/**
 * DTO для банка СБП
 */
data class SbpBankDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("bic") val bic: String? = null
)

/**
 * DTO для health check
 */
data class HealthCheckDto(
    @SerializedName("status") val status: String,
    @SerializedName("timestamp") val timestamp: String
)

/**
 * DTO для подтверждения платежа
 */
data class ConfirmPaymentRequestDto(
    @SerializedName("paymentToken") val paymentToken: String
) 