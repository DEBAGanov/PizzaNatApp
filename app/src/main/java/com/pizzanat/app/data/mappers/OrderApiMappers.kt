/**
 * @file: OrderApiMappers.kt
 * @description: Мапперы для преобразования между Order API DTO и domain entities
 * @dependencies: OrderDto, Order, CreateOrderRequest
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.mappers

import com.pizzanat.app.data.remote.dto.*
import com.pizzanat.app.domain.entities.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Преобразование OrderDto в Order (domain)
 */
fun OrderDto.toDomain(): Order {
    return Order(
        id = id,
        userId = userId ?: 0L, // Fallback для null
        status = try {
            OrderStatus.valueOf(status.uppercase())
        } catch (e: Exception) {
            when (status.uppercase()) {
                "CREATED" -> OrderStatus.PENDING
                "CONFIRMED" -> OrderStatus.CONFIRMED
                "PREPARING" -> OrderStatus.PREPARING
                "READY" -> OrderStatus.READY
                "DELIVERING" -> OrderStatus.DELIVERING
                "DELIVERED" -> OrderStatus.DELIVERED
                "CANCELLED" -> OrderStatus.CANCELLED
                else -> OrderStatus.PENDING
            }
        },
        items = items?.map { it.toDomain() } ?: emptyList(),
        totalAmount = totalAmount,
        deliveryAddress = deliveryAddress,
        customerPhone = contactPhone,
        customerName = contactName,
        notes = notes ?: comment ?: "", // Используем notes или comment, или пустую строку
        paymentMethod = PaymentMethod.ONLINE_CARD, // По умолчанию, так как в DTO нет этого поля
        deliveryMethod = DeliveryMethod.DELIVERY, // По умолчанию, так как в DTO нет этого поля
        deliveryCost = deliveryFee,
        estimatedDeliveryTime = parseDateTime(estimatedDeliveryTime),
        createdAt = parseDateTime(createdAt),
        updatedAt = parseDateTime(updatedAt ?: createdAt) // Используем updatedAt или createdAt
    )
}

/**
 * Преобразование OrderItemDto в OrderItem (domain)
 */
fun OrderItemDto.toDomain(): OrderItem {
    return OrderItem(
        id = id,
        orderId = 0L, // В DTO нет orderId, будет установлен позже
        productId = productId,
        productName = productName,
        productPrice = productPrice ?: price, // Используем productPrice или price
        quantity = quantity,
        totalPrice = subtotal ?: (productPrice ?: price) * quantity // Используем subtotal или вычисляем
    )
}

/**
 * Преобразование списка OrderDto в список Order (domain)
 * Null-safe версия для обработки пустых ответов API
 */
fun List<OrderDto>?.toDomainOrders(): List<Order> {
    return this?.map { it.toDomain() } ?: emptyList()
}

/**
 * Преобразование AdminOrdersPageResponse в список Order (domain)
 * Для обработки Spring Boot Page структуры
 */
fun AdminOrdersPageResponse.toDomainOrders(): List<Order> {
    return content.map { it.toDomain() }
}

/**
 * Преобразование строки даты в LocalDateTime
 */
private fun parseDateTime(dateTimeString: String?): LocalDateTime {
    return if (!dateTimeString.isNullOrBlank()) {
        try {
            LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            // Fallback на текущее время если парсинг не удался
            LocalDateTime.now()
        }
    } else {
        LocalDateTime.now()
    }
} 