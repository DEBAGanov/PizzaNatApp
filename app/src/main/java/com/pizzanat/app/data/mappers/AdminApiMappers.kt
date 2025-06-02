/**
 * @file: AdminApiMappers.kt
 * @description: Мапперы для преобразования между Admin API DTO и domain entities
 * @dependencies: AdminDto, AdminStats, Product, Category
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.mappers

import com.pizzanat.app.data.remote.dto.*
import com.pizzanat.app.domain.entities.AdminStats
import com.pizzanat.app.domain.entities.Category
import com.pizzanat.app.domain.entities.PopularProduct
import com.pizzanat.app.domain.entities.Product
import java.time.LocalDateTime

/**
 * Преобразование AdminStatsDto в AdminStats (domain)
 */
fun AdminStatsDto.toDomain(): AdminStats {
    return AdminStats(
        totalOrders = totalOrders,
        todayOrders = ordersToday,
        pendingOrders = orderStatusStats["PENDING"] ?: 0,
        completedOrders = orderStatusStats["COMPLETED"] ?: 0,
        totalRevenue = totalRevenue,
        todayRevenue = revenueToday,
        totalProducts = totalProducts,
        totalCategories = totalCategories,
        totalUsers = 0, // Не возвращается в микросервисе, ставим 0
        popularProducts = popularProducts.map { it.toDomain() },
        recentOrders = emptyList(), // Заполняется отдельно
        generatedAt = LocalDateTime.now()
    )
}

/**
 * Преобразование PopularProductDto в PopularProduct (domain)
 */
fun PopularProductDto.toDomain(): PopularProduct {
    return PopularProduct(
        productId = productId,
        productName = productName,
        orderCount = ordersCount,
        totalRevenue = revenue
    )
}

/**
 * Преобразование Product в CreateProductRequest
 */
fun Product.toCreateRequest(): CreateProductRequest {
    return CreateProductRequest(
        name = name,
        description = description,
        price = price,
        categoryId = categoryId,
        imageUrl = imageUrl,
        available = available
    )
}

/**
 * Преобразование Product в UpdateProductRequest
 */
fun Product.toUpdateRequest(): UpdateProductRequest {
    return UpdateProductRequest(
        name = name,
        description = description,
        price = price,
        categoryId = categoryId,
        imageUrl = imageUrl,
        available = available
    )
}

/**
 * Преобразование Category в CreateCategoryRequest
 */
fun Category.toCreateRequest(): CreateCategoryRequest {
    return CreateCategoryRequest(
        name = name,
        description = description,
        imageUrl = imageUrl
    )
}

/**
 * Преобразование Category в UpdateCategoryRequest
 */
fun Category.toUpdateRequest(): UpdateCategoryRequest {
    return UpdateCategoryRequest(
        name = name,
        description = description,
        imageUrl = imageUrl
    )
} 