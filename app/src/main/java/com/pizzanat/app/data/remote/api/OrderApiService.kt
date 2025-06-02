/**
 * @file: OrderApiService.kt
 * @description: API интерфейс для работы с заказами
 * @dependencies: Retrofit
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.remote.api

import com.pizzanat.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface OrderApiService {
    
    /**
     * Создать новый заказ
     * POST /api/v1/orders
     */
    @POST("orders")
    suspend fun createOrder(
        @Body request: CreateOrderRequest
    ): Response<CreateOrderResponse>
    
    /**
     * Получить заказы текущего пользователя
     * GET /api/v1/orders
     */
    @GET("orders")
    suspend fun getUserOrders(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<OrdersResponse>
    
    /**
     * Получить заказ по ID
     * GET /api/v1/orders/{id}
     */
    @GET("orders/{id}")
    suspend fun getOrderById(
        @Path("id") id: Long
    ): Response<OrderDto>
    
    /**
     * Обновить статус заказа (только для админа)
     * PUT /api/v1/admin/orders/{orderId}/status
     */
    @PUT("admin/orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: Long,
        @Body request: UpdateOrderStatusRequest
    ): Response<OrderDto>
    
    /**
     * Отменить заказ
     * DELETE /api/v1/orders/{id}
     */
    @DELETE("orders/{id}")
    suspend fun cancelOrder(
        @Path("id") id: Long
    ): Response<Void>
    
    /**
     * Получить все заказы (только для админа)
     * GET /api/v1/admin/orders
     * Возвращает Spring Boot Page структуру
     */
    @GET("admin/orders")
    suspend fun getAllOrders(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<AdminOrdersPageResponse>
    
    /**
     * Получить статистику заказов (только для админа)
     */
    @GET("orders/stats")
    suspend fun getOrdersStats(): Response<OrderStatsDto>
} 