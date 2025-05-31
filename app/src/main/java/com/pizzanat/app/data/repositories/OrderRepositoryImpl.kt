/**
 * @file: OrderRepositoryImpl.kt
 * @description: Реализация репозитория заказов с API интеграцией и Room кэшированием
 * @dependencies: OrderRepository, OrderDao, OrderApiService, OrderMappers
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.repositories

import android.util.Log
import com.pizzanat.app.data.local.dao.OrderDao
import com.pizzanat.app.data.local.entities.OrderEntity
import com.pizzanat.app.data.mappers.toDomain
import com.pizzanat.app.data.mappers.toEntity
import com.pizzanat.app.data.mappers.toOrderItemEntity
import com.pizzanat.app.data.mappers.toCreateOrderRequest
import com.pizzanat.app.data.mappers.toOrderDomain
import com.pizzanat.app.data.remote.api.OrderApiService
import com.pizzanat.app.data.remote.util.safeApiCall
import com.pizzanat.app.data.remote.util.toResult
import com.pizzanat.app.domain.entities.CartItem
import com.pizzanat.app.domain.entities.Order
import com.pizzanat.app.domain.entities.OrderStatus
import com.pizzanat.app.domain.entities.PaymentMethod
import com.pizzanat.app.domain.entities.DeliveryMethod
import com.pizzanat.app.domain.repositories.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
    private val orderApiService: OrderApiService
) : OrderRepository {

    override fun getUserOrdersFlow(userId: Long): Flow<List<Order>> {
        // Используем локальную базу данных для реактивного обновления
        return orderDao.getUserOrdersFlow(userId).map { entities ->
            entities.map { orderEntity ->
                val orderItems = orderDao.getOrderItems(orderEntity.id)
                orderEntity.toDomain(orderItems)
            }
        }
    }

    override suspend fun getUserOrders(userId: Long): Result<List<Order>> {
        return try {
            // Сначала пробуем загрузить с API для актуальных данных
            val apiResult = safeApiCall { orderApiService.getUserOrders() }
            
            if (apiResult.isSuccess) {
                val ordersResponse = apiResult.getOrNull()
                if (ordersResponse != null) {
                    val orders = ordersResponse.orders.toOrderDomain()
                    
                    // Кэшируем в локальную базу данных
                    cacheOrdersLocally(orders)
                    
                    Result.success(orders)
                } else {
                    // Fallback к локальным данным
                    getLocalUserOrders(userId)
                }
            } else {
                // При ошибке API используем локальные данные
                getLocalUserOrders(userId)
            }
        } catch (e: Exception) {
            // В случае ошибки возвращаем локальные данные
            getLocalUserOrders(userId)
        }
    }

    override suspend fun getOrderById(orderId: Long): Result<Order?> {
        return try {
            // Пробуем загрузить с API
            val apiResult = safeApiCall { orderApiService.getOrderById(orderId) }
            
            if (apiResult.isSuccess) {
                val orderDto = apiResult.getOrNull()
                if (orderDto != null) {
                    val order = orderDto.toDomain()
                    
                    // Кэшируем в локальную базу
                    cacheOrderLocally(order)
                    
                    Result.success(order)
                } else {
                    Result.success(null)
                }
            } else {
                // Fallback к локальным данным
                getLocalOrderById(orderId)
            }
        } catch (e: Exception) {
            getLocalOrderById(orderId)
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
    ): Result<Long> {
        Log.d("OrderRepository", "Создание заказа для пользователя $userId: товаров=${cartItems.size}, адрес=$deliveryAddress")
        
        return try {
            // Создаем заказ через API
            val createOrderRequest = cartItems.toCreateOrderRequest(
                deliveryAddress = deliveryAddress,
                contactPhone = customerPhone,
                contactName = customerName,
                notes = notes
            )
            
            Log.d("OrderRepository", "Отправляем запрос на создание заказа: ${createOrderRequest.items.size} товаров")
            
            val apiResult = safeApiCall { orderApiService.createOrder(createOrderRequest) }
            val result = apiResult.toResult()
            
            Log.d("OrderRepository", "Результат API запроса: success=${result.isSuccess}")
            
            if (result.isSuccess) {
                val response = result.getOrNull()
                if (response != null) {
                    Log.d("OrderRepository", "Заказ создан на сервере с ID: ${response.id}")
                    
                    // Создаем локальную копию заказа для немедленного отображения
                    val localOrder = createLocalOrder(
                        userId = userId,
                        cartItems = cartItems,
                        deliveryAddress = deliveryAddress,
                        customerPhone = customerPhone,
                        customerName = customerName,
                        notes = notes,
                        paymentMethod = paymentMethod,
                        deliveryMethod = deliveryMethod,
                        remoteOrderId = response.id
                    )
                    
                    Log.d("OrderRepository", "Локальная копия заказа создана")
                    
                    Result.success(response.id)
                } else {
                    Log.e("OrderRepository", "API ответ пустой")
                    Result.failure(Exception("Не удалось создать заказ"))
                }
            } else {
                val error = result.exceptionOrNull()
                Log.e("OrderRepository", "API ошибка: ${error?.message}")
                
                // При ошибке API сохраняем заказ локально для отправки позже
                val localOrderId = createLocalPendingOrder(
                    userId = userId,
                    cartItems = cartItems,
                    deliveryAddress = deliveryAddress,
                    customerPhone = customerPhone,
                    customerName = customerName,
                    notes = notes,
                    paymentMethod = paymentMethod,
                    deliveryMethod = deliveryMethod
                )
                
                Log.d("OrderRepository", "Заказ сохранен локально с ID: $localOrderId")
                Result.success(localOrderId)
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Исключение при создании заказа: ${e.message}")
            Result.failure(Exception("Ошибка создания заказа: ${e.message}"))
        }
    }

    override suspend fun updateOrderStatus(orderId: Long, status: OrderStatus): Result<Unit> {
        return try {
            // Обновляем через API
            val apiResult = safeApiCall { 
                orderApiService.updateOrderStatus(orderId, com.pizzanat.app.data.remote.dto.UpdateOrderStatusRequest(status.name))
            }
            
            val result = apiResult.toResult()
            
            if (result.isSuccess) {
                // Обновляем локально
                orderDao.updateOrderStatus(orderId, status)
                Result.success(Unit)
            } else {
                result.map { Unit }
            }
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
            // Отменяем через API
            val apiResult = safeApiCall { orderApiService.cancelOrder(orderId) }
            
            // Удаляем локально независимо от результата API
            orderDao.deleteOrder(orderId)
            
            if (apiResult.isSuccess) {
                Result.success(Unit)
            } else {
                Result.success(Unit) // Удалили локально, это главное
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка удаления заказа: ${e.message}"))
        }
    }

    // Приватные методы для работы с локальными данными
    private suspend fun getLocalUserOrders(userId: Long): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            val orderEntities = orderDao.getUserOrders(userId)
            val orders = orderEntities.map { orderEntity ->
                val orderItems = orderDao.getOrderItems(orderEntity.id)
                orderEntity.toDomain(orderItems)
            }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка получения локальных заказов: ${e.message}"))
        }
    }

    private suspend fun getLocalOrderById(orderId: Long): Result<Order?> = withContext(Dispatchers.IO) {
        try {
            val orderEntity = orderDao.getOrderById(orderId)
            if (orderEntity != null) {
                val orderItems = orderDao.getOrderItems(orderId)
                Result.success(orderEntity.toDomain(orderItems))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка получения локального заказа: ${e.message}"))
        }
    }

    private suspend fun cacheOrdersLocally(orders: List<Order>) = withContext(Dispatchers.IO) {
        try {
            orders.forEach { order ->
                cacheOrderLocally(order)
            }
        } catch (e: Exception) {
            // Игнорируем ошибки кэширования
        }
    }

    private suspend fun cacheOrderLocally(order: Order) = withContext(Dispatchers.IO) {
        try {
            val orderEntity = order.toEntity()
            val orderItemEntities = order.items.map { it.toOrderItemEntity(order.id) }
            
            orderDao.insertOrderWithItems(orderEntity, orderItemEntities)
        } catch (e: Exception) {
            // Игнорируем ошибки кэширования
        }
    }

    private suspend fun createLocalOrder(
        userId: Long,
        cartItems: List<CartItem>,
        deliveryAddress: String,
        customerPhone: String,
        customerName: String,
        notes: String,
        paymentMethod: PaymentMethod,
        deliveryMethod: DeliveryMethod,
        remoteOrderId: Long
    ): Long = withContext(Dispatchers.IO) {
        val totalAmount = cartItems.sumOf { it.totalPrice }
        
        val orderEntity = OrderEntity(
            id = remoteOrderId,
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
            estimatedDeliveryTime = System.currentTimeMillis() + (30 * 60 * 1000)
        )
        
        val orderItemEntities = cartItems.map { cartItem ->
            cartItem.toOrderItemEntity(remoteOrderId)
        }
        
        orderDao.insertOrderWithItems(orderEntity, orderItemEntities)
        remoteOrderId
    }

    private suspend fun createLocalPendingOrder(
        userId: Long,
        cartItems: List<CartItem>,
        deliveryAddress: String,
        customerPhone: String,
        customerName: String,
        notes: String,
        paymentMethod: PaymentMethod,
        deliveryMethod: DeliveryMethod
    ): Long = withContext(Dispatchers.IO) {
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
            estimatedDeliveryTime = System.currentTimeMillis() + (30 * 60 * 1000)
        )
        
        val orderId = orderDao.insertOrder(orderEntity)
        val orderItemEntities = cartItems.map { cartItem ->
            cartItem.toOrderItemEntity(orderId)
        }
        
        orderDao.insertOrderItems(orderItemEntities)
        orderId
    }
} 