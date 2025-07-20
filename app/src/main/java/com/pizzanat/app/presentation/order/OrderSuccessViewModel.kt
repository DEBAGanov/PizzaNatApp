/**
 * @file: OrderSuccessViewModel.kt
 * @description: ViewModel для экрана успешного заказа
 * @dependencies: Hilt, ViewModel, GetOrderByIdUseCase
 * @created: 2024-12-25
 */
package com.pizzanat.app.presentation.order

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.entities.Order
import com.pizzanat.app.domain.entities.OrderItem
import com.pizzanat.app.domain.entities.OrderStatus
import com.pizzanat.app.domain.entities.DeliveryMethod
import com.pizzanat.app.domain.entities.PaymentMethod
import com.pizzanat.app.domain.usecases.order.GetOrderByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class OrderSuccessUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val error: String? = null
)

@HiltViewModel
class OrderSuccessViewModel @Inject constructor(
    private val getOrderByIdUseCase: GetOrderByIdUseCase,
    private val sharedOrderStorage: SharedOrderStorage
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OrderSuccessUiState())
    val uiState: StateFlow<OrderSuccessUiState> = _uiState.asStateFlow()
    
    fun loadOrder(orderId: Long) {
        Log.d("OrderSuccessViewModel", "🔄 Загрузка заказа #$orderId - сначала проверяем SharedOrderStorage")
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            // 🎯 НОВОЕ: Сначала проверяем SharedOrderStorage 
            Log.d("OrderSuccessViewModel", "💾 Проверяем SharedOrderStorage для заказа #$orderId")
            val cachedOrder = sharedOrderStorage.getOrder(orderId)
            
            if (cachedOrder != null) {
                Log.d("OrderSuccessViewModel", "✅ Заказ найден в SharedOrderStorage!")
                Log.d("OrderSuccessViewModel", "  📊 Данные из PaymentViewModel:")
                Log.d("OrderSuccessViewModel", "    ID: ${cachedOrder.id}")
                Log.d("OrderSuccessViewModel", "    Товаров: ${cachedOrder.items.size}")
                Log.d("OrderSuccessViewModel", "    Сумма: ${cachedOrder.totalAmount}")
                Log.d("OrderSuccessViewModel", "    Доставка: ${cachedOrder.deliveryCost}")
                Log.d("OrderSuccessViewModel", "    ИТОГО: ${cachedOrder.grandTotal}")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    order = cachedOrder,
                    error = null
                )
                return@launch
            }
            
            // Если нет в cache, загружаем из API как fallback
            Log.d("OrderSuccessViewModel", "⚠️ Заказ НЕ найден в SharedOrderStorage, загружаем из API")
            
            try {
                val result = getOrderByIdUseCase(orderId)
                
                if (result.isSuccess) {
                    val order = result.getOrNull()
                    if (order != null) {
                        Log.d("OrderSuccessViewModel", "✅ Заказ #$orderId загружен из API:")
                        Log.d("OrderSuccessViewModel", "  Товаров: ${order.items.size}")
                        order.items.forEachIndexed { index, item ->
                            Log.d("OrderSuccessViewModel", "    ${index + 1}. ${item.productName} - ${item.quantity} × ${item.productPrice}₽")
                        }
                        Log.d("OrderSuccessViewModel", "  📍 Адрес: '${order.deliveryAddress}'")
                        Log.d("OrderSuccessViewModel", "  📞 Телефон: '${order.customerPhone}'")
                        Log.d("OrderSuccessViewModel", "  👤 Имя: '${order.customerName}'")
                        Log.d("OrderSuccessViewModel", "  💰 Итого: ${order.grandTotal}₽")
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            order = order,
                            error = null
                        )
                    } else {
                        Log.w("OrderSuccessViewModel", "⚠️ Заказ #$orderId не найден в API")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            order = createFallbackOrder(orderId),
                            error = "Заказ не найден, отображаются базовые данные"
                        )
                    }
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Ошибка загрузки заказа"
                    Log.e("OrderSuccessViewModel", "❌ Ошибка загрузки заказа #$orderId: $error")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        order = createFallbackOrder(orderId),
                        error = "Не удалось загрузить данные заказа: $error"
                    )
                }
            } catch (e: Exception) {
                Log.e("OrderSuccessViewModel", "💥 Исключение при загрузке заказа #$orderId: ${e.message}")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    order = createFallbackOrder(orderId),
                    error = "Произошла ошибка при загрузке заказа"
                )
            }
        }
    }
    
    private fun createFallbackOrder(orderId: Long): Order {
        Log.d("OrderSuccessViewModel", "🔄 Создание fallback заказа #$orderId")
        
        return Order(
            id = orderId,
            userId = 1L,
            items = listOf(
                OrderItem(
                    id = 1L,
                    orderId = orderId,
                    productId = 1L,
                    productName = "Заказ #$orderId",
                    productPrice = 1.0,
                    quantity = 1,
                    totalPrice = 1.0
                )
            ),
            status = OrderStatus.PENDING,
            totalAmount = 1.0,
            deliveryMethod = DeliveryMethod.DELIVERY,
            deliveryAddress = "Данные заказа недоступны",
            deliveryCost = 0.0,
            paymentMethod = PaymentMethod.SBP,
            customerPhone = "Данные недоступны",
            customerName = "Клиент",
            notes = "Отображаются fallback данные",
            estimatedDeliveryTime = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 