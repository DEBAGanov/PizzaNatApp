/**
 * @file: CheckoutViewModel.kt
 * @description: ViewModel для экрана оформления заказа
 * @dependencies: Hilt, ViewModel, Use Cases, Coroutines
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.entities.CartItem
import com.pizzanat.app.domain.usecases.cart.GetCartItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val cartItems: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val totalItems: Int = 0,
    val deliveryAddress: String = "",
    val customerPhone: String = "",
    val customerName: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val addressError: String? = null,
    val phoneError: String? = null,
    val nameError: String? = null
)

/**
 * Данные заказа для передачи в PaymentScreen
 */
data class OrderData(
    val cartItems: List<CartItem>,
    val totalPrice: Double,
    val deliveryAddress: String,
    val customerPhone: String,
    val customerName: String,
    val notes: String
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val getCartItemsUseCase: GetCartItemsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()
    
    // Сохраненные данные заказа для передачи в PaymentScreen
    private var _savedOrderData: OrderData? = null
    val savedOrderData: OrderData? get() = _savedOrderData

    init {
        loadCartItems()
    }

    private fun loadCartItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            getCartItemsUseCase().collect { cartItems ->
                val totalPrice = cartItems.sumOf { it.totalPrice }
                val totalItems = cartItems.sumOf { it.quantity }
                
                _uiState.value = _uiState.value.copy(
                    cartItems = cartItems,
                    totalPrice = totalPrice,
                    totalItems = totalItems,
                    isLoading = false
                )
            }
        }
    }

    fun updateDeliveryAddress(address: String) {
        _uiState.value = _uiState.value.copy(
            deliveryAddress = address,
            addressError = null
        )
    }

    fun updateCustomerPhone(phone: String) {
        _uiState.value = _uiState.value.copy(
            customerPhone = phone,
            phoneError = null
        )
    }

    fun updateCustomerName(name: String) {
        _uiState.value = _uiState.value.copy(
            customerName = name,
            nameError = null
        )
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    /**
     * Валидирует поля формы
     * @return true если все поля валидны
     */
    fun validateFields(): Boolean {
        var isValid = true
        val currentState = _uiState.value
        
        val addressError = when {
            currentState.deliveryAddress.isBlank() -> "Адрес доставки обязателен"
            currentState.deliveryAddress.length < 10 -> "Адрес слишком короткий"
            else -> null
        }
        
        val phoneError = when {
            currentState.customerPhone.isBlank() -> "Номер телефона обязателен"
            currentState.customerPhone.length < 10 -> "Некорректный номер телефона"
            else -> null
        }
        
        val nameError = when {
            currentState.customerName.isBlank() -> "Имя получателя обязательно"
            currentState.customerName.length < 2 -> "Имя слишком короткое"
            else -> null
        }
        
        if (addressError != null || phoneError != null || nameError != null) {
            isValid = false
        }
        
        _uiState.value = _uiState.value.copy(
            addressError = addressError,
            phoneError = phoneError,
            nameError = nameError
        )
        
        return isValid
    }
    
    /**
     * Сохраняет данные заказа для передачи в PaymentScreen
     */
    fun saveOrderData() {
        val currentState = _uiState.value
        _savedOrderData = OrderData(
            cartItems = currentState.cartItems,
            totalPrice = currentState.totalPrice,
            deliveryAddress = currentState.deliveryAddress,
            customerPhone = currentState.customerPhone,
            customerName = currentState.customerName,
            notes = currentState.notes
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 