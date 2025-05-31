/**
 * @file: OrderMappers.kt
 * @description: Маппер функции для преобразования между Entity и Domain объектами заказов
 * @dependencies: Order, OrderEntity, OrderItemEntity
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.mappers

import com.pizzanat.app.data.local.entities.OrderEntity
import com.pizzanat.app.data.local.entities.OrderItemEntity
import com.pizzanat.app.domain.entities.Order
import com.pizzanat.app.domain.entities.OrderItem
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

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