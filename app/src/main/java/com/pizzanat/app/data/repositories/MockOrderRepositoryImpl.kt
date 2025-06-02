/**
 * @file: MockOrderRepositoryImpl.kt
 * @description: Мок-реализация репозитория заказов с тестовыми данными
 * @dependencies: OrderRepository
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.repositories

import com.pizzanat.app.domain.entities.*
import com.pizzanat.app.domain.repositories.OrderRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockOrderRepositoryImpl @Inject constructor() : OrderRepository {

    private val mockOrders = mutableListOf<Order>()
    private var nextOrderId = 1L

    override fun getUserOrdersFlow(userId: Long): Flow<List<Order>> = flow {
        while (true) {
            val result = getUserOrders(userId)
            if (result.isSuccess) {
                result.getOrNull()?.let { emit(it) }
            }
            delay(10000)
        }
    }

    override suspend fun getUserOrders(userId: Long): Result<List<Order>> {
            delay(500)
        val userOrders = mockOrders.filter { it.userId == userId }
        return Result.success(userOrders)
    }

    override suspend fun getOrderById(orderId: Long): Result<Order?> {
        delay(300)
        val order = mockOrders.find { it.id == orderId }
        return Result.success(order)
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
    ): Result<Long> {
            delay(1000)
            
        val orderId = nextOrderId++
            val totalAmount = cartItems.sumOf { it.totalPrice }
            
        // Создаем OrderItem из CartItem
        val orderItems = cartItems.map { cartItem ->
            OrderItem(
                id = 0L,
                orderId = orderId,
                productId = cartItem.productId,
                productName = cartItem.productName,
                productPrice = cartItem.productPrice,
                quantity = cartItem.quantity,
                totalPrice = cartItem.totalPrice
            )
        }
        
        val newOrder = Order(
            id = orderId,
                userId = userId,
            items = orderItems,
                status = OrderStatus.PENDING,
                totalAmount = totalAmount,
                deliveryMethod = deliveryMethod,
                deliveryAddress = deliveryAddress,
                deliveryCost = deliveryMethod.cost,
                paymentMethod = paymentMethod,
                customerPhone = customerPhone,
                customerName = customerName,
                notes = notes,
            estimatedDeliveryTime = LocalDateTime.now().plusMinutes(30),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
            )
            
        mockOrders.add(newOrder)
        return Result.success(orderId)
    }

    override suspend fun updateOrderStatus(orderId: Long, status: OrderStatus): Result<Unit> {
        delay(500)
        val orderIndex = mockOrders.indexOfFirst { it.id == orderId }
        return if (orderIndex != -1) {
            val updatedOrder = mockOrders[orderIndex].copy(
                status = status,
                updatedAt = LocalDateTime.now()
            )
            mockOrders[orderIndex] = updatedOrder
            Result.success(Unit)
        } else {
            Result.failure(Exception("Заказ не найден"))
        }
    }

    override suspend fun getUserOrdersCount(userId: Long): Result<Int> {
        val userOrders = mockOrders.filter { it.userId == userId }
        return Result.success(userOrders.size)
    }

    override suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>> {
        delay(300)
        val filteredOrders = mockOrders.filter { it.status == status }
        return Result.success(filteredOrders)
    }

    override suspend fun deleteOrder(orderId: Long): Result<Unit> {
            delay(200)
        val removed = mockOrders.removeAll { it.id == orderId }
        return if (removed) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Заказ не найден"))
        }
    }
} 