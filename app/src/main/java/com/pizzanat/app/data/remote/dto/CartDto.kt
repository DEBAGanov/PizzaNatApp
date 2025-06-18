/**
 * @file: CartDto.kt
 * @description: DTO классы для работы с корзиной API
 * @dependencies: Gson
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO для корзины пользователя
 */
data class CartDto(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("sessionId")
    val sessionId: String?,
    @SerializedName("items")
    val items: List<CartItemDto>,
    @SerializedName("totalAmount")
    val totalAmount: Double
)

/**
 * DTO для элемента корзины
 */
data class CartItemDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("productId")
    val productId: Long,
    @SerializedName("productName")
    val productName: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("discountedPrice")
    val discountedPrice: Double?,
    @SerializedName("productImageUrl")
    val productImageUrl: String?,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("subtotal")
    val subtotal: Double
)

/**
 * Запрос для добавления товара в корзину
 */
data class AddToCartRequest(
    @SerializedName("productId")
    val productId: Long,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("selectedOptions")
    val selectedOptions: Map<String, Any>? = null
)

/**
 * Запрос для обновления количества товара в корзине
 */
data class UpdateCartItemRequest(
    @SerializedName("quantity")
    val quantity: Int
) 