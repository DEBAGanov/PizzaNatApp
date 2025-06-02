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
    val id: Long,
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("items")
    val items: List<CartItemDto>,
    @SerializedName("totalAmount")
    val totalAmount: Double,
    @SerializedName("itemsCount")
    val itemsCount: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
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
    @SerializedName("productPrice")
    val productPrice: Double,
    @SerializedName("productImageUrl")
    val productImageUrl: String?,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("totalPrice")
    val totalPrice: Double,
    @SerializedName("selectedOptions")
    val selectedOptions: Map<String, String>? = emptyMap()
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