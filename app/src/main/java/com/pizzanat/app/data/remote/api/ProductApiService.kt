/**
 * @file: ProductApiService.kt
 * @description: API интерфейс для работы с продуктами
 * @dependencies: Retrofit
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.remote.api

import com.pizzanat.app.data.remote.dto.CategoryDto
import com.pizzanat.app.data.remote.dto.ProductDto
import com.pizzanat.app.data.remote.dto.ProductsResponse
import retrofit2.Response
import retrofit2.http.*

interface ProductApiService {
    
    /**
     * Получить все категории продуктов
     * Сервер возвращает массив категорий напрямую
     */
    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryDto>>
    
    /**
     * Получить продукты по категории с пагинацией
     * Используем основной endpoint с фильтрацией
     */
    @GET("products")
    suspend fun getProductsByCategory(
        @Query("categoryId") categoryId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "id,asc"
    ): Response<ProductsResponse>
    
    /**
     * Получить все продукты с пагинацией
     */
    @GET("products")
    suspend fun getAllProducts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "id,asc"
    ): Response<ProductsResponse>
    
    /**
     * Получить продукт по ID
     */
    @GET("products/{id}")
    suspend fun getProductById(
        @Path("id") id: Long
    ): Response<ProductDto>
    
    /**
     * Поиск продуктов
     */
    @GET("products/search")
    suspend fun searchProducts(
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ProductsResponse>
    
    /**
     * Получить популярные продукты
     */
    @GET("products/popular")
    suspend fun getPopularProducts(
        @Query("limit") limit: Int = 10
    ): Response<List<ProductDto>>
    
    /**
     * Получить рекомендуемые продукты
     */
    @GET("products/recommended")
    suspend fun getRecommendedProducts(
        @Query("limit") limit: Int = 10
    ): Response<List<ProductDto>>
} 