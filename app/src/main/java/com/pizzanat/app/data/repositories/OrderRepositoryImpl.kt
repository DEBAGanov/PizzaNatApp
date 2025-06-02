/**
 * @file: OrderRepositoryImpl.kt
 * @description: Реализация репозитория заказов с API интеграцией
 * @dependencies: OrderApiService, OrderRepository
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.repositories

import android.util.Log
import com.pizzanat.app.data.mappers.toDomain
import com.pizzanat.app.data.mappers.toDomainOrders
import com.pizzanat.app.data.remote.api.OrderApiService
import com.pizzanat.app.data.remote.util.safeApiCall
import com.pizzanat.app.domain.entities.*
import com.pizzanat.app.domain.repositories.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderApiService: OrderApiService,
    private val mockOrderRepository: MockOrderRepositoryImpl
) : OrderRepository {

    /**
     * Нормализация номера телефона для API
     * Удаляет все символы кроме цифр и знака плюс в начале
     * Форматирует телефон в российский формат
     */
    private fun normalizePhoneNumber(phone: String): String {
        // Убираем все пробелы, скобки, тире и другие символы
        var normalized = phone.replace(Regex("[^+\\d]"), "")
        
        // Если номер начинается с 8, заменяем на +7
        if (normalized.startsWith("8") && normalized.length == 11) {
            normalized = "+7" + normalized.substring(1)
        }
        
        // Если номер начинается с 7 без плюса
        if (normalized.startsWith("7") && normalized.length == 11) {
            normalized = "+$normalized"
        }
        
        // Если номер без кода страны (10 цифр), добавляем +7
        if (normalized.matches(Regex("^\\d{10}$"))) {
            normalized = "+7$normalized"
        }
        
        // Валидируем финальный формат
        if (!normalized.matches(Regex("^\\+7\\d{10}$"))) {
            Log.w("OrderRepository", "Номер телефона '$phone' нормализован как '$normalized', но не соответствует ожидаемому формату +7XXXXXXXXXX")
        }
        
        return normalized
    }

    override fun getUserOrdersFlow(userId: Long): Flow<List<Order>> = flow {
        while (true) {
            val result = getUserOrders(userId)
            if (result.isSuccess) {
                result.getOrNull()?.let { emit(it) }
            }
            kotlinx.coroutines.delay(10000) // Обновляем каждые 10 секунд
        }
    }

    override suspend fun getUserOrders(userId: Long): Result<List<Order>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiResult = safeApiCall { orderApiService.getUserOrders() }
            
            if (apiResult.isSuccess) {
                val ordersResponse = apiResult.getOrNull()
                if (ordersResponse != null) {
                    val orders = ordersResponse.orders.toDomainOrders()
                    Log.d("OrderRepository", "Заказы пользователя загружены с API: ${orders.size}")
                    Result.success(orders)
                } else {
                    Log.w("OrderRepository", "Пустой ответ API заказов пользователя, используем mock")
                    mockOrderRepository.getUserOrders(userId)
                }
            } else {
                Log.w("OrderRepository", "Ошибка API заказов пользователя: ${apiResult.getErrorMessage()}, используем mock")
                mockOrderRepository.getUserOrders(userId)
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Исключение при получении заказов пользователя: ${e.message}")
            mockOrderRepository.getUserOrders(userId)
        }
    }

    override suspend fun getOrderById(orderId: Long): Result<Order?> = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiResult = safeApiCall { orderApiService.getOrderById(orderId) }
            
            if (apiResult.isSuccess) {
                val orderDto = apiResult.getOrNull()
                if (orderDto != null) {
                    val order = orderDto.toDomain()
                    Log.d("OrderRepository", "Заказ загружен с API: ${order.id}")
                    Result.success(order)
                } else {
                    Log.w("OrderRepository", "Заказ не найден через API")
                    Result.success(null)
                }
            } else {
                Log.w("OrderRepository", "Ошибка API заказа: ${apiResult.getErrorMessage()}, используем mock")
                mockOrderRepository.getOrderById(orderId)
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Исключение при получении заказа: ${e.message}")
            mockOrderRepository.getOrderById(orderId)
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
            // Нормализуем номер телефона перед отправкой в API
            val normalizedPhone = normalizePhoneNumber(customerPhone)
            
            // Backend автоматически берет товары из корзины пользователя
            val createOrderRequest = com.pizzanat.app.data.remote.dto.CreateOrderRequest(
                deliveryAddress = deliveryAddress,
                contactPhone = normalizedPhone,
                contactName = customerName,
                notes = notes
            )
            
            Log.d("OrderRepository", "Создание заказа через API: deliveryAddress=$deliveryAddress, contactName=$customerName")
            Log.d("OrderRepository", "Оригинальный телефон: '$customerPhone', нормализованный: '$normalizedPhone'")
            
            val apiResult = safeApiCall { orderApiService.createOrder(createOrderRequest) }
            
            if (apiResult.isSuccess) {
                val createResponse = apiResult.getOrNull()
                if (createResponse != null) {
                    Log.d("OrderRepository", "Заказ создан через API: ${createResponse.id}")
                    Result.success(createResponse.id)
                } else {
                    Log.w("OrderRepository", "Пустой ответ API при создании заказа, используем mock")
                    mockOrderRepository.createOrder(userId, cartItems, deliveryAddress, customerPhone, customerName, notes, paymentMethod, deliveryMethod)
                }
            } else {
                Log.w("OrderRepository", "Ошибка API при создании заказа: ${apiResult.getErrorMessage()}, используем mock")
                mockOrderRepository.createOrder(userId, cartItems, deliveryAddress, customerPhone, customerName, notes, paymentMethod, deliveryMethod)
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Исключение при создании заказа: ${e.message}")
            mockOrderRepository.createOrder(userId, cartItems, deliveryAddress, customerPhone, customerName, notes, paymentMethod, deliveryMethod)
        }
    }

    override suspend fun updateOrderStatus(orderId: Long, status: OrderStatus): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val request = com.pizzanat.app.data.remote.dto.UpdateOrderStatusRequest(status.name)
            val apiResult = safeApiCall { orderApiService.updateOrderStatus(orderId, request) }
            
            if (apiResult.isSuccess) {
                Log.d("OrderRepository", "Статус заказа обновлен через API: $orderId -> $status")
                Result.success(Unit)
            } else {
                Log.w("OrderRepository", "Ошибка API при обновлении статуса: ${apiResult.getErrorMessage()}, используем mock")
                mockOrderRepository.updateOrderStatus(orderId, status)
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Исключение при обновлении статуса: ${e.message}")
            mockOrderRepository.updateOrderStatus(orderId, status)
        }
    }

    override suspend fun getUserOrdersCount(userId: Long): Result<Int> = withContext(Dispatchers.IO) {
        return@withContext try {
            val ordersResult = getUserOrders(userId)
            if (ordersResult.isSuccess) {
                val count = ordersResult.getOrNull()?.size ?: 0
                Result.success(count)
            } else {
                mockOrderRepository.getUserOrdersCount(userId)
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Исключение при подсчете заказов: ${e.message}")
            mockOrderRepository.getUserOrdersCount(userId)
        }
    }

    override suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiResult = safeApiCall { 
                orderApiService.getAllOrders(status = status.name, page = 0, size = 100) 
            }
            
            if (apiResult.isSuccess) {
                val adminOrdersPageResponse = apiResult.getOrNull()
                if (adminOrdersPageResponse != null) {
                    val orders = adminOrdersPageResponse.toDomainOrders()
                    Log.d("OrderRepository", "Заказы по статусу $status загружены с API: ${orders.size}")
                    Result.success(orders)
                } else {
                    mockOrderRepository.getOrdersByStatus(status)
                }
            } else {
                Log.w("OrderRepository", "Ошибка API заказов по статусу, используем mock данные")
                mockOrderRepository.getOrdersByStatus(status)
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Исключение при получении заказов по статусу: ${e.message}")
            mockOrderRepository.getOrdersByStatus(status)
        }
    }

    override suspend fun deleteOrder(orderId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiResult = safeApiCall { orderApiService.cancelOrder(orderId) }
            
            if (apiResult.isSuccess) {
                Log.d("OrderRepository", "Заказ отменен через API: $orderId")
                Result.success(Unit)
            } else {
                Log.w("OrderRepository", "Ошибка API при отмене заказа: ${apiResult.getErrorMessage()}, используем mock")
                mockOrderRepository.deleteOrder(orderId)
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Исключение при отмене заказа: ${e.message}")
            mockOrderRepository.deleteOrder(orderId)
        }
    }
} 

