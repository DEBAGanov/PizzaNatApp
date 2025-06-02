/**
 * @file: AdminDto.kt
 * @description: DTO классы для администраторских функций
 * @dependencies: Gson
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Запрос для создания продукта
 */
data class CreateProductRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("categoryId")
    val categoryId: Long,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("available")
    val available: Boolean = true
)

/**
 * Запрос для обновления продукта
 */
data class UpdateProductRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("categoryId")
    val categoryId: Long,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("available")
    val available: Boolean
)

/**
 * Запрос для создания категории
 */
data class CreateCategoryRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("imageUrl")
    val imageUrl: String?
)

/**
 * Запрос для обновления категории
 */
data class UpdateCategoryRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("imageUrl")
    val imageUrl: String?
)

/**
 * DTO для статистики админа
 */
data class AdminStatsDto(
    @SerializedName("totalOrders")
    val totalOrders: Int,
    @SerializedName("totalRevenue")
    val totalRevenue: Double,
    @SerializedName("totalProducts")
    val totalProducts: Int,
    @SerializedName("totalCategories")
    val totalCategories: Int,
    @SerializedName("ordersToday")
    val ordersToday: Int,
    @SerializedName("revenueToday")
    val revenueToday: Double,
    @SerializedName("popularProducts")
    val popularProducts: List<PopularProductDto>,
    @SerializedName("orderStatusStats")
    val orderStatusStats: Map<String, Int>
)

/**
 * DTO для популярного продукта в статистике
 */
data class PopularProductDto(
    @SerializedName("productId")
    val productId: Long,
    @SerializedName("productName")
    val productName: String,
    @SerializedName("ordersCount")
    val ordersCount: Int,
    @SerializedName("revenue")
    val revenue: Double
) 