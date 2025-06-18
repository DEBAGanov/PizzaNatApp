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
import com.pizzanat.app.domain.entities.DeliveryMethod
import com.pizzanat.app.domain.entities.Order
import com.pizzanat.app.domain.entities.PaymentMethod
import com.pizzanat.app.domain.repositories.AuthRepository
import com.pizzanat.app.domain.usecases.order.CreateOrderUseCase
import com.pizzanat.app.domain.usecases.order.GetUserOrdersUseCase
import com.pizzanat.app.presentation.checkout.OrderData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentUiState(
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH,
    val selectedDeliveryMethod: DeliveryMethod = DeliveryMethod.DELIVERY,
    val subtotal: Double = 0.0,
    val deliveryCost: Double = DeliveryMethod.DELIVERY.cost,
    val total: Double = 0.0,
    val isCreatingOrder: Boolean = false,
    val orderCreated: Boolean = false,
    val createdOrderId: Long? = null,
    val createdOrder: Order? = null,
    val error: String? = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val createOrderUseCase: CreateOrderUseCase,
    private val getUserOrdersUseCase: GetUserOrdersUseCase,
    private val authRepository: AuthRepository
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
    }
    
    fun selectPaymentMethod(method: PaymentMethod) {
        viewModelScope.launch {
            Log.d("PaymentViewModel", "Выбран способ оплаты: ${method.displayName}")
            _uiState.value = _uiState.value.copy(
                selectedPaymentMethod = method
            )
        }
    }
    
    fun selectDeliveryMethod(method: DeliveryMethod) {
        viewModelScope.launch {
            Log.d("PaymentViewModel", "Выбран способ доставки: ${method.displayName}, стоимость: ${method.cost}")
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                selectedDeliveryMethod = method,
                deliveryCost = method.cost,
                total = currentState.subtotal + method.cost
            )
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
                    
                    // Получаем полную информацию о созданном заказе
                    if (orderId != null) {
                        try {
                            val ordersResult = getUserOrdersUseCase.getUserOrders(currentUser.id)
                            val orders = ordersResult.getOrNull()
                            val createdOrder = orders?.find { it.id == orderId }
                            Log.d("PaymentViewModel", "Найден созданный заказ: $createdOrder")
                            
                            _uiState.value = _uiState.value.copy(
                                isCreatingOrder = false,
                                orderCreated = true,
                                createdOrderId = orderId,
                                createdOrder = createdOrder
                            )
                        } catch (e: Exception) {
                            Log.e("PaymentViewModel", "Ошибка получения созданного заказа: ${e.message}")
                            _uiState.value = _uiState.value.copy(
                                isCreatingOrder = false,
                                orderCreated = true,
                                createdOrderId = orderId
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isCreatingOrder = false,
                            orderCreated = true,
                            createdOrderId = orderId
                        )
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
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun resetOrderCreated() {
        _uiState.value = _uiState.value.copy(
            orderCreated = false,
            createdOrderId = null,
            createdOrder = null
        )
    }
} 