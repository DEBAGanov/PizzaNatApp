/**
 * @file: AdminProductsViewModel.kt
 * @description: ViewModel для управления продуктами в админ панели
 * @dependencies: Hilt, ViewModel, Admin use cases
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.admin.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.entities.Category
import com.pizzanat.app.domain.entities.Product
import com.pizzanat.app.domain.usecases.admin.GetAllProductsUseCase
import com.pizzanat.app.domain.usecases.admin.GetAllCategoriesUseCase
import com.pizzanat.app.domain.usecases.admin.CreateProductUseCase
import com.pizzanat.app.domain.usecases.admin.UpdateProductUseCase
import com.pizzanat.app.domain.usecases.admin.DeleteProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminProductsUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val selectedCategory: Category? = null,
    val searchQuery: String = "",
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val showAddProductDialog: Boolean = false,
    val showEditProductDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val editingProduct: Product? = null,
    val deletingProduct: Product? = null,
    val isOperationInProgress: Boolean = false
)

@HiltViewModel
class AdminProductsViewModel @Inject constructor(
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val createProductUseCase: CreateProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminProductsUiState())
    val uiState: StateFlow<AdminProductsUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                // Загружаем продукты и категории параллельно
                val productsResult = getAllProductsUseCase()
                val categoriesResult = getAllCategoriesUseCase()
                
                if (productsResult.isSuccess && categoriesResult.isSuccess) {
                    val products = productsResult.getOrNull() ?: emptyList()
                    val categories = categoriesResult.getOrNull() ?: emptyList()
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        products = products,
                        categories = categories,
                        error = null
                    )
                    applyFilters()
                } else {
                    val error = productsResult.exceptionOrNull()?.message 
                        ?: categoriesResult.exceptionOrNull()?.message 
                        ?: "Ошибка загрузки данных"
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
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
    
    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            
            try {
                val productsResult = getAllProductsUseCase()
                val categoriesResult = getAllCategoriesUseCase()
                
                if (productsResult.isSuccess && categoriesResult.isSuccess) {
                    val products = productsResult.getOrNull() ?: emptyList()
                    val categories = categoriesResult.getOrNull() ?: emptyList()
                    
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        products = products,
                        categories = categories,
                        error = null
                    )
                    applyFilters()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = "Ошибка обновления данных"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Ошибка обновления"
                )
            }
        }
    }
    
    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }
    
    fun setCategoryFilter(category: Category?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        applyFilters()
    }
    
    private fun applyFilters() {
        val currentState = _uiState.value
        var filteredProducts = currentState.products
        
        // Фильтр по категории
        if (currentState.selectedCategory != null) {
            filteredProducts = filteredProducts.filter { 
                it.categoryId == currentState.selectedCategory.id 
            }
        }
        
        // Поиск по названию
        if (currentState.searchQuery.isNotBlank()) {
            filteredProducts = filteredProducts.filter { product ->
                product.name.contains(currentState.searchQuery, ignoreCase = true) ||
                product.description.contains(currentState.searchQuery, ignoreCase = true)
            }
        }
        
        _uiState.value = _uiState.value.copy(filteredProducts = filteredProducts)
    }
    
    fun showAddProductDialog() {
        _uiState.value = _uiState.value.copy(showAddProductDialog = true)
    }
    
    fun hideAddProductDialog() {
        _uiState.value = _uiState.value.copy(showAddProductDialog = false)
    }
    
    fun showEditProductDialog(product: Product) {
        _uiState.value = _uiState.value.copy(
            showEditProductDialog = true,
            editingProduct = product
        )
    }
    
    fun hideEditProductDialog() {
        _uiState.value = _uiState.value.copy(
            showEditProductDialog = false,
            editingProduct = null
        )
    }
    
    fun showDeleteConfirmDialog(product: Product) {
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmDialog = true,
            deletingProduct = product
        )
    }
    
    fun hideDeleteConfirmDialog() {
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmDialog = false,
            deletingProduct = null
        )
    }
    
    fun createProduct(product: Product) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOperationInProgress = true)
            
            try {
                val result = createProductUseCase(product)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isOperationInProgress = false,
                        showAddProductDialog = false
                    )
                    refreshData() // Обновляем список
                } else {
                    _uiState.value = _uiState.value.copy(
                        isOperationInProgress = false,
                        error = result.exceptionOrNull()?.message ?: "Ошибка создания продукта"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isOperationInProgress = false,
                    error = e.message ?: "Ошибка создания продукта"
                )
            }
        }
    }
    
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOperationInProgress = true)
            
            try {
                val result = updateProductUseCase(product)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isOperationInProgress = false,
                        showEditProductDialog = false,
                        editingProduct = null
                    )
                    refreshData() // Обновляем список
                } else {
                    _uiState.value = _uiState.value.copy(
                        isOperationInProgress = false,
                        error = result.exceptionOrNull()?.message ?: "Ошибка обновления продукта"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isOperationInProgress = false,
                    error = e.message ?: "Ошибка обновления продукта"
                )
            }
        }
    }
    
    fun deleteProduct() {
        val productToDelete = _uiState.value.deletingProduct ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOperationInProgress = true)
            
            try {
                val result = deleteProductUseCase(productToDelete.id)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isOperationInProgress = false,
                        showDeleteConfirmDialog = false,
                        deletingProduct = null
                    )
                    refreshData() // Обновляем список
                } else {
                    _uiState.value = _uiState.value.copy(
                        isOperationInProgress = false,
                        error = result.exceptionOrNull()?.message ?: "Ошибка удаления продукта"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isOperationInProgress = false,
                    error = e.message ?: "Ошибка удаления продукта"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 