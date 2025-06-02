/**
 * @file: CartApiMappers.kt
 * @description: Мапперы для преобразования между Cart API DTO и domain entities
 * @dependencies: CartDto, CartItem
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.mappers

import com.pizzanat.app.data.remote.dto.AddToCartRequest
import com.pizzanat.app.data.remote.dto.CartDto
import com.pizzanat.app.data.remote.dto.CartItemDto
import com.pizzanat.app.data.remote.dto.UpdateCartItemRequest
import com.pizzanat.app.domain.entities.CartItem
import com.pizzanat.app.domain.entities.Product

/**
 * Преобразование CartDto в список CartItem
 */
fun CartDto.toCartItems(): List<CartItem> {
    return items.map { it.toDomain() }
}

/**
 * Преобразование CartItemDto в CartItem (domain)
 */
fun CartItemDto.toDomain(): CartItem {
    return CartItem(
        id = id,
        productId = productId,
        productName = productName,
        productPrice = productPrice,
        productImageUrl = productImageUrl ?: "",
        quantity = quantity,
        selectedOptions = selectedOptions ?: emptyMap()
    )
}

/**
 * Преобразование Product в AddToCartRequest
 */
fun Product.toAddToCartRequest(quantity: Int = 1): AddToCartRequest {
    return AddToCartRequest(
        productId = id,
        quantity = quantity
    )
}

/**
 * Создание UpdateCartItemRequest
 */
fun createUpdateCartItemRequest(quantity: Int): UpdateCartItemRequest {
    return UpdateCartItemRequest(quantity = quantity)
} 