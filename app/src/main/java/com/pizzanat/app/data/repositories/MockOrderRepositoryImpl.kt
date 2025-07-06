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

    init {
        // Добавляем тестовые заказы для демонстрации
        createTestOrders()
    }

    private fun createTestOrders() {
        // Тестовый заказ 1 для пользователя с ID=1 (первый зарегистрированный)
        val testOrder1 = Order(
            id = nextOrderId++,
            userId = 1L,
            items = listOf(
                OrderItem(
                    id = 1L,
                    orderId = 1L,
                    productId = 1L,
                    productName = "Маргарита",
                    productPrice = 450.0,
                    quantity = 2,
                    totalPrice = 900.0
                ),
                OrderItem(
                    id = 2L,
                    orderId = 1L,
                    productId = 2L,
                    productName = "Пепперони",
                    productPrice = 520.0,
                    quantity = 1,
                    totalPrice = 520.0
                )
            ),
            status = OrderStatus.PENDING,
            totalAmount = 1420.0,
            deliveryMethod = DeliveryMethod.DELIVERY,
            deliveryAddress = "ул. Тестовая, д. 1, кв. 1",
            deliveryCost = 200.0,
            paymentMethod = PaymentMethod.CARD_ON_DELIVERY,
            customerPhone = "+79001234567",
            customerName = "Тестовый Пользователь",
            notes = "Тестовый заказ",
            estimatedDeliveryTime = LocalDateTime.now().plusMinutes(30),
            createdAt = LocalDateTime.now().minusHours(1),
            updatedAt = LocalDateTime.now().minusHours(1)
        )

        // Тестовый заказ 2 для пользователя с ID=1
        val testOrder2 = Order(
            id = nextOrderId++,
            userId = 1L,
            items = listOf(
                OrderItem(
                    id = 3L,
                    orderId = 2L,
                    productId = 3L,
                    productName = "Четыре сыра",
                    productPrice = 580.0,
                    quantity = 1,
                    totalPrice = 580.0
                )
            ),
            status = OrderStatus.DELIVERED,
            totalAmount = 780.0,
            deliveryMethod = DeliveryMethod.DELIVERY,
            deliveryAddress = "ул. Тестовая, д. 1, кв. 1",
            deliveryCost = 200.0,
            paymentMethod = PaymentMethod.CARD_ON_DELIVERY,
            customerPhone = "+79001234567",
            customerName = "Тестовый Пользователь",
            notes = "",
            estimatedDeliveryTime = LocalDateTime.now().minusHours(2),
            createdAt = LocalDateTime.now().minusHours(3),
            updatedAt = LocalDateTime.now().minusHours(2)
        )

        mockOrders.addAll(listOf(testOrder1, testOrder2))
        nextOrderId = 3L
    }

    override fun getUserOrdersFlow(userId: Long): Flow<List<Order>> = flow {
        val result = getUserOrders(userId)
        if (result.isSuccess) {
            val orders = result.getOrNull() ?: emptyList()
            emit(orders)
        } else {
            emit(emptyList())
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