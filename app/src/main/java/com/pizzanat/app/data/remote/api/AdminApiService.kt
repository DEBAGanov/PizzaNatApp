/**
 * @file: AdminApiService.kt
 * @description: API интерфейс для административных функций
 * @dependencies: Retrofit
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.remote.api

import com.pizzanat.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AdminApiService {
    
    /**
     * Создать новый продукт (только для админа)
     * POST /api/v1/admin/products
     */
    @POST("admin/products")
    suspend fun createProduct(
        @Body request: CreateProductRequest
    ): Response<ProductDto>
    
    /**
     * Обновить продукт (только для админа)
     * PUT /api/v1/admin/products/{id}
     */
    @PUT("admin/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Long,
        @Body request: UpdateProductRequest
    ): Response<ProductDto>
    
    /**
     * Удалить продукт (только для админа)
     * DELETE /api/v1/admin/products/{id}
     */
    @DELETE("admin/products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: Long
    ): Response<Void>
    
    /**
     * Создать новую категорию (только для админа)
     * POST /api/v1/admin/categories
     */
    @POST("admin/categories")
    suspend fun createCategory(
        @Body request: CreateCategoryRequest
    ): Response<CategoryDto>
    
    /**
     * Обновить категорию (только для админа)
     * PUT /api/v1/admin/categories/{id}
     */
    @PUT("admin/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Long,
        @Body request: UpdateCategoryRequest
    ): Response<CategoryDto>
    
    /**
     * Удалить категорию (только для админа)
     * DELETE /api/v1/admin/categories/{id}
     */
    @DELETE("admin/categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: Long
    ): Response<Void>
    
    /**
     * Получить статистику (только для админа)
     * GET /api/v1/admin/stats
     */
    @GET("admin/stats")
    suspend fun getAdminStats(): Response<AdminStatsDto>
} 