/**
 * @file: CartViewModel.kt
 * @description: ViewModel для экрана корзины покупок
 * @dependencies: Hilt, ViewModel, Use Cases, Coroutines
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.entities.CartItem
import com.pizzanat.app.domain.usecases.cart.GetCartItemsUseCase
import com.pizzanat.app.domain.usecases.cart.UpdateCartItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val totalItems: Int = 0,
    val totalQuantity: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEmpty: Boolean = true
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartItemsUseCase: GetCartItemsUseCase,
    private val updateCartItemUseCase: UpdateCartItemUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        loadCartItems()
        observeCartItems()
    }

    private fun observeCartItems() {
        viewModelScope.launch {
            getCartItemsUseCase().collect { cartItems ->
                val totalPrice = cartItems.sumOf { it.totalPrice }
                val totalItems = cartItems.sumOf { it.quantity }
                
                _uiState.value = _uiState.value.copy(
                    items = cartItems,
                    totalPrice = totalPrice,
                    totalItems = totalItems,
                    totalQuantity = totalItems,
                    isEmpty = cartItems.isEmpty(),
                    isLoading = false
                )
            }
        }
    }

    private fun loadCartItems() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
    }

    fun updateItemQuantity(cartItemId: Long, newQuantity: Int) {
        viewModelScope.launch {
            val result = updateCartItemUseCase.updateQuantity(cartItemId, newQuantity)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Ошибка обновления количества"
                )
            }
        }
    }

    fun removeItem(cartItemId: Long) {
        viewModelScope.launch {
            val result = updateCartItemUseCase.removeItem(cartItemId)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Ошибка удаления товара"
                )
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            val result = updateCartItemUseCase.clearCart()
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Ошибка очистки корзины"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refresh() {
        loadCartItems()
    }
} 