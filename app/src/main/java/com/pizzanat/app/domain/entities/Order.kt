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
enum class PaymentMethod(val displayName: String) {
    CASH("Наличными"),
    CARD_ON_DELIVERY("Картой при получении"),
    ONLINE_CARD("Банковской картой онлайн")
}

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

/**
 * Оценка доставки
 */
data class DeliveryEstimate(
    val estimatedTime: Int, // в минутах
    val deliveryCost: Double
) 