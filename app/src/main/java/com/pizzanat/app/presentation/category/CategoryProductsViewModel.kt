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
import com.pizzanat.app.domain.usecases.cart.AddToCartUseCase
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
    val categoryName: String = "",
    val addToCartSuccess: String? = null
)

@HiltViewModel
class CategoryProductsViewModel @Inject constructor(
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialCategoryId: Long = savedStateHandle.get<Long>("categoryId") ?: 0L

    private val _uiState = MutableStateFlow(
        CategoryProductsUiState(categoryId = initialCategoryId)
    )
    val uiState: StateFlow<CategoryProductsUiState> = _uiState.asStateFlow()

    companion object {
        private const val PAGE_SIZE = 20
    }

    init {
        // Загружаем продукты только если есть корректный ID
        if (initialCategoryId > 0) {
            loadProducts()
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            val currentCategoryId = _uiState.value.categoryId
            android.util.Log.d("CategoryProductsVM", "Начинаем загрузку продуктов для категории: $currentCategoryId")
            
            if (currentCategoryId <= 0) {
                android.util.Log.w("CategoryProductsVM", "Некорректный categoryId: $currentCategoryId")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Некорректный ID категории"
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                currentPage = 0
            )

            try {
                val result = getProductsByCategoryUseCase(currentCategoryId, 0, PAGE_SIZE)
                android.util.Log.d("CategoryProductsVM", "Результат загрузки: isSuccess=${result.isSuccess}")
                
                if (result.isSuccess) {
                    val products = result.getOrNull() ?: emptyList()
                    android.util.Log.d("CategoryProductsVM", "Загружено продуктов: ${products.size}")
                    products.forEach { product ->
                        android.util.Log.d("CategoryProductsVM", "Продукт: ${product.name} (ID: ${product.id})")
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        products = products,
                        isLoading = false,
                        error = null,
                        currentPage = 0,
                        hasMoreData = products.size >= PAGE_SIZE
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                    android.util.Log.e("CategoryProductsVM", "Ошибка загрузки: $error", result.exceptionOrNull())
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("CategoryProductsVM", "Exception при загрузке: ${e.message}", e)
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
                val result = getProductsByCategoryUseCase(currentState.categoryId, nextPage, PAGE_SIZE)
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
            val currentCategoryId = _uiState.value.categoryId
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                error = null
            )

            try {
                val result = getProductsByCategoryUseCase(currentCategoryId, 0, PAGE_SIZE)
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
    
    fun setCategoryId(id: Long) {
        android.util.Log.d("CategoryProductsVM", "Установка нового categoryId: $id (было: ${_uiState.value.categoryId})")
        if (id != _uiState.value.categoryId && id > 0) {
            _uiState.value = _uiState.value.copy(categoryId = id)
            // Перезагружаем продукты с новым ID
            loadProducts()
        }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            try {
                val result = addToCartUseCase(product, 1)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        addToCartSuccess = "Товар \"${product.name}\" добавлен в корзину"
                    )
                    // Автоматически скрываем сообщение через 3 секунды
                    kotlinx.coroutines.delay(3000)
                    clearAddToCartSuccess()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Ошибка добавления в корзину"
                    _uiState.value = _uiState.value.copy(error = error)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Произошла ошибка при добавлении в корзину"
                )
            }
        }
    }

    fun clearAddToCartSuccess() {
        _uiState.value = _uiState.value.copy(addToCartSuccess = null)
    }
}