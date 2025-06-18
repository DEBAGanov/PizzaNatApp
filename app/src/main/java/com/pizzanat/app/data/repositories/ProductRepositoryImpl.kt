/**
 * @file: ProductRepositoryImpl.kt
 * @description: Реализация репозитория продуктов с API интеграцией
 * @dependencies: ProductApiService, ApiResult, Mappers
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.repositories

import android.util.Log
import com.pizzanat.app.data.mappers.toDomain
import com.pizzanat.app.data.mappers.toCategoryDomain
import com.pizzanat.app.data.mappers.toProductDomain
import com.pizzanat.app.data.mappers.toProductsDomain
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
        Log.d("ProductRepository", "🌐 Запрос категорий")
        val apiResult = safeApiCall { 
            productApiService.getCategories() 
        }
        apiResult.toResult().map { categories ->
            categories?.toCategoryDomain() ?: emptyList()
        }.also { result ->
            if (result.isSuccess) {
                Log.d("ProductRepository", "✅ Категории загружены: ${result.getOrNull()?.size} категорий")
                result.getOrNull()?.forEach { category ->
                    Log.d("ProductRepository", "📂 Категория: ${category.name} (ID: ${category.id})")
                }
            } else {
                Log.w("ProductRepository", "❌ Ошибка загрузки категорий: ${result.exceptionOrNull()?.message}")
            }
        }
    }
    
    override suspend fun getProductsByCategory(
        categoryId: Long,
        page: Int,
        size: Int
    ): Result<List<Product>> = withContext(Dispatchers.IO) {
        Log.d("ProductRepository", "🌐 Запрос продуктов категории $categoryId, страница $page, размер $size")
        Log.d("ProductRepository", "🌐 URL: products/category/$categoryId?page=$page&size=$size")
        
        val apiResult = safeApiCall { 
            productApiService.getProductsByCategory(categoryId, page, size) 
        }
        apiResult.toResult().map { pageResponse ->
            // Используем новый маппер для пагинированного ответа
            val products = pageResponse?.toProductsDomain() ?: emptyList()
            Log.d("ProductRepository", "📦 Получено продуктов: ${products.size}")
            products.forEach { product ->
                Log.d("ProductRepository", "🍕 Продукт: ${product.name} (ID: ${product.id}, Цена: ${product.price}₽)")
            }
            products
        }.also { result ->
            if (result.isSuccess) {
                Log.d("ProductRepository", "✅ Продукты категории $categoryId загружены: ${result.getOrNull()?.size} товаров")
            } else {
                Log.w("ProductRepository", "❌ Ошибка загрузки продуктов категории $categoryId: ${result.exceptionOrNull()?.message}")
                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }
    
    override suspend fun getProductById(productId: Long): Result<Product> = withContext(Dispatchers.IO) {
        val apiResult = safeApiCall { 
            productApiService.getProductById(productId) 
        }
        apiResult.toResult().map { productDto ->
            productDto?.toDomain() ?: throw Exception("Продукт не найден")
        }.also { result ->
            if (result.isSuccess) {
                Log.d("ProductRepository", "Продукт с ID $productId загружен: ${result.getOrNull()?.name}")
            } else {
                Log.w("ProductRepository", "Ошибка загрузки продукта с ID $productId: ${result.exceptionOrNull()?.message}")
            }
        }
    }
    
    override suspend fun searchProducts(
        query: String,
        page: Int,
        size: Int
    ): Result<List<Product>> = withContext(Dispatchers.IO) {
        val apiResult = safeApiCall { 
            productApiService.searchProducts(query, page, size) 
        }
        apiResult.toResult().map { pageResponse ->
            // Используем новый маппер для пагинированного ответа
            pageResponse?.toProductsDomain() ?: emptyList()
        }.also { result ->
            if (result.isSuccess) {
                Log.d("ProductRepository", "Поиск по запросу '$query' завершен: ${result.getOrNull()?.size} товаров")
            } else {
                Log.w("ProductRepository", "Ошибка поиска по запросу '$query': ${result.exceptionOrNull()?.message}")
            }
        }
    }
    
    override suspend fun getSpecialOffers(): Result<List<Product>> = withContext(Dispatchers.IO) {
        // Пока возвращаем пустой список, т.к. API не готов
        Result.success(emptyList())
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
        // Пока возвращаем пустой поток, т.к. функционал не реализован
        return flowOf(emptyList())
    }
    
    override suspend fun addToFavorites(productId: Long) {
        // Пока ничего не делаем, т.к. функционал не реализован
        Log.d("ProductRepository", "Добавление в избранное пока не реализовано: $productId")
    }
    
    override suspend fun removeFromFavorites(productId: Long) {
        // Пока ничего не делаем, т.к. функционал не реализован
        Log.d("ProductRepository", "Удаление из избранного пока не реализовано: $productId")
    }
} 