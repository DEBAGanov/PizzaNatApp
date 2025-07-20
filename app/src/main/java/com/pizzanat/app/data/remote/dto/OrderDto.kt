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
    val userId: Long? = null,
    @SerializedName("status")
    val status: String,
    @SerializedName("statusDescription")
    val statusDescription: String? = null,
    @SerializedName("totalAmount")
    val totalAmount: Double,
    @SerializedName("deliveryAddress")
    val deliveryAddress: String,
    @SerializedName("contactPhone")
    val contactPhone: String,
    @SerializedName("contactName")
    val contactName: String,
    @SerializedName("notes")
    val notes: String? = "",
    @SerializedName("comment")
    val comment: String? = null,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("estimatedDeliveryTime")
    val estimatedDeliveryTime: String? = null,
    @SerializedName("deliveryFee")
    val deliveryFee: Double = 0.0,
    @SerializedName("deliveryLocationId")
    val deliveryLocationId: Long? = null,
    @SerializedName("deliveryLocationName")
    val deliveryLocationName: String? = null,
    @SerializedName("deliveryLocationAddress")
    val deliveryLocationAddress: String? = null,
    @SerializedName("items")
    val items: List<OrderItemDto>? = null
)

data class OrderItemDto(
    @SerializedName("id")
    val id: Long = 0L,
    @SerializedName("productId")
    val productId: Long,
    @SerializedName("productName")
    val productName: String,
    @SerializedName("productPrice")
    val productPrice: Double? = null,
    @SerializedName("price")
    val price: Double,
    @SerializedName("productImageUrl")
    val productImageUrl: String? = null,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("subtotal")
    val subtotal: Double? = null,
    @SerializedName("selectedOptions")
    val selectedOptions: Map<String, String>? = null
)

data class CreateOrderRequest(
    @SerializedName("deliveryLocationId")
    val deliveryLocationId: Long? = null,
    @SerializedName("deliveryAddress")
    val deliveryAddress: String? = null,
    @SerializedName("contactPhone")
    val contactPhone: String,
    @SerializedName("contactName")
    val contactName: String,
    @SerializedName("comment")
    val comment: String? = null,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("paymentMethod")
    val paymentMethod: String = "CASH"
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
    @SerializedName("content")
    val orders: List<OrderDto>,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("number")
    val currentPage: Int,
    @SerializedName("totalElements")
    val totalElements: Int,
    @SerializedName("last")
    val last: Boolean = false,
    @SerializedName("first")
    val first: Boolean = false,
    @SerializedName("empty")
    val empty: Boolean = false
)

/**
 * DTO для админского API заказов (Spring Boot Page структура)
 */
data class AdminOrdersPageResponse(
    @SerializedName("content")
    val content: List<OrderDto>,
    @SerializedName("pageable")
    val pageable: PageableDto,
    @SerializedName("totalElements")
    val totalElements: Long,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("last")
    val last: Boolean,
    @SerializedName("size")
    val size: Int,
    @SerializedName("number")
    val number: Int,
    @SerializedName("numberOfElements")
    val numberOfElements: Int,
    @SerializedName("first")
    val first: Boolean,
    @SerializedName("empty")
    val empty: Boolean
) 