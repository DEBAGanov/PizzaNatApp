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
import android.util.Log

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
                else -> {
                    Log.w("OrderApiMappers", "Неизвестный статус заказа: $status, используем PENDING")
                    OrderStatus.PENDING
                }
            }
        },
        items = items?.map { it.toDomain(orderId = id) } ?: emptyList(),
        totalAmount = totalAmount,
        deliveryAddress = deliveryAddress,
        customerPhone = contactPhone,
        customerName = contactName,
        notes = comment ?: notes ?: "", // API использует comment, а приложение notes
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
fun OrderItemDto.toDomain(orderId: Long = 0L): OrderItem {
    return OrderItem(
        id = id,
        orderId = orderId,
        productId = productId,
        productName = productName,
        productPrice = price, // API использует price, не productPrice
        quantity = quantity,
        totalPrice = subtotal ?: (price * quantity) // Используем subtotal или вычисляем
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
 * Поддерживает ISO формат с микросекундами: 2025-06-05T16:32:37.577780
 */
private fun parseDateTime(dateTimeString: String?): LocalDateTime {
    return if (!dateTimeString.isNullOrBlank()) {
        try {
            // Сначала пробуем стандартный ISO формат
            LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            try {
                // Если не получилось, пробуем формат с микросекундами
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                LocalDateTime.parse(dateTimeString, formatter)
            } catch (e2: Exception) {
                try {
                    // Последняя попытка - формат с миллисекундами  
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                    LocalDateTime.parse(dateTimeString, formatter)
                } catch (e3: Exception) {
                    // Fallback на текущее время если ничего не получилось
                    Log.w("OrderApiMappers", "Не удалось распарсить дату: $dateTimeString", e3)
                    LocalDateTime.now()
                }
            }
        }
    } else {
        LocalDateTime.now()
    }
} 