/**
 * @file: CartRepositoryImpl.kt
 * @description: Реализация репозитория корзины с API интеграцией и локальным fallback
 * @dependencies: CartRepository, CartDao, CartApiService, CartMappers
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.repositories

import android.util.Log
import com.pizzanat.app.data.local.dao.CartDao
import com.pizzanat.app.data.local.entities.CartItemEntity
import com.pizzanat.app.data.mappers.toDomain
import com.pizzanat.app.data.mappers.toEntity
import com.pizzanat.app.data.mappers.toCartItems
import com.pizzanat.app.data.mappers.toAddToCartRequest
import com.pizzanat.app.data.mappers.createUpdateCartItemRequest
import com.pizzanat.app.data.remote.api.CartApiService
import com.pizzanat.app.data.remote.util.safeApiCall
import com.pizzanat.app.domain.entities.CartItem
import com.pizzanat.app.domain.repositories.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao,
    private val cartApiService: CartApiService
) : CartRepository {

    override fun getCartItemsFlow(): Flow<List<CartItem>> {
        // Используем локальные данные для реактивного обновления UI
        return cartDao.getAllCartItemsFlow().map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getCartItems(): Result<List<CartItem>> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Пробуем загрузить с API для актуальных данных
            val apiResult = safeApiCall { cartApiService.getCart() }
            
            if (apiResult.isSuccess) {
                val cartDto = apiResult.getOrNull()
                if (cartDto != null) {
                    val cartItems = cartDto.toCartItems()
                    
                    // Синхронизируем с локальной базой
                    syncCartWithLocal(cartItems)
                    
                    Log.d("CartRepository", "Корзина загружена с API: ${cartItems.size} товаров")
                    Result.success(cartItems)
                } else {
                    // Fallback к локальным данным
                    getLocalCartItems()
                }
            } else {
                Log.w("CartRepository", "Ошибка API: ${apiResult.getErrorMessage()}, используем локальные данные")
                // При ошибке API используем локальные данные
                getLocalCartItems()
            }
        } catch (e: Exception) {
            Log.e("CartRepository", "Исключение при загрузке корзины: ${e.message}")
            // В случае ошибки возвращаем локальные данные
            getLocalCartItems()
        }
    }

    override suspend fun addToCart(
        productId: Long, 
        productName: String, 
        productPrice: Double, 
        productImageUrl: String, 
        quantity: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Пробуем добавить через API
            val request = com.pizzanat.app.data.remote.dto.AddToCartRequest(productId, quantity)
            val apiResult = safeApiCall { cartApiService.addToCart(request) }
            
            if (apiResult.isSuccess) {
                // Также добавляем в локальную базу для offline доступа
                addToLocalCart(productId, productName, productPrice, productImageUrl, quantity)
                
                Log.d("CartRepository", "Товар добавлен в корзину через API: product=$productId, quantity=$quantity")
                Result.success(Unit)
            } else {
                Log.w("CartRepository", "Ошибка API при добавлении, используем локальное хранение")
                // Fallback к локальному добавлению
                return@withContext addToLocalCart(productId, productName, productPrice, productImageUrl, quantity)
            }
        } catch (e: Exception) {
            Log.e("CartRepository", "Исключение при добавлении в корзину: ${e.message}")
            // Fallback к локальному добавлению
            return@withContext addToLocalCart(productId, productName, productPrice, productImageUrl, quantity)
        }
    }

    override suspend fun updateCartItemQuantity(cartItemId: Long, quantity: Int): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Получаем productId из локальной базы
            val localItem = cartDao.getCartItemByProductId(cartItemId)
            val productId = localItem?.productId ?: cartItemId
            
            if (quantity <= 0) {
                // Удаляем товар
                removeFromCart(cartItemId)
            } else {
                // Пробуем обновить через API
                val request = createUpdateCartItemRequest(quantity)
                val apiResult = safeApiCall { cartApiService.updateCartItem(productId, request) }
                
                if (apiResult.isSuccess) {
                    // Также обновляем локально
                    updateLocalCartItemQuantity(cartItemId, quantity)
                    
                    Log.d("CartRepository", "Количество обновлено через API: product=$productId, quantity=$quantity")
                    Result.success(Unit)
                } else {
                    Log.w("CartRepository", "Ошибка API при обновлении, используем локальное хранение")
                    // Fallback к локальному обновлению
                    return@withContext updateLocalCartItemQuantity(cartItemId, quantity)
                }
            }
        } catch (e: Exception) {
            Log.e("CartRepository", "Исключение при обновлении корзины: ${e.message}")
            // Fallback к локальному обновлению
            return@withContext updateLocalCartItemQuantity(cartItemId, quantity)
        }
    }

    override suspend fun removeFromCart(cartItemId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Получаем productId из локальной базы
            val localItem = cartDao.getCartItemByProductId(cartItemId)
            val productId = localItem?.productId ?: cartItemId
            
            // Пробуем удалить через API
            val apiResult = safeApiCall { cartApiService.removeFromCart(productId) }
            
            if (apiResult.isSuccess) {
                // Также удаляем локально
                removeFromLocalCart(cartItemId)
                
                Log.d("CartRepository", "Товар удален через API: product=$productId")
                Result.success(Unit)
            } else {
                Log.w("CartRepository", "Ошибка API при удалении, используем локальное хранение")
                // Fallback к локальному удалению
                return@withContext removeFromLocalCart(cartItemId)
            }
        } catch (e: Exception) {
            Log.e("CartRepository", "Исключение при удалении из корзины: ${e.message}")
            // Fallback к локальному удалению
            return@withContext removeFromLocalCart(cartItemId)
        }
    }

    override suspend fun clearCart(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Пробуем очистить через API
            val apiResult = safeApiCall { cartApiService.clearCart() }
            
            if (apiResult.isSuccess) {
                // Также очищаем локально
                clearLocalCart()
                
                Log.d("CartRepository", "Корзина очищена через API")
                Result.success(Unit)
            } else {
                Log.w("CartRepository", "Ошибка API при очистке, используем локальное хранение")
                // Fallback к локальной очистке
                return@withContext clearLocalCart()
            }
        } catch (e: Exception) {
            Log.e("CartRepository", "Исключение при очистке корзины: ${e.message}")
            // Fallback к локальной очистке
            return@withContext clearLocalCart()
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

    // ========== Private Helper Methods ==========

    private suspend fun getLocalCartItems(): Result<List<CartItem>> {
        return try {
            val entities = cartDao.getAllCartItems()
            Result.success(entities.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка получения локальной корзины: ${e.message}"))
        }
    }

    private suspend fun syncCartWithLocal(cartItems: List<CartItem>) {
        try {
            // Очищаем локальную корзину и добавляем актуальные данные с API
            cartDao.clearCart()
            val entities = cartItems.map { it.toEntity() }
            entities.forEach { cartDao.insertCartItem(it) }
        } catch (e: Exception) {
            Log.e("CartRepository", "Ошибка синхронизации с локальной базой: ${e.message}")
        }
    }

    private suspend fun addToLocalCart(
        productId: Long,
        productName: String,
        productPrice: Double,
        productImageUrl: String,
        quantity: Int
    ): Result<Unit> {
        return try {
            val existingItem = cartDao.getCartItemByProductId(productId)
            
            if (existingItem != null) {
                val newQuantity = existingItem.quantity + quantity
                cartDao.updateCartItemQuantity(existingItem.id, newQuantity)
            } else {
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
            Result.failure(Exception("Ошибка локального добавления: ${e.message}"))
        }
    }

    private suspend fun updateLocalCartItemQuantity(cartItemId: Long, quantity: Int): Result<Unit> {
        return try {
            if (quantity <= 0) {
                cartDao.deleteCartItem(cartItemId)
            } else {
                cartDao.updateCartItemQuantity(cartItemId, quantity)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка локального обновления: ${e.message}"))
        }
    }

    private suspend fun removeFromLocalCart(cartItemId: Long): Result<Unit> {
        return try {
            cartDao.deleteCartItem(cartItemId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка локального удаления: ${e.message}"))
        }
    }

    private suspend fun clearLocalCart(): Result<Unit> {
        return try {
            cartDao.clearCart()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка локальной очистки: ${e.message}"))
        }
    }
} 