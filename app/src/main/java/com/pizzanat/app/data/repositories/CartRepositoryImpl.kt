/**
 * @file: CartRepositoryImpl.kt
 * @description: Реализация репозитория корзины с Room database
 * @dependencies: CartRepository, CartDao, CartMappers
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.repositories

import com.pizzanat.app.data.local.dao.CartDao
import com.pizzanat.app.data.local.entities.CartItemEntity
import com.pizzanat.app.data.mappers.toDomain
import com.pizzanat.app.data.mappers.toEntity
import com.pizzanat.app.domain.entities.CartItem
import com.pizzanat.app.domain.repositories.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao
) : CartRepository {

    override fun getCartItemsFlow(): Flow<List<CartItem>> {
        return cartDao.getAllCartItemsFlow().map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getCartItems(): Result<List<CartItem>> {
        return try {
            val entities = cartDao.getAllCartItems()
            Result.success(entities.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка получения корзины: ${e.message}"))
        }
    }

    override suspend fun addToCart(
        productId: Long, 
        productName: String, 
        productPrice: Double, 
        productImageUrl: String, 
        quantity: Int
    ): Result<Unit> {
        return try {
            // Проверяем, есть ли уже этот товар в корзине
            val existingItem = cartDao.getCartItemByProductId(productId)
            
            if (existingItem != null) {
                // Если товар уже есть, обновляем количество
                val newQuantity = existingItem.quantity + quantity
                cartDao.updateCartItemQuantity(existingItem.id, newQuantity)
            } else {
                // Если товара нет, добавляем новый
                val cartItemEntity = CartItemEntity(
                    productId = productId,
                    productName = productName,
                    productPrice = productPrice,
                    productImageUrl = productImageUrl,
                    quantity = quantity
                )
                cartDao.insertCartItem(cartItemEntity)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка добавления в корзину: ${e.message}"))
        }
    }

    override suspend fun updateCartItemQuantity(cartItemId: Long, quantity: Int): Result<Unit> {
        return try {
            if (quantity <= 0) {
                cartDao.deleteCartItem(cartItemId)
            } else {
                cartDao.updateCartItemQuantity(cartItemId, quantity)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка обновления корзины: ${e.message}"))
        }
    }

    override suspend fun removeFromCart(cartItemId: Long): Result<Unit> {
        return try {
            cartDao.deleteCartItem(cartItemId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка удаления из корзины: ${e.message}"))
        }
    }

    override suspend fun clearCart(): Result<Unit> {
        return try {
            cartDao.clearCart()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка очистки корзины: ${e.message}"))
        }
    }

    override suspend fun getCartItemsCount(): Result<Int> {
        return try {
            val count = cartDao.getCartItemsCount()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка подсчета товаров: ${e.message}"))
        }
    }

    override suspend fun getCartTotal(): Result<Double> {
        return try {
            val total = cartDao.getCartTotal() ?: 0.0
            Result.success(total)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка расчета суммы: ${e.message}"))
        }
    }

    override suspend fun isProductInCart(productId: Long): Result<Boolean> {
        return try {
            val exists = cartDao.isProductInCart(productId)
            Result.success(exists)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка проверки товара: ${e.message}"))
        }
    }
} 