/**
 * @file: UpdateCartItemUseCase.kt
 * @description: Use Case для обновления элементов корзины
 * @dependencies: CartRepository
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.cart

import com.pizzanat.app.domain.repositories.CartRepository
import javax.inject.Inject

class UpdateCartItemUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend fun updateQuantity(cartItemId: Long, quantity: Int): Result<Unit> {
        return try {
            if (quantity <= 0) {
                return Result.failure(Exception("Количество должно быть больше 0"))
            }
            
            cartRepository.updateCartItemQuantity(cartItemId, quantity)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка при обновлении корзины: ${e.message}"))
        }
    }
    
    suspend fun removeItem(cartItemId: Long): Result<Unit> {
        return try {
            cartRepository.removeFromCart(cartItemId)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка при удалении из корзины: ${e.message}"))
        }
    }
    
    suspend fun clearCart(): Result<Unit> {
        return try {
            cartRepository.clearCart()
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка при очистке корзины: ${e.message}"))
        }
    }
} 