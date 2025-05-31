/**
 * @file: AdminStats.kt
 * @description: Доменная сущность для статистики администратора
 * @dependencies: Order, Product entities
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.entities

import java.time.LocalDateTime

data class AdminStats(
    val totalOrders: Int,
    val todayOrders: Int,
    val pendingOrders: Int,
    val completedOrders: Int,
    val totalRevenue: Double,
    val todayRevenue: Double,
    val totalProducts: Int,
    val totalCategories: Int,
    val totalUsers: Int,
    val popularProducts: List<PopularProduct>,
    val recentOrders: List<Order>,
    val generatedAt: LocalDateTime
)

data class PopularProduct(
    val productId: Long,
    val productName: String,
    val orderCount: Int,
    val totalRevenue: Double
)

data class DashboardCard(
    val title: String,
    val value: String,
    val subtitle: String? = null,
    val trend: TrendType = TrendType.NEUTRAL,
    val trendValue: String? = null
)

enum class TrendType {
    UP, DOWN, NEUTRAL
} 