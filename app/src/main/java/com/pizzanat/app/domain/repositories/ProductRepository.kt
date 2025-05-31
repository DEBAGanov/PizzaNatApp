/**
 * @file: ProductRepository.kt
 * @description: Интерфейс репозитория для продуктов и категорий
 * @dependencies: Domain entities
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.repositories

import com.pizzanat.app.domain.entities.Category
import com.pizzanat.app.domain.entities.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    
    /**
     * Получение всех категорий
     */
    suspend fun getCategories(): Result<List<Category>>
    
    /**
     * Получение продуктов по категории с пагинацией
     */
    suspend fun getProductsByCategory(
        categoryId: Long,
        page: Int = 0,
        size: Int = 10
    ): Result<List<Product>>
    
    /**
     * Получение продукта по ID
     */
    suspend fun getProductById(productId: Long): Result<Product>
    
    /**
     * Поиск продуктов
     */
    suspend fun searchProducts(
        query: String,
        page: Int = 0,
        size: Int = 10
    ): Result<List<Product>>
    
    /**
     * Получение специальных предложений
     */
    suspend fun getSpecialOffers(): Result<List<Product>>
    
    /**
     * Flow для отслеживания избранных продуктов
     */
    fun getFavoriteProducts(): Flow<List<Product>>
    
    /**
     * Добавление в избранное
     */
    suspend fun addToFavorites(productId: Long)
    
    /**
     * Удаление из избранного
     */
    suspend fun removeFromFavorites(productId: Long)
} 