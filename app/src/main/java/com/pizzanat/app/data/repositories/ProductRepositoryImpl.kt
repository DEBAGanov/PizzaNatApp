/**
 * @file: ProductRepositoryImpl.kt
 * @description: Реализация репозитория продуктов с API интеграцией
 * @dependencies: ProductApiService, ApiResult, Mappers
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.repositories

import com.pizzanat.app.data.mappers.toDomain
import com.pizzanat.app.data.mappers.toCategoryDomain
import com.pizzanat.app.data.mappers.toProductDomain
import com.pizzanat.app.data.remote.api.ProductApiService
import com.pizzanat.app.data.remote.util.safeApiCall
import com.pizzanat.app.data.remote.util.toResult
import com.pizzanat.app.domain.entities.Category
import com.pizzanat.app.domain.entities.Product
import com.pizzanat.app.domain.repositories.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productApiService: ProductApiService
) : ProductRepository {
    
    override suspend fun getCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
        val apiResult = safeApiCall { productApiService.getCategories() }
        apiResult.toResult().map { categories ->
            categories?.toCategoryDomain() ?: emptyList()
        }
    }
    
    override suspend fun getProductsByCategory(
        categoryId: Long,
        page: Int,
        size: Int
    ): Result<List<Product>> = withContext(Dispatchers.IO) {
        val apiResult = safeApiCall { 
            productApiService.getProductsByCategory(categoryId, page, size) 
        }
        apiResult.toResult().map { response ->
            response?.content?.toProductDomain() ?: emptyList()
        }
    }
    
    override suspend fun getProductById(productId: Long): Result<Product> = withContext(Dispatchers.IO) {
        val apiResult = safeApiCall { productApiService.getProductById(productId) }
        apiResult.toResult().map { productDto ->
            productDto?.toDomain() ?: throw Exception("Продукт не найден")
        }
    }
    
    override suspend fun searchProducts(
        query: String,
        page: Int,
        size: Int
    ): Result<List<Product>> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            return@withContext Result.success(emptyList())
        }
        
        val apiResult = safeApiCall { 
            productApiService.searchProducts(query, page, size) 
        }
        apiResult.toResult().map { response ->
            response?.content?.toProductDomain() ?: emptyList()
        }
    }
    
    override suspend fun getSpecialOffers(): Result<List<Product>> = withContext(Dispatchers.IO) {
        val apiResult = safeApiCall { productApiService.getPopularProducts(10) }
        apiResult.toResult().map { products ->
            products?.toProductDomain() ?: emptyList()
        }
    }
    
    suspend fun getPopularProducts(limit: Int = 10): Result<List<Product>> = withContext(Dispatchers.IO) {
        val apiResult = safeApiCall { productApiService.getPopularProducts(limit) }
        apiResult.toResult().map { products ->
            products?.toProductDomain() ?: emptyList()
        }
    }
    
    suspend fun getRecommendedProducts(limit: Int = 10): Result<List<Product>> = withContext(Dispatchers.IO) {
        val apiResult = safeApiCall { productApiService.getRecommendedProducts(limit) }
        apiResult.toResult().map { products ->
            products?.toProductDomain() ?: emptyList()
        }
    }
    
    // Пока заглушки для избранных продуктов (будет реализовано в будущем с Room)
    override fun getFavoriteProducts(): Flow<List<Product>> {
        // TODO: Implement with Room database
        return flowOf(emptyList())
    }
    
    override suspend fun addToFavorites(productId: Long) {
        // TODO: Implement with Room database
    }
    
    override suspend fun removeFromFavorites(productId: Long) {
        // TODO: Implement with Room database
    }
} 