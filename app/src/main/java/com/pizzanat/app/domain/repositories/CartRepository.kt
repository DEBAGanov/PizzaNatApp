/**
 * @file: CartRepository.kt
 * @description: Интерфейс репозитория для управления корзиной
 * @dependencies: CartItem entity
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.repositories

import com.pizzanat.app.domain.entities.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {

    /**
     * Получить все элементы корзины как Flow для реактивного обновления UI
     */
    fun getCartItemsFlow(): Flow<List<CartItem>>

    /**
     * Получить все элементы корзины
     */
    suspend fun getCartItems(): Result<List<CartItem>>

    /**
     * Добавить товар в корзину
     */
    suspend fun addToCart(productId: Long, productName: String, productPrice: Double, productImageUrl: String, quantity: Int): Result<Unit>

    /**
     * Обновить количество товара в корзине
     */
    suspend fun updateCartItemQuantity(cartItemId: Long, quantity: Int): Result<Unit>

    /**
     * Удалить элемент из корзины
     */
    suspend fun removeFromCart(cartItemId: Long): Result<Unit>

    /**
     * Очистить всю корзину
     */
    suspend fun clearCart(): Result<Unit>

    /**
     * Получить общее количество товаров в корзине
     */
    suspend fun getCartItemsCount(): Result<Int>

    /**
     * Получить общую сумму корзины
     */
    suspend fun getCartTotal(): Result<Double>

    /**
     * Проверить, есть ли товар в корзине
     */
    suspend fun isProductInCart(productId: Long): Result<Boolean>
}