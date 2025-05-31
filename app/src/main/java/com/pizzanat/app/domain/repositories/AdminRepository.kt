/**
 * @file: AdminRepository.kt
 * @description: Интерфейс репозитория для администрирования
 * @dependencies: AdminUser, AdminStats, Order, Product
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.repositories

import com.pizzanat.app.domain.entities.*
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    
    // Аутентификация админа
    suspend fun loginAdmin(username: String, password: String): Result<AdminUser>
    suspend fun getCurrentAdmin(): Result<AdminUser?>
    suspend fun logoutAdmin(): Result<Unit>
    
    // Статистика
    suspend fun getAdminStats(): Result<AdminStats>
    fun getAdminStatsFlow(): Flow<AdminStats>
    
    // Управление заказами
    suspend fun getAllOrders(page: Int = 0, size: Int = 20): Result<List<Order>>
    fun getAllOrdersFlow(): Flow<List<Order>>
    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus): Result<Order>
    suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>>
    
    // Управление продуктами
    suspend fun getAllProducts(): Result<List<Product>>
    suspend fun createProduct(product: Product): Result<Product>
    suspend fun updateProduct(product: Product): Result<Product>
    suspend fun deleteProduct(productId: Long): Result<Unit>
    
    // Управление категориями
    suspend fun getAllCategories(): Result<List<Category>>
    suspend fun createCategory(category: Category): Result<Category>
    suspend fun updateCategory(category: Category): Result<Category>
    suspend fun deleteCategory(categoryId: Long): Result<Unit>
    
    // Аналитика
    suspend fun getPopularProducts(limit: Int = 10): Result<List<PopularProduct>>
    suspend fun getRecentOrders(limit: Int = 10): Result<List<Order>>
    suspend fun getRevenueStats(days: Int = 30): Result<Double>
} 