/**
 * @file: Order.kt
 * @description: Доменные сущности для заказов, оплаты и доставки
 * @dependencies: None
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.entities

import java.time.LocalDateTime

/**
 * Способы оплаты
 */
enum class PaymentMethod(val displayName: String, val yookassaMethod: String? = null) {
    CARD_ON_DELIVERY("Картой/наличными при получении"),
    SBP("СБП", "sbp")
}

/**
 * Статусы платежа
 */
enum class PaymentStatus(val displayName: String) {
    PENDING("Ожидает оплаты"),
    PROCESSING("Обрабатывается"),
    SUCCEEDED("Оплачен"),
    FAILED("Ошибка оплаты"),
    CANCELLED("Отменен"),
    REFUNDED("Возвращен")
}

/**
 * Информация о платеже
 */
data class PaymentInfo(
    val id: String = "",
    val orderId: Long = 0,
    val amount: Double = 0.0,
    val currency: String = "RUB",
    val method: PaymentMethod,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val paymentToken: String? = null,
    val confirmationUrl: String? = null,
    val createdAt: String = "",
    val description: String = "",
    val metadata: Map<String, String> = emptyMap(),
    val returnUrl: String? = null,
    val customerEmail: String? = null,
    val customerPhone: String? = null
)

/**
 * Данные для создания платежа
 */
data class CreatePaymentRequest(
    val amount: Double,
    val currency: String = "RUB",
    val orderId: Long,
    val paymentMethod: PaymentMethod,
    val description: String,
    val customerEmail: String? = null,
    val customerPhone: String? = null,
    val returnUrl: String? = null
)

/**
 * Способы доставки
 */
enum class DeliveryMethod(val displayName: String, val cost: Double) {
    DELIVERY("Доставка курьером", 200.0),
    PICKUP("Самовывоз", 0.0)
}

/**
 * Статусы заказа
 */
enum class OrderStatus(val displayName: String) {
    PENDING("Ожидает подтверждения"),
    CONFIRMED("Подтвержден"),
    PREPARING("Готовится"),
    READY("Готов"),
    DELIVERING("В доставке"),
    DELIVERED("Доставлен"),
    CANCELLED("Отменен")
}

/**
 * Позиция в заказе
 */
data class OrderItem(
    val id: Long = 0,
    val orderId: Long = 0,
    val productId: Long,
    val productName: String,
    val productPrice: Double,
    val quantity: Int,
    val totalPrice: Double = productPrice * quantity
)

/**
 * Заказ
 */
data class Order(
    val id: Long = 0,
    val userId: Long,
    val items: List<OrderItem>,
    val status: OrderStatus = OrderStatus.PENDING,
    val totalAmount: Double,
    val deliveryMethod: DeliveryMethod,
    val deliveryAddress: String = "",
    val deliveryCost: Double = deliveryMethod.cost,
    val paymentMethod: PaymentMethod,
    val customerPhone: String,
    val customerName: String,
    val notes: String = "",
    val estimatedDeliveryTime: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val grandTotal: Double = totalAmount + deliveryCost
}

fun OrderStatus.getDisplayName(): String {
    return when (this) {
        OrderStatus.PENDING -> "Ожидает подтверждения"
        OrderStatus.CONFIRMED -> "Подтвержден"
        OrderStatus.PREPARING -> "Готовится"
        OrderStatus.READY -> "Готов к доставке"
        OrderStatus.DELIVERING -> "Доставляется"
        OrderStatus.DELIVERED -> "Доставлен"
        OrderStatus.CANCELLED -> "Отменен"
    }
} 

