/**
 * @file: ProductApiService.kt
 * @description: API интерфейс для работы с продуктами и категориями
 * @dependencies: Retrofit, Category/Product DTOs
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.network.api

import com.pizzanat.app.data.network.dto.CategoryDto
import com.pizzanat.app.data.network.dto.ProductDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApiService {
    
    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryDto>>
    
    @GET("products/category/{categoryId}")
    suspend fun getProductsByCategory(
        @Path("categoryId") categoryId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<List<ProductDto>>
    
    @GET("products/{id}")
    suspend fun getProductById(
        @Path("id") id: Long
    ): Response<ProductDto>
    
    @GET("products/search")
    suspend fun searchProducts(
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<List<ProductDto>>
    
    @GET("products/special-offers")
    suspend fun getSpecialOffers(): Response<List<ProductDto>>
} 