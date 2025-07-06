/**
 * @file: PaymentDto.kt
 * @description: DTO классы для работы с платежами ЮКасса Backend
 * @dependencies: Gson annotations, YooKassa integration
 * @created: 2025-01-23
 * @updated: 2025-01-04 - Обновлено для соответствия ЮКасса Backend API
 */
package com.pizzanat.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO платежа ЮКасса (соответствует Backend API)
 */
data class PaymentDto(
    @SerializedName("id") val id: String,
    @SerializedName("orderId") val orderId: Long = 0,
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("currency") val currency: String = "RUB",
    @SerializedName("method") val paymentMethod: String = "BANK_CARD", // "BANK_CARD" или "SBP"
    @SerializedName("status") val status: String = "pending",
    @SerializedName("yookassaPaymentId") val yookassaPaymentId: String? = null,
    @SerializedName("confirmationUrl") val confirmationUrl: String? = null,
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("description") val description: String? = null, // Может отсутствовать в ответе
    @SerializedName("bankId") val bankId: String? = null, // Для СБП
    @SerializedName("metadata") val metadata: Map<String, String>? = null,
    @SerializedName("returnUrl") val returnUrl: String? = null,
    @SerializedName("customerEmail") val customerEmail: String? = null,
    @SerializedName("customerPhone") val customerPhone: String? = null,
    // Дополнительные поля ЮКасса
    @SerializedName("paid") val paid: Boolean? = null,
    @SerializedName("refundable") val refundable: Boolean? = null,
    @SerializedName("test") val test: Boolean? = null
) {
    // Совместимость со старым форматом
    val paymentToken: String? get() = yookassaPaymentId
}

/**
 * DTO ответа создания платежа ЮКасса
 */
data class CreatePaymentResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("payment") val payment: PaymentDto?,
    @SerializedName("error") val error: String?,
    @SerializedName("message") val message: String?
)

/**
 * DTO подтверждения платежа ЮКасса
 */
data class ConfirmPaymentResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("payment") val payment: PaymentDto?,
    @SerializedName("error") val error: String?,
    @SerializedName("message") val message: String?
) 