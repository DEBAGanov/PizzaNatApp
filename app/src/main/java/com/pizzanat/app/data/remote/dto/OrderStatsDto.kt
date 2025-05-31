/**
 * @file: OrderStatsDto.kt
 * @description: DTO для статистики заказов
 * @dependencies: Gson
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OrderStatsDto(
    @SerializedName("totalOrders")
    val totalOrders: Int,
    @SerializedName("totalRevenue")
    val totalRevenue: Double,
    @SerializedName("avgOrderValue")
    val avgOrderValue: Double,
    @SerializedName("pendingOrders")
    val pendingOrders: Int,
    @SerializedName("completedOrders")
    val completedOrders: Int
) 