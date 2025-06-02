/**
 * @file: ProductApiService.kt
 * @description: API интерфейс для работы с продуктами
 * @dependencies: Retrofit
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.remote.api

import com.pizzanat.app.data.remote.dto.CategoryDto
import com.pizzanat.app.data.remote.dto.ProductDto
import com.pizzanat.app.data.remote.dto.ProductsPageResponse
import com.pizzanat.app.data.remote.dto.ProductsResponse
import retrofit2.Response
import retrofit2.http.*

interface ProductApiService {
    
    /**
     * Получить все категории продуктов
     * GET /api/v1/categories
     */
    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryDto>>
    
    /**
     * Получить категорию по ID
     * GET /api/v1/categories/{id}
     */
    @GET("categories/{id}")
    suspend fun getCategoryById(
        @Path("id") id: Long
    ): Response<CategoryDto>
    
    /**
     * Получить все продукты с пагинацией
     * GET /api/v1/products
     */
    @GET("products")
    suspend fun getAllProducts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "id,asc"
    ): Response<ProductsPageResponse>
    
    /**
     * Получить продукты по категории с пагинацией
     * GET /api/v1/products/category/{categoryId}
     */
    @GET("products/category/{categoryId}")
    suspend fun getProductsByCategory(
        @Path("categoryId") categoryId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ProductsPageResponse>
    
    /**
     * Получить продукт по ID
     * GET /api/v1/products/{id}
     */
    @GET("products/{id}")
    suspend fun getProductById(
        @Path("id") id: Long
    ): Response<ProductDto>
    
    /**
     * Поиск продуктов
     * GET /api/v1/products/search?query=text
     */
    @GET("products/search")
    suspend fun searchProducts(
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ProductsPageResponse>
    
    /**
     * Получить специальные предложения
     * GET /api/v1/products/special-offers
     */
    @GET("products/special-offers")
    suspend fun getSpecialOffers(): Response<List<ProductDto>>
    
    /**
     * Получить популярные продукты (дополнительный endpoint)
     */
    @GET("products/popular")
    suspend fun getPopularProducts(
        @Query("limit") limit: Int = 10
    ): Response<List<ProductDto>>
    
    /**
     * Получить рекомендуемые продукты (дополнительный endpoint)
     */
    @GET("products/recommended")
    suspend fun getRecommendedProducts(
        @Query("limit") limit: Int = 10
    ): Response<List<ProductDto>>
} 