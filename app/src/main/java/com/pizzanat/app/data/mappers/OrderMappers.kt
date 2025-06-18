/**
 * @file: OrderMappers.kt
 * @description: Маппер функции для преобразования между Entity и Domain объектами заказов
 * @dependencies: Order, OrderEntity, OrderItemEntity
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.mappers

import com.pizzanat.app.data.local.entities.OrderEntity
import com.pizzanat.app.data.local.entities.OrderItemEntity
import com.pizzanat.app.data.remote.dto.AdminOrdersPageResponse
import com.pizzanat.app.data.remote.dto.CreateOrderRequest
import com.pizzanat.app.data.remote.dto.OrderDto
import com.pizzanat.app.data.remote.dto.OrderItemDto
import com.pizzanat.app.domain.entities.Order
import com.pizzanat.app.domain.entities.OrderItem
import com.pizzanat.app.domain.entities.OrderStatus
import com.pizzanat.app.domain.entities.DeliveryMethod
import com.pizzanat.app.domain.entities.PaymentMethod
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Преобразование OrderEntity в Order (Domain)
 */
fun OrderEntity.toDomain(orderItems: List<OrderItemEntity> = emptyList()): Order {
    return Order(
        id = id,
        userId = userId,
        items = orderItems.map { it.toDomain() },
        status = status,
        totalAmount = totalAmount,
        deliveryMethod = deliveryMethod,
        deliveryAddress = deliveryAddress,
        deliveryCost = deliveryCost,
        paymentMethod = paymentMethod,
        customerPhone = customerPhone,
        customerName = customerName,
        notes = notes,
        createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.systemDefault()),
        updatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(updatedAt), ZoneId.systemDefault()),
        estimatedDeliveryTime = estimatedDeliveryTime?.let { 
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) 
        }
    )
}

/**
 * Преобразование OrderItemEntity в OrderItem (Domain)
 */
fun OrderItemEntity.toDomain(): OrderItem {
    return OrderItem(
        id = id,
        productId = productId,
        productName = productName,
        productPrice = productPrice,
        quantity = quantity
    )
}

/**
 * Преобразование Order (Domain) в OrderEntity
 */
fun Order.toEntity(): OrderEntity {
    return OrderEntity(
        id = id,
        userId = userId,
        status = status,
        totalAmount = totalAmount,
        deliveryMethod = deliveryMethod,
        deliveryAddress = deliveryAddress,
        deliveryCost = deliveryCost,
        paymentMethod = paymentMethod,
        customerPhone = customerPhone,
        customerName = customerName,
        notes = notes,
        estimatedDeliveryTime = estimatedDeliveryTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
}

/**
 * Преобразование OrderItem в OrderItemEntity
 */
fun OrderItem.toOrderItemEntity(orderId: Long): OrderItemEntity {
    return OrderItemEntity(
        orderId = orderId,
        productId = productId,
        productName = productName,
        productPrice = productPrice,
        productImageUrl = "", // Заполним позже из CartItem
        quantity = quantity,
        selectedOptions = emptyMap() // Заполним позже из CartItem
    )
}

/**
 * Преобразование списка OrderEntity в список Order
 */
fun List<OrderEntity>.toDomain(): List<Order> {
    return map { it.toDomain() }
}

// API DTO Mappers

/**
 * Преобразование OrderDto в Order (Domain)
 */
fun OrderDto.toDomain(): Order {
    return Order(
        id = this.id,
        userId = this.userId ?: 0L,
        items = this.items?.map { it.toDomain() } ?: emptyList(),
        status = parseOrderStatus(this.status),
        totalAmount = this.totalAmount,
        deliveryMethod = DeliveryMethod.DELIVERY, // Backend не возвращает метод доставки
        deliveryAddress = this.deliveryAddress,
        deliveryCost = this.deliveryFee,
        paymentMethod = PaymentMethod.CASH, // Backend не возвращает метод оплаты
        customerPhone = this.contactPhone,
        customerName = this.contactName,
        notes = this.comment ?: "",
        createdAt = parseDateTime(this.createdAt),
        updatedAt = parseDateTime(this.updatedAt ?: this.createdAt),
        estimatedDeliveryTime = this.estimatedDeliveryTime?.let { parseDateTime(it) }
    )
}

/**
 * Преобразование OrderItemDto в OrderItem (Domain)
 */
fun OrderItemDto.toDomain(): OrderItem {
    return OrderItem(
        id = this.id,
        productId = this.productId,
        productName = this.productName,
        productPrice = this.price,
        quantity = this.quantity
    )
}

/**
 * Преобразование AdminOrdersPageResponse в список Order (Domain)
 */
fun AdminOrdersPageResponse.toDomain(): List<Order> {
    return this.content.map { it.toDomain() }
}

/**
 * Создание CreateOrderRequest из параметров
 */
fun createOrderRequest(
    deliveryAddress: String,
    contactName: String,
    contactPhone: String,
    comment: String? = null
): CreateOrderRequest {
    return CreateOrderRequest(
        deliveryAddress = deliveryAddress,
        contactName = contactName,
        contactPhone = contactPhone,
        comment = comment
    )
}

/**
 * Парсинг даты из строки Backend API
 */
private fun parseDateTime(dateString: String): LocalDateTime {
    return try {
        // Backend возвращает ISO 8601 формат: "2025-06-18T08:19:18.373688"
        LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    } catch (e: Exception) {
        // Fallback на текущее время
        LocalDateTime.now()
    }
}

/**
 * Парсинг статуса заказа из строки Backend API
 */
private fun parseOrderStatus(statusString: String): OrderStatus {
    return try {
        when (statusString.uppercase()) {
            "CREATED" -> OrderStatus.PENDING
            "CONFIRMED" -> OrderStatus.CONFIRMED
            "PREPARING" -> OrderStatus.PREPARING
            "READY" -> OrderStatus.READY
            "DELIVERING" -> OrderStatus.DELIVERING
            "DELIVERED" -> OrderStatus.DELIVERED
            "CANCELLED" -> OrderStatus.CANCELLED
            else -> OrderStatus.PENDING
        }
    } catch (e: Exception) {
        OrderStatus.PENDING
    }
} 