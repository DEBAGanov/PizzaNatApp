/**
 * @file: AddToCartUseCase.kt
 * @description: Use Case для добавления товара в корзину
 * @dependencies: CartRepository, Product entity
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.cart

import com.pizzanat.app.domain.entities.Product
import com.pizzanat.app.domain.repositories.CartRepository
import javax.inject.Inject

class AddToCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(product: Product, quantity: Int = 1): Result<Unit> {
        return try {
            if (!product.available) {
                return Result.failure(Exception("Товар недоступен для заказа"))
            }
            
            if (quantity <= 0) {
                return Result.failure(Exception("Количество должно быть больше 0"))
            }
            
            cartRepository.addToCart(
                productId = product.id,
                productName = product.name,
                productPrice = product.price,
                productImageUrl = product.imageUrl ?: "",
                quantity = quantity
            )
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка при добавлении в корзину: ${e.message}"))
        }
    }
} 