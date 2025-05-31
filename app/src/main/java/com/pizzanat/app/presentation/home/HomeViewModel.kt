/**
 * @file: HomeViewModel.kt
 * @description: ViewModel для главного экрана с категориями и логикой загрузки
 * @dependencies: Hilt, ViewModel, Use Cases, Coroutines
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.entities.Category
import com.pizzanat.app.domain.usecases.product.GetCategoriesUseCase
import com.pizzanat.app.domain.usecases.notification.GetNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val unreadNotificationsCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getNotificationsUseCase: GetNotificationsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadCategories()
        loadUnreadNotificationsCount()
    }
    
    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val result = getCategoriesUseCase()
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        categories = result.getOrNull() ?: emptyList(),
                        isLoading = false,
                        error = null
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
                    error = e.message ?: "Произошла ошибка при загрузке категорий"
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
                val result = getCategoriesUseCase()
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        categories = result.getOrNull() ?: emptyList(),
                        isRefreshing = false,
                        error = null
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
    
    fun onCategorySelected(category: Category) {
        // Навигация к списку продуктов категории будет реализована в следующем шаге
        // TODO: Implement navigation to category products
    }
    
    private fun loadUnreadNotificationsCount() {
        viewModelScope.launch {
            getNotificationsUseCase.getUnreadCount().collect { count ->
                _uiState.value = _uiState.value.copy(
                    unreadNotificationsCount = count
                )
            }
        }
    }
} 