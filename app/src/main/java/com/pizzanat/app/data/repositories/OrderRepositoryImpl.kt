/**
 * @file: OrderRepositoryImpl.kt
 * @description: Реализация репозитория заказов с API интеграцией
 * @dependencies: OrderApiService, OrderRepository
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.repositories

import android.util.Log
import com.pizzanat.app.data.mappers.toDomain
import com.pizzanat.app.data.mappers.createOrderRequest
import com.pizzanat.app.data.remote.api.OrderApiService
import com.pizzanat.app.data.remote.util.safeApiCall
import com.pizzanat.app.data.remote.util.ApiResult
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
    private val orderApiService: OrderApiService
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
        try {
            val result = getUserOrders(userId)
            if (result.isSuccess) {
                val orders = result.getOrNull() ?: emptyList()
                Log.d("OrderRepository", "getUserOrdersFlow: Выдаем ${orders.size} заказов")
                emit(orders)
            } else {
                Log.w("OrderRepository", "getUserOrdersFlow: Ошибка API, возвращаем пустой список")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "getUserOrdersFlow: Исключение ${e.message}, возвращаем пустой список")
            emit(emptyList())
        }
    }

    override suspend fun getUserOrders(userId: Long): Result<List<Order>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("OrderRepository", "Запрос заказов пользователя через API...")
            
            val apiResult = safeApiCall { orderApiService.getUserOrders() }
            
            Log.d("OrderRepository", "API результат: success=${apiResult.isSuccess}")
            
            when (apiResult) {
                is ApiResult.Success -> {
                    val ordersResponse = apiResult.data
                    Log.d("OrderRepository", "API ответ получен: $ordersResponse")
                    
                    if (ordersResponse != null) {
                        Log.d("OrderRepository", "Обработка заказов: ${ordersResponse.content.size} записей")
                        ordersResponse.content.forEachIndexed { index, orderDto ->
                            Log.d("OrderRepository", "DTO Заказ $index: ID=${orderDto.id}, статус='${orderDto.status}', сумма=${orderDto.totalAmount}")
                            Log.d("OrderRepository", "  Адрес: '${orderDto.deliveryAddress}'")
                            Log.d("OrderRepository", "  Телефон: '${orderDto.contactPhone}'")
                            Log.d("OrderRepository", "  Комментарий: '${orderDto.comment}'")
                            Log.d("OrderRepository", "  Товаров: ${orderDto.items?.size ?: 0}")
                        }
                        
                        val orders = ordersResponse.toDomain()
                        Log.d("OrderRepository", "Заказы пользователя загружены с API: ${orders.size}")
                        orders.forEach { order ->
                            Log.d("OrderRepository", "Domain Заказ: ID=${order.id}, статус=${order.status}, сумма=${order.totalAmount}₽")
                            Log.d("OrderRepository", "  Адрес: '${order.deliveryAddress}'")
                            Log.d("OrderRepository", "  Телефон: '${order.customerPhone}'")
                            Log.d("OrderRepository", "  Комментарий: '${order.notes}'")
                            Log.d("OrderRepository", "  Дата: ${order.createdAt}")
                            Log.d("OrderRepository", "  Товаров: ${order.items.size}")
                        }
                        Result.success(orders)
                    } else {
                        Log.w("OrderRepository", "Пустой ответ API заказов пользователя")
                        Result.success(emptyList())
                    }
                }
                is ApiResult.Error -> {
                    Log.w("OrderRepository", "Ошибка API заказов пользователя: ${apiResult.message} (код: ${apiResult.code})")
                    
                    // Если ошибка 401, то проблема с токеном
                    if (apiResult.code == 401) {
                        Log.e("OrderRepository", "Проблема с авторизацией - возможно истек JWT токен")
                    }
                    
                    Result.failure(Exception("API Error: ${apiResult.message}"))
                }
                is ApiResult.NetworkError -> {
                    Log.w("OrderRepository", "Сетевая ошибка при загрузке заказов: ${apiResult.message}")
                    Result.failure(Exception("Network Error: ${apiResult.message}"))
                }
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Исключение при получении заказов пользователя: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getOrderById(orderId: Long): Result<Order?> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("OrderRepository", "🔍 Запрос заказа #$orderId через API...")
            
            val apiResult = safeApiCall { orderApiService.getOrderById(orderId) }
            
            Log.d("OrderRepository", "📡 API результат для заказа #$orderId: success=${apiResult.isSuccess}")
            
            if (apiResult.isSuccess) {
                val orderDto = apiResult.getOrNull()
                Log.d("OrderRepository", "📦 DTO заказа получен: $orderDto")
                
                if (orderDto != null) {
                    Log.d("OrderRepository", "📋 DTO детали заказа #$orderId:")
                    Log.d("OrderRepository", "  DTO ID: ${orderDto.id}")
                    Log.d("OrderRepository", "  DTO Status: '${orderDto.status}'")
                    Log.d("OrderRepository", "  DTO TotalAmount: ${orderDto.totalAmount}")
                    Log.d("OrderRepository", "  DTO DeliveryFee: ${orderDto.deliveryFee}") 
                    Log.d("OrderRepository", "  DTO ContactName: '${orderDto.contactName}'")
                    Log.d("OrderRepository", "  DTO ContactPhone: '${orderDto.contactPhone}'")
                    Log.d("OrderRepository", "  DTO DeliveryAddress: '${orderDto.deliveryAddress}'")
                    Log.d("OrderRepository", "  DTO DeliveryLocationName: '${orderDto.deliveryLocationName}'")
                    Log.d("OrderRepository", "  DTO Items count: ${orderDto.items?.size ?: 0}")
                    
                    // 🔍 НОВОЕ: Логируем весь JSON для диагностики
                    try {
                        val gson = com.google.gson.Gson()
                        val jsonString = gson.toJson(orderDto)
                        Log.d("OrderRepository", "📄 Полный JSON заказа: $jsonString")
                    } catch (e: Exception) {
                        Log.w("OrderRepository", "⚠️ Не удалось сериализовать DTO в JSON: ${e.message}")
                    }
                    
                    orderDto.items?.forEachIndexed { index, item ->
                        Log.d("OrderRepository", "    DTO Item ${index + 1}: ${item.productName} - ${item.quantity} × ${item.price}₽ = ${item.subtotal}₽")
                    }
                    
                    Log.d("OrderRepository", "🔄 Преобразование DTO в Domain объект...")
                    val order = orderDto.toDomain()
                    
                    Log.d("OrderRepository", "✅ Domain объект создан для заказа #$orderId:")
                    Log.d("OrderRepository", "  Domain ID: ${order.id}")
                    Log.d("OrderRepository", "  Domain Status: ${order.status}")
                    Log.d("OrderRepository", "  Domain TotalAmount: ${order.totalAmount}")
                    Log.d("OrderRepository", "  Domain CustomerName: '${order.customerName}'")
                    Log.d("OrderRepository", "  Domain CustomerPhone: '${order.customerPhone}'")
                    Log.d("OrderRepository", "  Domain DeliveryAddress: '${order.deliveryAddress}'")
                    Log.d("OrderRepository", "  Domain Items count: ${order.items.size}")
                    Log.d("OrderRepository", "  Domain GrandTotal: ${order.grandTotal}")
                    
                    order.items.forEachIndexed { index, item ->
                        Log.d("OrderRepository", "    Domain Item ${index + 1}: ${item.productName} - ${item.quantity} × ${item.productPrice}₽ = ${item.totalPrice}₽")
                    }
                    
                    Result.success(order)
                } else {
                    Log.w("OrderRepository", "⚠️ Заказ #$orderId не найден в API (orderDto = null)")
                    Result.success(null)
                }
            } else {
                val errorMessage = apiResult.getErrorMessage()
                Log.e("OrderRepository", "❌ Ошибка API при получении заказа #$orderId: $errorMessage")
                Result.failure(Exception("API Error: $errorMessage"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "💥 Исключение при получении заказа #$orderId: ${e.message}")
            Log.e("OrderRepository", "💥 Stacktrace: ${e.stackTraceToString()}")
            Result.failure(e)
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
            
            // Преобразуем PaymentMethod в строку для API
            val paymentMethodString = when (paymentMethod) {
                PaymentMethod.SBP -> "SBP"
                PaymentMethod.CARD_ON_DELIVERY -> "CASH"
            }
            
            // Преобразуем DeliveryMethod в русские значения для backend API
            val deliveryTypeString = when (deliveryMethod) {
                DeliveryMethod.DELIVERY -> "Доставка курьером"
                DeliveryMethod.PICKUP -> "Самовывоз"
            }
            
            // Backend автоматически берет товары из корзины пользователя
            val createOrderRequest = createOrderRequest(
                deliveryAddress = deliveryAddress,
                contactName = customerName,
                contactPhone = normalizedPhone,
                comment = notes,
                paymentMethod = paymentMethodString,
                deliveryType = deliveryTypeString
            )
            
            Log.d("OrderRepository", "📤 ДЕТАЛЬНАЯ ДИАГНОСТИКА СОЗДАНИЯ ЗАКАЗА:")
            Log.d("OrderRepository", "  📋 CreateOrderRequest содержит:")
            Log.d("OrderRepository", "    deliveryAddress: '${createOrderRequest.deliveryAddress}'")
            Log.d("OrderRepository", "    contactName: '${createOrderRequest.contactName}'")
            Log.d("OrderRepository", "    contactPhone: '${createOrderRequest.contactPhone}'")
            Log.d("OrderRepository", "    comment: '${createOrderRequest.comment}'")
            Log.d("OrderRepository", "    paymentMethod: '${createOrderRequest.paymentMethod}'")
            Log.d("OrderRepository", "    deliveryType: '${createOrderRequest.deliveryType}' ← ИСПРАВЛЕНО! (русские значения)")
            
            // 🔍 Логируем JSON запроса
            try {
                val gson = com.google.gson.Gson()
                val jsonRequest = gson.toJson(createOrderRequest)
                Log.d("OrderRepository", "📤 JSON запроса в backend:")
                Log.d("OrderRepository", jsonRequest)
            } catch (e: Exception) {
                Log.w("OrderRepository", "⚠️ Не удалось сериализовать запрос в JSON: ${e.message}")
            }
            
            Log.d("OrderRepository", "🚀 Отправляем запрос в backend API...")
            
            val apiResult = safeApiCall { orderApiService.createOrder(createOrderRequest) }
            
            Log.d("OrderRepository", "📡 ПОЛУЧЕН ОТВЕТ ОТ BACKEND:")
            Log.d("OrderRepository", "  success: ${apiResult.isSuccess}")
            
            if (apiResult.isSuccess) {
                val orderDto = apiResult.getOrNull()
                if (orderDto != null) {
                    Log.d("OrderRepository", "✅ Заказ создан через API: ${orderDto.id}")
                    Log.d("OrderRepository", "🔍 ДИАГНОСТИКА СОЗДАННОГО ЗАКАЗА:")
                    Log.d("OrderRepository", "  📋 Backend вернул OrderDto:")
                    Log.d("OrderRepository", "    id: ${orderDto.id}")
                    Log.d("OrderRepository", "    deliveryAddress: '${orderDto.deliveryAddress}'")
                    Log.d("OrderRepository", "    deliveryFee: ${orderDto.deliveryFee} ₽")
                    Log.d("OrderRepository", "    contactName: '${orderDto.contactName}'")
                    Log.d("OrderRepository", "    contactPhone: '${orderDto.contactPhone}'")
                    
                    // 🎯 ИСПРАВЛЕНО: Теперь backend должен обработать deliveryType!
                    Log.i("OrderRepository", "✅ Отправлен корректный deliveryType: '$deliveryTypeString'")
                    Log.i("OrderRepository", "📋 Backend получил поле 'deliveryType' с русскими значениями")
                    Log.i("OrderRepository", "🔍 Ожидаем в БД: delivery_type='$deliveryTypeString', delivery_cost=${if (deliveryMethod == DeliveryMethod.DELIVERY) "200.0" else "0.0"}")
                    
                    Result.success(orderDto.id)
                } else {
                    Log.w("OrderRepository", "⚠️ Пустой ответ API при создании заказа")
                    Result.failure(Exception("Empty API response"))
                }
            } else {
                val errorMessage = apiResult.getErrorMessage()
                Log.e("OrderRepository", "❌ Ошибка API при создании заказа: $errorMessage")
                Log.e("OrderRepository", "❌ Возможные причины:")
                Log.e("OrderRepository", "   1. Backend не принимает значения deliveryType='$deliveryTypeString'")
                Log.e("OrderRepository", "   2. Проблема валидации в backend")
                Log.e("OrderRepository", "   3. Ошибка сохранения в базе данных")
                Result.failure(Exception("API Error: $errorMessage"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Исключение при создании заказа: ${e.message}")
            Result.failure(e)
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
                Log.w("OrderRepository", "Ошибка API при обновлении статуса: ${apiResult.getErrorMessage()}")
                Result.failure(Exception("API Error: ${apiResult.getErrorMessage()}"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Исключение при обновлении статуса: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getUserOrdersCount(userId: Long): Result<Int> = withContext(Dispatchers.IO) {
        return@withContext try {
            val ordersResult = getUserOrders(userId)
            if (ordersResult.isSuccess) {
                val count = ordersResult.getOrNull()?.size ?: 0
                Result.success(count)
            } else {
                Result.failure(Exception("Failed to get orders"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Исключение при подсчете заказов: ${e.message}")
            Result.failure(e)
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
                    val orders = adminOrdersPageResponse.toDomain()
                    Log.d("OrderRepository", "Заказы по статусу $status загружены с API: ${orders.size}")
                    Result.success(orders)
                } else {
                    Result.success(emptyList())
                }
            } else {
                Log.w("OrderRepository", "Ошибка API заказов по статусу")
                Result.failure(Exception("API Error: ${apiResult.getErrorMessage()}"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Исключение при получении заказов по статусу: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun deleteOrder(orderId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiResult = safeApiCall { orderApiService.cancelOrder(orderId) }
            
            if (apiResult.isSuccess) {
                Log.d("OrderRepository", "Заказ отменен через API: $orderId")
                Result.success(Unit)
            } else {
                Log.w("OrderRepository", "Ошибка API при отмене заказа: ${apiResult.getErrorMessage()}")
                Result.failure(Exception("API Error: ${apiResult.getErrorMessage()}"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Исключение при отмене заказа: ${e.message}")
            Result.failure(e)
        }
    }
} 

