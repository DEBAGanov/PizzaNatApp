/**
 * @file: MockOrderRepositoryImpl.kt
 * @description: Мок-реализация репозитория заказов для работы без API
 * @dependencies: OrderDao, OrderMappers, Room
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.repositories

import com.pizzanat.app.data.local.dao.OrderDao
import com.pizzanat.app.data.local.entities.OrderEntity
import com.pizzanat.app.data.mappers.toDomain
import com.pizzanat.app.data.mappers.toOrderItemEntity
import com.pizzanat.app.domain.entities.CartItem
import com.pizzanat.app.domain.entities.Order
import com.pizzanat.app.domain.entities.OrderStatus
import com.pizzanat.app.domain.entities.PaymentMethod
import com.pizzanat.app.domain.entities.DeliveryMethod
import com.pizzanat.app.domain.repositories.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockOrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao
) : OrderRepository {

    override fun getUserOrdersFlow(userId: Long): Flow<List<Order>> {
        return orderDao.getUserOrdersFlow(userId).map { entities ->
            entities.map { orderEntity ->
                val orderItems = orderDao.getOrderItems(orderEntity.id)
                orderEntity.toDomain(orderItems)
            }
        }
    }

    override suspend fun getUserOrders(userId: Long): Result<List<Order>> {
        return try {
            // Симуляция сетевой задержки для реалистичности
            delay(500)
            
            val orderEntities = orderDao.getUserOrders(userId)
            val orders = orderEntities.map { orderEntity ->
                val orderItems = orderDao.getOrderItems(orderEntity.id)
                orderEntity.toDomain(orderItems)
            }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка получения заказов: ${e.message}"))
        }
    }

    override suspend fun getOrderById(orderId: Long): Result<Order?> {
        return try {
            delay(200)
            
            val orderEntity = orderDao.getOrderById(orderId)
            if (orderEntity != null) {
                val orderItems = orderDao.getOrderItems(orderId)
                Result.success(orderEntity.toDomain(orderItems))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка получения заказа: ${e.message}"))
        }
    }

    override suspend fun createOrder(
        userId: Long,
        cartItems: List<CartItem>,
        deliveryAddress: String,
        customerPhone: String,
        customerName: String,
        notes: String,
        paymentMethod: PaymentMethod,
        deliveryMethod: DeliveryMethod
    ): Result<Long> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Симуляция обработки заказа
            delay(1000)
            
            val totalAmount = cartItems.sumOf { it.totalPrice }
            
            val orderEntity = OrderEntity(
                userId = userId,
                status = OrderStatus.PENDING,
                totalAmount = totalAmount,
                deliveryMethod = deliveryMethod,
                deliveryAddress = deliveryAddress,
                deliveryCost = deliveryMethod.cost,
                paymentMethod = paymentMethod,
                customerPhone = customerPhone,
                customerName = customerName,
                notes = notes,
                estimatedDeliveryTime = System.currentTimeMillis() + (30 * 60 * 1000), // +30 минут
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // Сохраняем заказ и получаем его ID
            val orderId = orderDao.insertOrder(orderEntity)
            
            // Сохраняем элементы заказа
            val orderItemEntities = cartItems.map { cartItem ->
                cartItem.toOrderItemEntity(orderId)
            }
            orderDao.insertOrderItems(orderItemEntities)
            
            Result.success(orderId)
            
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка создания заказа: ${e.message}"))
        }
    }

    override suspend fun updateOrderStatus(orderId: Long, status: OrderStatus): Result<Unit> {
        return try {
            delay(300)
            orderDao.updateOrderStatus(orderId, status)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка обновления статуса: ${e.message}"))
        }
    }

    override suspend fun getUserOrdersCount(userId: Long): Result<Int> {
        return try {
            val count = orderDao.getUserOrdersCount(userId)
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка подсчета заказов: ${e.message}"))
        }
    }

    override suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>> {
        return try {
            delay(400)
            val orderEntities = orderDao.getOrdersByStatus(status)
            val orders = orderEntities.map { orderEntity ->
                val orderItems = orderDao.getOrderItems(orderEntity.id)
                orderEntity.toDomain(orderItems)
            }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка получения заказов по статусу: ${e.message}"))
        }
    }

    override suspend fun deleteOrder(orderId: Long): Result<Unit> {
        return try {
            delay(200)
            orderDao.deleteOrder(orderId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка удаления заказа: ${e.message}"))
        }
    }
} 