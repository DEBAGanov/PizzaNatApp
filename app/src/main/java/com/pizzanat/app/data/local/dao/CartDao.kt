/**
 * @file: CartDao.kt
 * @description: DAO для работы с элементами корзины в Room database
 * @dependencies: Room, CartItemEntity
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.local.dao

import androidx.room.*
import com.pizzanat.app.data.local.entities.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    
    @Query("SELECT * FROM cart_items ORDER BY createdAt DESC")
    fun getAllCartItemsFlow(): Flow<List<CartItemEntity>>
    
    @Query("SELECT * FROM cart_items ORDER BY createdAt DESC")
    suspend fun getAllCartItems(): List<CartItemEntity>
    
    @Query("SELECT * FROM cart_items WHERE productId = :productId LIMIT 1")
    suspend fun getCartItemByProductId(productId: Long): CartItemEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity): Long
    
    @Update
    suspend fun updateCartItem(cartItem: CartItemEntity)
    
    @Query("UPDATE cart_items SET quantity = :quantity, updatedAt = :updatedAt WHERE id = :cartItemId")
    suspend fun updateCartItemQuantity(cartItemId: Long, quantity: Int, updatedAt: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM cart_items WHERE id = :cartItemId")
    suspend fun deleteCartItem(cartItemId: Long)
    
    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
    
    @Query("SELECT COUNT(*) FROM cart_items")
    suspend fun getCartItemsCount(): Int
    
    @Query("SELECT SUM(quantity * productPrice) FROM cart_items")
    suspend fun getCartTotal(): Double?
    
    @Query("SELECT EXISTS(SELECT 1 FROM cart_items WHERE productId = :productId)")
    suspend fun isProductInCart(productId: Long): Boolean
} 