/**
 * @file: OrderDao.kt
 * @description: DAO для работы с заказами в Room database
 * @dependencies: Room, OrderEntity, OrderItemEntity
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.local.dao

import androidx.room.*
import com.pizzanat.app.data.local.entities.OrderEntity
import com.pizzanat.app.data.local.entities.OrderItemEntity
import com.pizzanat.app.domain.entities.OrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserOrdersFlow(userId: Long): Flow<List<OrderEntity>>
    
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getUserOrders(userId: Long): List<OrderEntity>
    
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Long): OrderEntity?
    
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: Long): List<OrderItemEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(orderItems: List<OrderItemEntity>)
    
    @Update
    suspend fun updateOrder(order: OrderEntity)
    
    @Query("UPDATE orders SET status = :status, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus, updatedAt: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrder(orderId: Long)
    
    @Query("SELECT COUNT(*) FROM orders WHERE userId = :userId")
    suspend fun getUserOrdersCount(userId: Long): Int
    
    @Query("SELECT * FROM orders WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getOrdersByStatus(status: OrderStatus): List<OrderEntity>
    
    @Transaction
    suspend fun insertOrderWithItems(order: OrderEntity, orderItems: List<OrderItemEntity>) {
        val orderId = insertOrder(order)
        val itemsWithOrderId = orderItems.map { it.copy(orderId = orderId) }
        insertOrderItems(itemsWithOrderId)
    }
} 