/**
 * @file: CategoryProductsViewModel.kt
 * @description: ViewModel для экрана списка продуктов выбранной категории
 * @dependencies: Hilt, ViewModel, Use Cases, Coroutines
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.entities.Product
import com.pizzanat.app.domain.usecases.product.GetProductsByCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryProductsUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val hasMoreData: Boolean = true,
    val currentPage: Int = 0,
    val categoryId: Long = 0L,
    val categoryName: String = ""
)

@HiltViewModel
class CategoryProductsViewModel @Inject constructor(
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val categoryId: Long = savedStateHandle.get<Long>("categoryId") ?: 0L
    
    private val _uiState = MutableStateFlow(
        CategoryProductsUiState(categoryId = categoryId)
    )
    val uiState: StateFlow<CategoryProductsUiState> = _uiState.asStateFlow()
    
    companion object {
        private const val PAGE_SIZE = 20
    }
    
    init {
        loadProducts()
    }
    
    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                currentPage = 0
            )
            
            try {
                val result = getProductsByCategoryUseCase(categoryId, 0, PAGE_SIZE)
                if (result.isSuccess) {
                    val products = result.getOrNull() ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        products = products,
                        isLoading = false,
                        error = null,
                        currentPage = 0,
                        hasMoreData = products.size >= PAGE_SIZE
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Произошла ошибка при загрузке продуктов"
                )
            }
        }
    }
    
    fun loadMoreProducts() {
        val currentState = _uiState.value
        if (currentState.isLoadingMore || !currentState.hasMoreData) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingMore = true,
                error = null
            )
            
            try {
                val nextPage = currentState.currentPage + 1
                val result = getProductsByCategoryUseCase(categoryId, nextPage, PAGE_SIZE)
                if (result.isSuccess) {
                    val newProducts = result.getOrNull() ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        products = currentState.products + newProducts,
                        isLoadingMore = false,
                        currentPage = nextPage,
                        hasMoreData = newProducts.size >= PAGE_SIZE
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Ошибка загрузки данных"
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        error = error
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    error = e.message ?: "Произошла ошибка при загрузке"
                )
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                error = null
            )
            
            try {
                val result = getProductsByCategoryUseCase(categoryId, 0, PAGE_SIZE)
                if (result.isSuccess) {
                    val products = result.getOrNull() ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        products = products,
                        isRefreshing = false,
                        error = null,
                        currentPage = 0,
                        hasMoreData = products.size >= PAGE_SIZE
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = error
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Произошла ошибка при обновлении"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun setCategoryName(name: String) {
        _uiState.value = _uiState.value.copy(categoryName = name)
    }
} 