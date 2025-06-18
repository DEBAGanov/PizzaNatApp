/**
 * @file: CartApiService.kt
 * @description: API интерфейс для работы с корзиной
 * @dependencies: Retrofit
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.remote.api

import com.pizzanat.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface CartApiService {
    
    /**
     * Получить корзину пользователя
     * GET /api/v1/cart
     */
    @GET("cart")
    suspend fun getCart(): Response<CartDto>
    
    /**
     * Добавить товар в корзину
     * POST /api/v1/cart/items
     */
    @POST("cart/items")
    suspend fun addToCart(
        @Body request: AddToCartRequest
    ): Response<CartDto>
    
    /**
     * Изменить количество товара в корзине
     * PUT /api/v1/cart/items/{itemId}
     */
    @PUT("cart/items/{itemId}")
    suspend fun updateCartItem(
        @Path("itemId") itemId: Long,
        @Body request: UpdateCartItemRequest
    ): Response<CartDto>
    
    /**
     * Удалить товар из корзины
     * DELETE /api/v1/cart/items/{itemId}
     */
    @DELETE("cart/items/{itemId}")
    suspend fun removeFromCart(
        @Path("itemId") itemId: Long
    ): Response<Void>
    
    /**
     * Очистить корзину
     * DELETE /api/v1/cart
     */
    @DELETE("cart")
    suspend fun clearCart(): Response<Void>
} 