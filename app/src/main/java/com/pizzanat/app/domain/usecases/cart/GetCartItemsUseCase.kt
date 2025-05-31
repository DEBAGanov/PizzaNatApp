/**
 * @file: GetCartItemsUseCase.kt
 * @description: Use Case для получения элементов корзины
 * @dependencies: CartRepository, CartItem entity
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.cart

import com.pizzanat.app.domain.entities.CartItem
import com.pizzanat.app.domain.repositories.CartRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCartItemsUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    operator fun invoke(): Flow<List<CartItem>> {
        return cartRepository.getCartItemsFlow()
    }
    
    suspend fun getCartItems(): Result<List<CartItem>> {
        return try {
            cartRepository.getCartItems()
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка при получении корзины: ${e.message}"))
        }
    }
} 