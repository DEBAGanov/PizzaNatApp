/**
 * @file: PaymentMappers.kt
 * @description: Маппер функции для преобразования DTO платежей в доменные модели (ЮКасса)
 * @dependencies: Domain entities, DTO classes, YooKassa integration
 * @created: 2025-01-23
 * @updated: 2025-01-04 - Обновлено для ЮКасса Backend API
 */
package com.pizzanat.app.data.mappers

import com.pizzanat.app.data.remote.dto.PaymentDto
import com.pizzanat.app.data.remote.dto.CreatePaymentResponseDto
import com.pizzanat.app.data.remote.dto.ConfirmPaymentResponseDto
import com.pizzanat.app.data.remote.api.CreatePaymentRequestDto
import com.pizzanat.app.domain.entities.*

/**
 * Преобразование DTO платежа в доменную модель
 */
fun PaymentDto.toDomain(): PaymentInfo {
    return PaymentInfo(
        id = id,
        orderId = orderId,
        amount = amount,
        currency = currency,
        method = paymentMethod.toPaymentMethod(),
        status = status.toPaymentStatus(),
        paymentToken = paymentToken,
        confirmationUrl = confirmationUrl,
        createdAt = createdAt,
        description = description?.takeIf { it.isNotEmpty() } ?: "Оплата заказа #$orderId",
        metadata = metadata ?: emptyMap(),
        returnUrl = returnUrl,
        customerEmail = customerEmail,
        customerPhone = customerPhone
    )
}

/**
 * Преобразование доменной модели запроса в DTO для ЮКасса Backend
 */
fun CreatePaymentRequest.toDto(): CreatePaymentRequestDto {
    return CreatePaymentRequestDto(
        orderId = orderId,
        amount = amount, // Передаем полную сумму (товары + доставка)
        currency = currency,
        method = when (paymentMethod) {
            PaymentMethod.SBP -> "SBP"
            PaymentMethod.CARD_ON_DELIVERY -> "BANK_CARD"
        },
        description = description,
        bankId = if (paymentMethod == PaymentMethod.SBP) "100000000111" else null, // Сбербанк по умолчанию
        customerEmail = customerEmail,
        customerPhone = customerPhone,
        returnUrl = returnUrl
    )
}

/**
 * Преобразование строки метода оплаты в enum
 */
private fun String.toPaymentMethod(): PaymentMethod {
    return when (this.uppercase()) {
        "SBP" -> PaymentMethod.SBP
        "BANK_CARD" -> PaymentMethod.CARD_ON_DELIVERY
        else -> PaymentMethod.CARD_ON_DELIVERY
    }
}

/**
 * Преобразование строки статуса в enum
 */
private fun String.toPaymentStatus(): PaymentStatus {
    return when (this.lowercase()) {
        "pending" -> PaymentStatus.PENDING
        "processing" -> PaymentStatus.PROCESSING
        "succeeded" -> PaymentStatus.SUCCEEDED
        "failed" -> PaymentStatus.FAILED
        "cancelled" -> PaymentStatus.CANCELLED
        "refunded" -> PaymentStatus.REFUNDED
        "waiting_for_capture" -> PaymentStatus.PENDING
        else -> PaymentStatus.PENDING
    }
} 