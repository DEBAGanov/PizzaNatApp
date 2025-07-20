/**
 * @file: PaymentViewModel.kt
 * @description: ViewModel для экрана оплаты и доставки
 * @dependencies: Hilt, ViewModel, Coroutines
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.payment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.entities.CreatePaymentRequest
import com.pizzanat.app.domain.entities.DeliveryEstimate
import com.pizzanat.app.domain.entities.DeliveryMethod
import com.pizzanat.app.domain.entities.Order
import com.pizzanat.app.domain.entities.OrderItem
import com.pizzanat.app.domain.entities.OrderStatus
import com.pizzanat.app.domain.entities.PaymentMethod
import com.pizzanat.app.domain.repositories.AuthRepository
import com.pizzanat.app.domain.usecases.address.GetDeliveryEstimateWithAmountUseCase
import com.pizzanat.app.domain.usecases.order.CreateOrderUseCase
import com.pizzanat.app.domain.usecases.order.GetUserOrdersUseCase
import com.pizzanat.app.domain.usecases.payment.CreatePaymentUseCase
import com.pizzanat.app.presentation.checkout.OrderData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDateTime

data class PaymentUiState(
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.SBP,
    val selectedDeliveryMethod: DeliveryMethod = DeliveryMethod.DELIVERY,
    val subtotal: Double = 0.0,
    val deliveryCost: Double = DeliveryMethod.DELIVERY.cost,
    val total: Double = 0.0,
    val isCreatingOrder: Boolean = false,
    val orderCreated: Boolean = false,
    val createdOrderId: Long? = null,
    val createdOrder: Order? = null,
    val paymentUrl: String? = null,
    val needsPayment: Boolean = false,
    val error: String? = null,
    // Новые поля для зонального расчета доставки
    val deliveryEstimate: DeliveryEstimate? = null,
    val isCalculatingDelivery: Boolean = false,
    val deliveryCalculationError: String? = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val createOrderUseCase: CreateOrderUseCase,
    private val getUserOrdersUseCase: GetUserOrdersUseCase,
    private val createPaymentUseCase: CreatePaymentUseCase,
    private val authRepository: AuthRepository,
    private val getDeliveryEstimateWithAmountUseCase: GetDeliveryEstimateWithAmountUseCase,
    private val sharedOrderStorage: com.pizzanat.app.presentation.order.SharedOrderStorage
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()
    
    private var orderData: OrderData? = null
    
    fun setOrderTotal(amount: Double) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                subtotal = amount,
                total = amount + currentState.deliveryCost
            )
        }
    }
    
    fun setOrderData(data: OrderData) {
        Log.d("PaymentViewModel", "Получены данные заказа: ${data.cartItems.size} товаров, адрес: ${data.deliveryAddress}")
        orderData = data
        setOrderTotal(data.totalPrice)
        
        // Рассчитываем стоимость доставки для указанного адреса
        if (data.deliveryAddress.isNotBlank()) {
            calculateDeliveryCost(data.deliveryAddress, data.totalPrice)
        }
    }
    
    /**
     * Расчет стоимости доставки с учетом зональной системы
     */
    private fun calculateDeliveryCost(address: String, orderAmount: Double) {
        Log.d("PaymentViewModel", "🚚 Расчет стоимости доставки для адреса: '$address', сумма заказа: $orderAmount ₽")
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCalculatingDelivery = true,
                deliveryCalculationError = null
            )
            
            getDeliveryEstimateWithAmountUseCase(address, orderAmount)
                .onSuccess { estimate ->
                    Log.d("PaymentViewModel", "✅ Расчет доставки завершен:")
                    Log.d("PaymentViewModel", "  Зона: ${estimate.zoneName}")
                    Log.d("PaymentViewModel", "  Стоимость: ${estimate.deliveryCost} ₽")
                    Log.d("PaymentViewModel", "  Бесплатная: ${estimate.isDeliveryFree}")
                    
                    val newDeliveryCost = estimate.deliveryCost
                    val currentState = _uiState.value
                    
                    _uiState.value = currentState.copy(
                        deliveryEstimate = estimate,
                        deliveryCost = newDeliveryCost,
                        total = currentState.subtotal + newDeliveryCost,
                        isCalculatingDelivery = false,
                        deliveryCalculationError = null
                    )
                }
                .onFailure { error ->
                    Log.e("PaymentViewModel", "❌ Ошибка расчета доставки: ${error.message}")
                    
                    // Используем стандартную стоимость доставки в случае ошибки
                    val defaultCost = DeliveryMethod.DELIVERY.cost
                    val currentState = _uiState.value
                    
                    _uiState.value = currentState.copy(
                        deliveryCost = defaultCost,
                        total = currentState.subtotal + defaultCost,
                        isCalculatingDelivery = false,
                        deliveryCalculationError = "Не удалось рассчитать точную стоимость доставки. Используется стандартный тариф."
                    )
                }
        }
    }
    
    fun selectPaymentMethod(method: PaymentMethod) {
        viewModelScope.launch {
            Log.d("PaymentViewModel", "🔄 Изменение способа оплаты:")
            Log.d("PaymentViewModel", "  Было: ${_uiState.value.selectedPaymentMethod.displayName}")
            Log.d("PaymentViewModel", "  Стало: ${method.displayName}")
            _uiState.value = _uiState.value.copy(
                selectedPaymentMethod = method
            )
        }
    }
    
    fun selectDeliveryMethod(method: DeliveryMethod) {
        viewModelScope.launch {
            Log.d("PaymentViewModel", "Выбран способ доставки: ${method.displayName}")
            val currentState = _uiState.value
            
            if (method == DeliveryMethod.DELIVERY) {
                // Для доставки используем рассчитанную стоимость или пересчитываем
                val deliveryCost = currentState.deliveryEstimate?.deliveryCost ?: method.cost
                _uiState.value = currentState.copy(
                    selectedDeliveryMethod = method,
                    deliveryCost = deliveryCost,
                    total = currentState.subtotal + deliveryCost
                )
                
                // Если есть адрес и нет расчета доставки, пересчитываем
                val address = orderData?.deliveryAddress
                if (address?.isNotBlank() == true && currentState.deliveryEstimate == null) {
                    calculateDeliveryCost(address, currentState.subtotal)
                }
            } else {
                // Для самовывоза стоимость всегда 0
                _uiState.value = currentState.copy(
                    selectedDeliveryMethod = method,
                    deliveryCost = method.cost,
                    total = currentState.subtotal + method.cost
                )
            }
        }
    }
    
    fun createOrder() {
        Log.d("PaymentViewModel", "Начинаем создание заказа...")
        val data = orderData
        if (data == null) {
            Log.e("PaymentViewModel", "Данные заказа не найдены!")
            _uiState.value = _uiState.value.copy(
                error = "Данные заказа не найдены"
            )
            return
        }
        
        // Проверяем, что корзина не пуста
        if (data.cartItems.isEmpty()) {
            Log.e("PaymentViewModel", "Корзина пуста! Нельзя создать заказ.")
            _uiState.value = _uiState.value.copy(
                error = "Корзина пуста. Добавьте товары для создания заказа."
            )
            return
        }
        
        Log.d("PaymentViewModel", "Данные заказа найдены: товаров=${data.cartItems.size}, адрес=${data.deliveryAddress}, телефон=${data.customerPhone}, имя=${data.customerName}")
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCreatingOrder = true,
                error = null
            )
            
            try {
                // Получаем текущего пользователя
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    Log.e("PaymentViewModel", "Пользователь не авторизован!")
                    _uiState.value = _uiState.value.copy(
                        isCreatingOrder = false,
                        error = "Пользователь не авторизован"
                    )
                    return@launch
                }
                
                Log.d("PaymentViewModel", "Найден пользователь: ID=${currentUser.id}, email=${currentUser.email}")
                Log.d("PaymentViewModel", "📋 Выбранные параметры заказа:")
                Log.d("PaymentViewModel", "  💳 Способ оплаты: ${_uiState.value.selectedPaymentMethod.displayName}")
                Log.d("PaymentViewModel", "  🚚 Способ доставки: ${_uiState.value.selectedDeliveryMethod.displayName}")
                Log.d("PaymentViewModel", "  💰 Сумма товаров: ${_uiState.value.subtotal} ₽")
                Log.d("PaymentViewModel", "  🚛 Стоимость доставки: ${_uiState.value.deliveryCost} ₽") 
                Log.d("PaymentViewModel", "  💸 Общая сумма: ${_uiState.value.total} ₽")
                Log.d("PaymentViewModel", "Вызываем CreateOrderUseCase...")
                
                val result = createOrderUseCase(
                    userId = currentUser.id,
                    deliveryAddress = data.deliveryAddress,
                    customerPhone = data.customerPhone,
                    customerName = data.customerName,
                    notes = data.notes,
                    paymentMethod = _uiState.value.selectedPaymentMethod,
                    deliveryMethod = _uiState.value.selectedDeliveryMethod
                )
                
                Log.d("PaymentViewModel", "Результат CreateOrderUseCase: success=${result.isSuccess}")
                
                if (result.isSuccess) {
                    val orderId = result.getOrNull()
                    Log.d("PaymentViewModel", "Заказ успешно создан с ID: $orderId")
                    
                    // Проверяем способ оплаты
                    if (_uiState.value.selectedPaymentMethod == PaymentMethod.SBP && orderId != null) {
                        // Для СБП создаем платеж через ЮКасса
                        Log.d("PaymentViewModel", "Создаем платеж СБП для заказа $orderId")
                        createSbpPayment(orderId, _uiState.value.total, currentUser.email ?: "", data.customerPhone)
                    } else {
                        // Для оплаты при получении сразу переходим на успех
                        handleOrderSuccess(orderId, currentUser.id)
                    }
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Ошибка создания заказа"
                    Log.e("PaymentViewModel", "Ошибка создания заказа: $error")
                    _uiState.value = _uiState.value.copy(
                        isCreatingOrder = false,
                        error = error
                    )
                }
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "Исключение в createOrder: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isCreatingOrder = false,
                    error = "Неожиданная ошибка: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun createSbpPayment(orderId: Long, amount: Double, email: String, phone: String) {
        try {
            val currentState = _uiState.value
            Log.d("PaymentViewModel", "🔄 Создание СБП платежа через ЮКасса API для заказа $orderId")
            Log.d("PaymentViewModel", "  💰 Сумма товаров (subtotal): ${currentState.subtotal} ₽")
            Log.d("PaymentViewModel", "  🚚 Стоимость доставки: ${currentState.deliveryCost} ₽")
            Log.d("PaymentViewModel", "  💳 Общая сумма (total): ${currentState.total} ₽")
            Log.d("PaymentViewModel", "  📤 Передаваемая сумма (amount): $amount ₽")
            
            // ДОПОЛНИТЕЛЬНАЯ ВАЛИДАЦИЯ
            if (amount <= 0) {
                Log.e("PaymentViewModel", "❌ ОШИБКА: Некорректная сумма платежа: $amount")
                _uiState.value = _uiState.value.copy(
                    isCreatingOrder = false,
                    error = "Ошибка: некорректная сумма платежа ($amount ₽)"
                )
                return
            }
            
            val request = CreatePaymentRequest(
                amount = amount,
                currency = "RUB",
                orderId = orderId,
                paymentMethod = PaymentMethod.SBP,
                description = "Оплата заказа #$orderId через СБП",
                customerEmail = email.takeIf { it.isNotBlank() },
                customerPhone = phone.takeIf { it.isNotBlank() },
                returnUrl = "dimbopizza://payment_result"
            )
            
            Log.d("PaymentViewModel", "📋 Данные запроса платежа:")
            Log.d("PaymentViewModel", "  orderId: $orderId")
            Log.d("PaymentViewModel", "  amount: $amount")
            Log.d("PaymentViewModel", "  currency: RUB")
            Log.d("PaymentViewModel", "  method: SBP")
            Log.d("PaymentViewModel", "  email: ${request.customerEmail ?: "не указан"}")
            Log.d("PaymentViewModel", "  phone: ${request.customerPhone ?: "не указан"}")
            Log.d("PaymentViewModel", "  description: ${request.description}")
            
            val paymentResult = createPaymentUseCase(request)
            
            if (paymentResult.isSuccess) {
                val paymentInfo = paymentResult.getOrNull()
                Log.d("PaymentViewModel", "✅ СБП платеж создан через ЮКасса:")
                Log.d("PaymentViewModel", "  ID: ${paymentInfo?.id}")
                Log.d("PaymentViewModel", "  URL: ${paymentInfo?.confirmationUrl}")
                Log.d("PaymentViewModel", "  Статус: ${paymentInfo?.status}")
                
                _uiState.value = _uiState.value.copy(
                    isCreatingOrder = false,
                    needsPayment = true,
                    paymentUrl = paymentInfo?.confirmationUrl,
                    createdOrderId = orderId
                )
            } else {
                val error = paymentResult.exceptionOrNull()?.message ?: "Ошибка создания платежа"
                Log.e("PaymentViewModel", "❌ Ошибка создания СБП платежа через ЮКасса: $error")
                
                _uiState.value = _uiState.value.copy(
                    isCreatingOrder = false,
                    error = "Ошибка создания платежа: $error"
                )
            }
        } catch (e: Exception) {
            Log.e("PaymentViewModel", "💥 Исключение при создании СБП платежа: ${e.message}")
            _uiState.value = _uiState.value.copy(
                isCreatingOrder = false,
                error = "Неожиданная ошибка при создании платежа: ${e.message}"
            )
        }
    }
    
    private suspend fun handleOrderSuccess(orderId: Long?, userId: Long) {
        Log.d("PaymentViewModel", "📋 handleOrderSuccess вызван:")
        Log.d("PaymentViewModel", "  orderId: $orderId")
        Log.d("PaymentViewModel", "  userId: $userId")
        Log.d("PaymentViewModel", "  orderData есть: ${orderData != null}")
        
        if (orderId != null && orderData != null) {
            try {
                // Создаем Order объект из имеющихся данных вместо запроса к API
                val currentState = _uiState.value
                val data = orderData!!
                
                Log.d("PaymentViewModel", "🛒 Данные корзины:")
                Log.d("PaymentViewModel", "  Товаров в корзине: ${data.cartItems.size}")
                data.cartItems.forEachIndexed { index, item ->
                    Log.d("PaymentViewModel", "    ${index + 1}. ${item.productName} - ${item.quantity} × ${item.productPrice}₽ = ${item.totalPrice}₽")
                }
                Log.d("PaymentViewModel", "  📍 Адрес: '${data.deliveryAddress}'")
                Log.d("PaymentViewModel", "  📞 Телефон: '${data.customerPhone}'")
                Log.d("PaymentViewModel", "  👤 Имя: '${data.customerName}'")
                
                // Преобразуем CartItem в OrderItem с проверками
                val orderItems = data.cartItems.mapIndexed { index, cartItem ->
                    Log.d("PaymentViewModel", "Преобразуем товар ${index + 1}: ${cartItem.productName}")
                    OrderItem(
                        id = 0L, // Для созданного заказа ID элементов могут быть неизвестны
                        orderId = orderId,
                        productId = cartItem.productId,
                        productName = cartItem.productName,
                        productPrice = cartItem.productPrice,
                        quantity = cartItem.quantity,
                        totalPrice = cartItem.totalPrice
                    )
                }
                
                Log.d("PaymentViewModel", "📊 Суммы:")
                Log.d("PaymentViewModel", "  Сумма товаров (subtotal): ${currentState.subtotal}₽")
                Log.d("PaymentViewModel", "  Доставка: ${currentState.deliveryCost}₽")
                Log.d("PaymentViewModel", "  ИТОГО: ${currentState.subtotal + currentState.deliveryCost}₽")
                
                // Создаем полный Order объект
                val createdOrder = Order(
                    id = orderId,
                    userId = userId,
                    items = orderItems,
                    status = OrderStatus.PENDING,
                    totalAmount = currentState.subtotal,
                    deliveryMethod = currentState.selectedDeliveryMethod,
                    deliveryAddress = data.deliveryAddress,
                    deliveryCost = currentState.deliveryCost,
                    paymentMethod = currentState.selectedPaymentMethod,
                    customerPhone = data.customerPhone,
                    customerName = data.customerName,
                    notes = data.notes,
                    estimatedDeliveryTime = null,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                
                Log.d("PaymentViewModel", "✅ Создан Order объект из данных приложения:")
                Log.d("PaymentViewModel", "  ID: ${createdOrder.id}")
                Log.d("PaymentViewModel", "  Товаров: ${createdOrder.items.size}")
                Log.d("PaymentViewModel", "  Сумма: ${createdOrder.totalAmount}")
                Log.d("PaymentViewModel", "  Доставка: ${createdOrder.deliveryCost}")
                Log.d("PaymentViewModel", "  Итого: ${createdOrder.grandTotal}")
                Log.d("PaymentViewModel", "  Способ оплаты: ${createdOrder.paymentMethod.displayName}")
                Log.d("PaymentViewModel", "  📍 Адрес в Order: '${createdOrder.deliveryAddress}'")
                Log.d("PaymentViewModel", "  📞 Телефон в Order: '${createdOrder.customerPhone}'")
                Log.d("PaymentViewModel", "  👤 Имя в Order: '${createdOrder.customerName}'")
                
                // 💾 НОВОЕ: Сохраняем правильные данные заказа для OrderSuccessScreen
                Log.d("PaymentViewModel", "💾 Сохраняем заказ в SharedOrderStorage")
                sharedOrderStorage.saveOrder(createdOrder)
                
                _uiState.value = _uiState.value.copy(
                    isCreatingOrder = false,
                    orderCreated = true,
                    createdOrderId = orderId,
                    createdOrder = createdOrder
                )
                
                Log.d("PaymentViewModel", "🎯 UiState обновлен с createdOrder!")
                
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "❌ Ошибка создания Order объекта: ${e.message}")
                Log.e("PaymentViewModel", "❌ Stacktrace: ${e.stackTraceToString()}")
                
                // Даже при ошибке попробуем создать минимальный Order объект
                try {
                    val currentState = _uiState.value
                    val data = orderData!!
                    
                    val simpleOrder = Order(
                        id = orderId,
                        userId = userId,
                        items = listOf(
                            OrderItem(
                                id = 1L,
                                orderId = orderId,
                                productId = 1L,
                                productName = "Заказ #$orderId",
                                productPrice = currentState.subtotal,
                                quantity = 1,
                                totalPrice = currentState.subtotal
                            )
                        ),
                        status = OrderStatus.PENDING,
                        totalAmount = currentState.subtotal,
                        deliveryMethod = currentState.selectedDeliveryMethod,
                        deliveryAddress = data.deliveryAddress,
                        deliveryCost = currentState.deliveryCost,
                        paymentMethod = currentState.selectedPaymentMethod,
                        customerPhone = data.customerPhone,
                        customerName = data.customerName,
                        notes = data.notes,
                        estimatedDeliveryTime = null,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                    
                    Log.w("PaymentViewModel", "⚠️ Создан упрощенный Order объект как fallback")
                    
                    _uiState.value = _uiState.value.copy(
                        isCreatingOrder = false,
                        orderCreated = true,
                        createdOrderId = orderId,
                        createdOrder = simpleOrder
                    )
                } catch (e2: Exception) {
                    Log.e("PaymentViewModel", "❌ Даже упрощенный Order создать не удалось: ${e2.message}")
                    // Fallback - устанавливаем только ID
                _uiState.value = _uiState.value.copy(
                    isCreatingOrder = false,
                    orderCreated = true,
                        createdOrderId = orderId,
                        createdOrder = null
                )
                }
            }
        } else {
            Log.w("PaymentViewModel", "⚠️ Недостаточно данных для создания Order объекта")
            Log.w("PaymentViewModel", "  orderId: $orderId")
            Log.w("PaymentViewModel", "  orderData: ${orderData != null}")
            _uiState.value = _uiState.value.copy(
                isCreatingOrder = false,
                orderCreated = true,
                createdOrderId = orderId,
                createdOrder = null
            )
        }
    }
    
    fun onPaymentSuccess() {
        Log.d("PaymentViewModel", "🎉 Платеж успешно завершен")
        
        // Создаем Order объект для отображения кассового чека
        if (orderData != null && _uiState.value.createdOrderId != null) {
            try {
                val currentState = _uiState.value
                val data = orderData!!
                val orderId = currentState.createdOrderId!!
                
                // Преобразуем CartItem в OrderItem
                val orderItems = data.cartItems.map { cartItem ->
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
                
                // Создаем полный Order объект для СБП заказа
                val createdOrder = Order(
                    id = orderId,
                    userId = 0L, // Пользователь уже авторизован
                    items = orderItems,
                    status = OrderStatus.CONFIRMED, // СБП заказ оплачен
                    totalAmount = currentState.subtotal,
                    deliveryMethod = currentState.selectedDeliveryMethod,
                    deliveryAddress = data.deliveryAddress,
                    deliveryCost = currentState.deliveryCost,
                    paymentMethod = currentState.selectedPaymentMethod,
                    customerPhone = data.customerPhone,
                    customerName = data.customerName,
                    notes = data.notes,
                    estimatedDeliveryTime = null,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                
                Log.d("PaymentViewModel", "✅ Создан Order объект после успешной оплаты СБП:")
                Log.d("PaymentViewModel", "  ID: ${createdOrder.id}")
                Log.d("PaymentViewModel", "  Статус: ${createdOrder.status.displayName}")
                Log.d("PaymentViewModel", "  Способ оплаты: ${createdOrder.paymentMethod.displayName}")
                
                // 💾 НОВОЕ: Сохраняем правильные данные заказа для OrderSuccessScreen
                Log.d("PaymentViewModel", "💾 Сохраняем СБП заказ в SharedOrderStorage")
                sharedOrderStorage.saveOrder(createdOrder)
                
                _uiState.value = _uiState.value.copy(
                    needsPayment = false,
                    orderCreated = true,
                    paymentUrl = null,
                    createdOrder = createdOrder
                )
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "❌ Ошибка создания Order объекта после оплаты: ${e.message}")
                // Fallback без Order объекта
                _uiState.value = _uiState.value.copy(
                    needsPayment = false,
                    orderCreated = true,
                    paymentUrl = null
                )
            }
        } else {
            Log.w("PaymentViewModel", "⚠️ Недостаточно данных для создания Order объекта после оплаты")
        _uiState.value = _uiState.value.copy(
            needsPayment = false,
            orderCreated = true,
            paymentUrl = null
        )
        }
    }
    
    fun onPaymentFailed() {
        Log.d("PaymentViewModel", "Платеж не удался")
        _uiState.value = _uiState.value.copy(
            needsPayment = false,
            paymentUrl = null,
            error = "Платеж не удался. Попробуйте еще раз."
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Сброс флага создания заказа
     */
    fun resetOrderCreated() {
        _uiState.value = _uiState.value.copy(orderCreated = false)
    }
    
    /**
     * Очистка ошибки расчета доставки
     */
    fun clearDeliveryError() {
        _uiState.value = _uiState.value.copy(deliveryCalculationError = null)
    }
} 