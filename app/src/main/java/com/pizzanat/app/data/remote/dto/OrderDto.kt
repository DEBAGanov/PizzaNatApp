/**
 * @file: OrderDto.kt
 * @description: DTO модели для API заказов
 * @dependencies: Gson
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OrderDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("status")
    val status: String,
    @SerializedName("totalAmount")
    val totalAmount: Double,
    @SerializedName("deliveryAddress")
    val deliveryAddress: String,
    @SerializedName("contactPhone")
    val contactPhone: String,
    @SerializedName("contactName")
    val contactName: String,
    @SerializedName("notes")
    val notes: String = "",
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("estimatedDeliveryTime")
    val estimatedDeliveryTime: String? = null,
    @SerializedName("deliveryFee")
    val deliveryFee: Double = 0.0,
    @SerializedName("items")
    val items: List<OrderItemDto> = emptyList()
)

data class OrderItemDto(
    @SerializedName("id")
    val id: Long = 0L,
    @SerializedName("productId")
    val productId: Long,
    @SerializedName("productName")
    val productName: String,
    @SerializedName("productPrice")
    val productPrice: Double,
    @SerializedName("productImageUrl")
    val productImageUrl: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("selectedOptions")
    val selectedOptions: Map<String, String> = emptyMap()
)

data class CreateOrderRequest(
    @SerializedName("items")
    val items: List<CreateOrderItemRequest>,
    @SerializedName("deliveryAddress")
    val deliveryAddress: String,
    @SerializedName("contactPhone")
    val contactPhone: String,
    @SerializedName("contactName")
    val contactName: String,
    @SerializedName("notes")
    val notes: String = ""
)

data class CreateOrderItemRequest(
    @SerializedName("productId")
    val productId: Long,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("selectedOptions")
    val selectedOptions: Map<String, String> = emptyMap()
)

data class CreateOrderResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("message")
    val message: String
)

data class UpdateOrderStatusRequest(
    @SerializedName("status")
    val status: String
)

data class OrdersResponse(
    @SerializedName("orders")
    val orders: List<OrderDto>,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("totalElements")
    val totalElements: Int
) 