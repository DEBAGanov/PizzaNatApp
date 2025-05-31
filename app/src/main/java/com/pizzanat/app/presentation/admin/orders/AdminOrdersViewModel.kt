/**
 * @file: AdminOrdersViewModel.kt
 * @description: ViewModel для управления заказами в админ панели
 * @dependencies: GetAllOrdersUseCase, UpdateOrderStatusUseCase
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.admin.orders

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
    val updatingOrderId: Long? = null
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
                val result = getAllOrdersUseCase()
                
                if (result.isSuccess) {
                    val orders = result.getOrNull() ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        orders = orders,
                        error = null
                    )
                    applyFilters()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Ошибка загрузки заказов"
                    )
                }
            } catch (e: Exception) {
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
                val result = getAllOrdersUseCase()
                
                if (result.isSuccess) {
                    val orders = result.getOrNull() ?: emptyList()
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
                val result = updateOrderStatusUseCase(orderId, newStatus)
                
                if (result.isSuccess) {
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
                    _uiState.value = _uiState.value.copy(
                        updatingOrderId = null,
                        error = "Ошибка обновления статуса: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    updatingOrderId = null,
                    error = "Ошибка: ${e.message}"
                )
            }
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