/**
 * @file: SearchViewModel.kt
 * @description: ViewModel для экрана поиска продуктов
 * @dependencies: Hilt, ViewModel, Use Cases, Coroutines
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.entities.Product
import com.pizzanat.app.domain.usecases.product.SearchProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showEmptyState: Boolean = false,
    val recentSearches: List<String> = emptyList()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchProductsUseCase: SearchProductsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    companion object {
        private const val SEARCH_DELAY_MS = 500L // Задержка для дебаунса
    }

    fun updateQuery(newQuery: String) {
        _uiState.value = _uiState.value.copy(
            query = newQuery,
            error = null
        )

        // Отменяем предыдущий поиск
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _uiState.value = _uiState.value.copy(
                products = emptyList(),
                showEmptyState = false,
                isLoading = false
            )
            return
        }

        // Дебаунс поиска
        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY_MS)
            searchProducts(newQuery)
        }
    }

    private suspend fun searchProducts(query: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null,
            showEmptyState = false
        )

        try {
            val result = searchProductsUseCase(query)
            if (result.isSuccess) {
                val products = result.getOrNull() ?: emptyList()
                _uiState.value = _uiState.value.copy(
                    products = products,
                    isLoading = false,
                    showEmptyState = products.isEmpty() && query.isNotBlank()
                )

                // Добавляем в недавние поиски если есть результаты
                if (products.isNotEmpty()) {
                    addToRecentSearches(query)
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Ошибка поиска"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error,
                    showEmptyState = false
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Произошла ошибка при поиске",
                showEmptyState = false
            )
        }
    }

    private fun addToRecentSearches(query: String) {
        val currentSearches = _uiState.value.recentSearches.toMutableList()
        currentSearches.remove(query) // Убираем если уже есть
        currentSearches.add(0, query) // Добавляем в начало

        // Ограничиваем до 10 элементов
        val limitedSearches = currentSearches.take(10)

        _uiState.value = _uiState.value.copy(
            recentSearches = limitedSearches
        )
    }

    fun selectRecentSearch(query: String) {
        updateQuery(query)
    }

    fun clearRecentSearches() {
        _uiState.value = _uiState.value.copy(
            recentSearches = emptyList()
        )
    }

    fun clearQuery() {
        _uiState.value = _uiState.value.copy(
            query = "",
            products = emptyList(),
            showEmptyState = false,
            error = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}