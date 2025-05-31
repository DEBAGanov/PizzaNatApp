/**
 * @file: ProductDetailViewModel.kt
 * @description: ViewModel для экрана детальной информации о продукте
 * @dependencies: Hilt, ViewModel, Use Cases, Coroutines
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.entities.Product
import com.pizzanat.app.domain.usecases.cart.AddToCartUseCase
import com.pizzanat.app.domain.usecases.product.GetProductByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedQuantity: Int = 1,
    val isAddingToCart: Boolean = false,
    val showAddToCartSuccess: Boolean = false
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val productId: Long = savedStateHandle.get<String>("productId")?.toLongOrNull() ?: 0L
    
    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadProduct()
    }
    
    private fun loadProduct() {
        if (productId == 0L) {
            _uiState.value = _uiState.value.copy(
                error = "Неверный ID продукта",
                isLoading = false
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = getProductByIdUseCase(productId)
                if (result.isSuccess) {
                    val product = result.getOrNull()
                    _uiState.value = _uiState.value.copy(
                        product = product,
                        isLoading = false
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Ошибка загрузки продукта"
                    _uiState.value = _uiState.value.copy(
                        error = error,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Произошла ошибка при загрузке продукта",
                    isLoading = false
                )
            }
        }
    }
    
    fun updateQuantity(quantity: Int) {
        if (quantity in 1..10) {
            _uiState.value = _uiState.value.copy(selectedQuantity = quantity)
        }
    }
    
    fun addToCart() {
        val currentProduct = _uiState.value.product ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAddingToCart = true)
            
            try {
                val result = addToCartUseCase(currentProduct, _uiState.value.selectedQuantity)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isAddingToCart = false,
                        showAddToCartSuccess = true
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Ошибка добавления в корзину"
                    _uiState.value = _uiState.value.copy(
                        isAddingToCart = false,
                        error = error
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAddingToCart = false,
                    error = e.message ?: "Произошла ошибка при добавлении в корзину"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun hideAddToCartSuccess() {
        _uiState.value = _uiState.value.copy(showAddToCartSuccess = false)
    }
    
    fun retry() {
        loadProduct()
    }
} 