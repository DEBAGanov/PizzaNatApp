/**
 * @file: CartItem.kt
 * @description: Сущность элемента корзины
 * @dependencies: Product entity
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.entities

data class CartItem(
    val id: Long = 0L,
    val productId: Long,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String,
    val quantity: Int,
    val selectedOptions: Map<String, String> = emptyMap() // Для будущих опций (размер, тесто и т.д.)
) {
    val totalPrice: Double
        get() = productPrice * quantity
    
    fun toProduct(): Product {
        return Product(
            id = productId,
            name = productName,
            description = "",
            price = productPrice,
            imageUrl = productImageUrl,
            categoryId = 0L,
            available = true
        )
    }
} 