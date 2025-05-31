/**
 * @file: OrderRepository.kt
 * @description: Интерфейс репозитория для управления заказами
 * @dependencies: Order entity, OrderStatus, CartItem
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.repositories

import com.pizzanat.app.domain.entities.CartItem
import com.pizzanat.app.domain.entities.DeliveryMethod
import com.pizzanat.app.domain.entities.Order
import com.pizzanat.app.domain.entities.OrderStatus
import com.pizzanat.app.domain.entities.PaymentMethod
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    
    /**
     * Получить заказы пользователя в виде Flow для реактивного обновления
     */
    fun getUserOrdersFlow(userId: Long): Flow<List<Order>>
    
    /**
     * Получить заказы пользователя
     */
    suspend fun getUserOrders(userId: Long): Result<List<Order>>
    
    /**
     * Получить заказ по ID
     */
    suspend fun getOrderById(orderId: Long): Result<Order?>
    
    /**
     * Создать новый заказ из корзины
     */
    suspend fun createOrder(
        userId: Long,
        cartItems: List<CartItem>,
        deliveryAddress: String,
        customerPhone: String,
        customerName: String,
        notes: String = "",
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        deliveryMethod: DeliveryMethod = DeliveryMethod.DELIVERY
    ): Result<Long>
    
    /**
     * Обновить статус заказа
     */
    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus): Result<Unit>
    
    /**
     * Получить количество заказов пользователя
     */
    suspend fun getUserOrdersCount(userId: Long): Result<Int>
    
    /**
     * Получить заказы по статусу (для админа)
     */
    suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>>
    
    /**
     * Удалить заказ
     */
    suspend fun deleteOrder(orderId: Long): Result<Unit>
} 