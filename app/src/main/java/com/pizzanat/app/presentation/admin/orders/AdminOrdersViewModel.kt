/**
 * @file: AdminOrdersViewModel.kt
 * @description: ViewModel для управления заказами в админ панели
 * @dependencies: GetAllOrdersUseCase, UpdateOrderStatusUseCase
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.admin.orders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.entities.Order
import com.pizzanat.app.domain.entities.OrderStatus
import com.pizzanat.app.domain.usecases.admin.GetAllOrdersUseCase
import com.pizzanat.app.domain.usecases.admin.UpdateOrderStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminOrdersUiState(
    val isLoading: Boolean = false,
    val orders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val error: String? = null,
    val selectedStatusFilter: OrderStatus? = null,
    val searchQuery: String = "",
    val isRefreshing: Boolean = false,
    val updatingOrderId: Long? = null,
    val testApiSuccess: String? = null // Для тестирования API
)

@HiltViewModel
class AdminOrdersViewModel @Inject constructor(
    private val getAllOrdersUseCase: GetAllOrdersUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminOrdersUiState())
    val uiState: StateFlow<AdminOrdersUiState> = _uiState.asStateFlow()
    
    private var autoRefreshJob: Job? = null
    
    init {
        loadOrders()
        startAutoRefresh()
    }
    
    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                Log.d("AdminOrdersViewModel", "Начинаем загрузку заказов для админ панели")
                val result = getAllOrdersUseCase()
                
                if (result.isSuccess) {
                    val orders = result.getOrNull() ?: emptyList()
                    Log.d("AdminOrdersViewModel", "✅ Загружено заказов для админа: ${orders.size}")
                    orders.forEach { order ->
                        Log.d("AdminOrdersViewModel", "Админ заказ: ID=${order.id}, клиент=${order.customerName}, статус=${order.status}, сумма=${order.totalAmount}")
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        orders = orders,
                        error = null
                    )
                    applyFilters()
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Ошибка загрузки заказов"
                    Log.e("AdminOrdersViewModel", "❌ Ошибка загрузки заказов: $errorMsg")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                }
            } catch (e: Exception) {
                Log.e("AdminOrdersViewModel", "❌ Исключение при загрузке заказов: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Неизвестная ошибка"
                )
            }
        }
    }
    
    fun refreshOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            
            try {
                Log.d("AdminOrdersViewModel", "Обновляем заказы админ панели")
                val result = getAllOrdersUseCase()
                
                if (result.isSuccess) {
                    val orders = result.getOrNull() ?: emptyList()
                    Log.d("AdminOrdersViewModel", "✅ Обновлено заказов: ${orders.size}")
                    _uiState.value = _uiState.value.copy(
                        orders = orders,
                        isRefreshing = false,
                        error = null
                    )
                    applyFilters()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = result.exceptionOrNull()?.message ?: "Ошибка обновления заказов"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Ошибка сети"
                )
            }
        }
    }
    
    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(updatingOrderId = orderId)
            
            try {
                Log.d("AdminOrdersViewModel", "Обновляем статус заказа $orderId на $newStatus")
                val result = updateOrderStatusUseCase(orderId, newStatus)
                
                if (result.isSuccess) {
                    Log.d("AdminOrdersViewModel", "✅ Статус заказа $orderId успешно обновлен")
                    // Обновляем заказ в локальном списке
                    val updatedOrders = _uiState.value.orders.map { order ->
                        if (order.id == orderId) {
                            order.copy(status = newStatus)
                        } else {
                            order
                        }
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        orders = updatedOrders,
                        updatingOrderId = null,
                        error = null
                    )
                    applyFilters()
                } else {
                    val errorMsg = "Ошибка обновления статуса: ${result.exceptionOrNull()?.message}"
                    Log.e("AdminOrdersViewModel", "❌ $errorMsg")
                    _uiState.value = _uiState.value.copy(
                        updatingOrderId = null,
                        error = errorMsg
                    )
                }
            } catch (e: Exception) {
                Log.e("AdminOrdersViewModel", "❌ Исключение при обновлении статуса: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    updatingOrderId = null,
                    error = "Ошибка: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Функция для тестирования API админской панели
     */
    fun testApiOrders() {
        viewModelScope.launch {
            try {
                Log.d("AdminOrdersViewModel", "🧪 ТЕСТ: Принудительная проверка API админских заказов")
                val result = getAllOrdersUseCase()
                
                if (result.isSuccess) {
                    val orders = result.getOrNull() ?: emptyList()
                    Log.d("AdminOrdersViewModel", "🧪 ТЕСТ API успешен: ${orders.size} заказов")
                    
                    _uiState.value = _uiState.value.copy(
                        testApiSuccess = "API работает! Загружено ${orders.size} заказов",
                        orders = orders,
                        error = null
                    )
                    applyFilters()
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "API недоступен"
                    Log.e("AdminOrdersViewModel", "🧪 ТЕСТ API неудачен: $errorMsg")
                    _uiState.value = _uiState.value.copy(
                        testApiSuccess = null,
                        error = "Тест API: $errorMsg"
                    )
                }
            } catch (e: Exception) {
                Log.e("AdminOrdersViewModel", "🧪 ТЕСТ API исключение: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    testApiSuccess = null,
                    error = "Тест API: ${e.message}"
                )
            }
            
            // Очищаем сообщение о тесте через 3 секунды
            delay(3000)
            _uiState.value = _uiState.value.copy(testApiSuccess = null)
        }
    }
    
    fun filterByStatus(status: OrderStatus?) {
        _uiState.value = _uiState.value.copy(selectedStatusFilter = status)
        applyFilters()
    }
    
    fun searchOrders(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }
    
    private fun applyFilters() {
        val currentState = _uiState.value
        var filteredOrders = currentState.orders
        
        // Фильтр по статусу
        currentState.selectedStatusFilter?.let { status ->
            filteredOrders = filteredOrders.filter { it.status == status }
        }
        
        // Поиск по имени клиента, телефону или ID заказа
        if (currentState.searchQuery.isNotBlank()) {
            val query = currentState.searchQuery.lowercase()
            filteredOrders = filteredOrders.filter { order ->
                order.customerName.lowercase().contains(query) ||
                order.customerPhone.contains(query) ||
                order.id.toString().contains(query) ||
                order.deliveryAddress.lowercase().contains(query)
            }
        }
        
        _uiState.value = currentState.copy(filteredOrders = filteredOrders)
    }
    
    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            delay(30_000) // Первое обновление через 30 секунд
            
            while (true) {
                if (!_uiState.value.isLoading && _uiState.value.updatingOrderId == null) {
                    refreshOrders()
                }
                delay(15_000) // Обновляем каждые 15 секунд
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun getOrderStatusDisplayName(status: OrderStatus): String {
        return when (status) {
            OrderStatus.PENDING -> "Ожидает"
            OrderStatus.CONFIRMED -> "Подтвержден"
            OrderStatus.PREPARING -> "Готовится"
            OrderStatus.READY -> "Готов"
            OrderStatus.DELIVERING -> "Доставляется"
            OrderStatus.DELIVERED -> "Доставлен"
            OrderStatus.CANCELLED -> "Отменен"
        }
    }
    
    fun getOrderStatusColor(status: OrderStatus): androidx.compose.ui.graphics.Color {
        return when (status) {
            OrderStatus.PENDING -> androidx.compose.ui.graphics.Color(0xFFFFA726)
            OrderStatus.CONFIRMED -> androidx.compose.ui.graphics.Color(0xFF42A5F5)
            OrderStatus.PREPARING -> androidx.compose.ui.graphics.Color(0xFF7E57C2)
            OrderStatus.READY -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
            OrderStatus.DELIVERING -> androidx.compose.ui.graphics.Color(0xFF26C6DA)
            OrderStatus.DELIVERED -> androidx.compose.ui.graphics.Color(0xFF66BB6A)
            OrderStatus.CANCELLED -> androidx.compose.ui.graphics.Color(0xFFEF5350)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        autoRefreshJob?.cancel()
    }
} 