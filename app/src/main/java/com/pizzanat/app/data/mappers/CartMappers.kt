/**
 * @file: CartMappers.kt
 * @description: Маппер функции для преобразования между Entity и Domain объектами корзины
 * @dependencies: CartItem, CartItemEntity, OrderItemEntity
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.mappers

import com.pizzanat.app.data.local.entities.CartItemEntity
import com.pizzanat.app.data.local.entities.OrderItemEntity
import com.pizzanat.app.data.remote.dto.CartDto
import com.pizzanat.app.data.remote.dto.CartItemDto
import com.pizzanat.app.domain.entities.CartItem
import com.pizzanat.app.domain.entities.Product

/**
 * Преобразование CartItemEntity в CartItem (Domain)
 */
fun CartItemEntity.toDomain(): CartItem {
    return CartItem(
        id = id,
        productId = productId,
        productName = productName,
        productPrice = productPrice,
        productImageUrl = productImageUrl,
        quantity = quantity,
        selectedOptions = selectedOptions
    )
}

/**
 * Преобразование CartItem (Domain) в CartItemEntity
 */
fun CartItem.toEntity(): CartItemEntity {
    return CartItemEntity(
        id = id,
        productId = productId,
        productName = productName,
        productPrice = productPrice,
        productImageUrl = productImageUrl,
        quantity = quantity,
        selectedOptions = selectedOptions,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}

/**
 * Преобразование CartItem в OrderItemEntity для создания заказа
 */
fun CartItem.toOrderItemEntity(orderId: Long): OrderItemEntity {
    return OrderItemEntity(
        orderId = orderId,
        productId = productId,
        productName = productName,
        productPrice = productPrice,
        productImageUrl = productImageUrl,
        quantity = quantity,
        selectedOptions = selectedOptions
    )
}

/**
 * Преобразование списка CartItemEntity в список CartItem
 */
fun List<CartItemEntity>.toDomain(): List<CartItem> {
    return map { it.toDomain() }
}

/**
 * Преобразование CartItem в Product для навигации
 */
fun CartItem.toProduct(): Product {
    return Product(
        id = productId,
        name = productName,
        description = "", // Описание не хранится в корзине
        price = productPrice,
        imageUrl = productImageUrl,
        categoryId = 0L, // Категория не хранится в корзине
        available = true
    )
}

// API DTO Mappers

/**
 * Преобразование CartItemDto в CartItem (Domain)
 */
fun CartItemDto.toDomain(): CartItem {
    return CartItem(
        id = this.id,
        productId = this.productId,
        productName = this.productName,
        productPrice = this.discountedPrice ?: this.price,
        productImageUrl = this.productImageUrl ?: "",
        quantity = this.quantity,
        selectedOptions = emptyMap() // API не возвращает опции
    )
}

/**
 * Преобразование CartDto в список CartItem (Domain)
 */
fun CartDto.toDomain(): List<CartItem> {
    return this.items.map { it.toDomain() }
}

/**
 * Получение общей суммы корзины из CartDto
 */
fun CartDto.getTotalAmount(): Double {
    return this.totalAmount
} 