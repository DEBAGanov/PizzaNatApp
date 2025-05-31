/**
 * @file: AdminDashboardViewModel.kt
 * @description: ViewModel для dashboard админ панели с real-time обновлениями
 * @dependencies: AdminRepository, GetAdminStatsUseCase
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.entities.AdminStats
import com.pizzanat.app.domain.entities.AdminUser
import com.pizzanat.app.domain.repositories.AdminRepository
import com.pizzanat.app.domain.usecases.admin.GetAdminStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardUiState(
    val isLoading: Boolean = false,
    val adminStats: AdminStats? = null,
    val currentAdmin: AdminUser? = null,
    val error: String? = null,
    val lastUpdated: Long = 0L
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val getAdminStatsUseCase: GetAdminStatsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()
    
    private var autoRefreshJob: Job? = null
    
    init {
        loadCurrentAdmin()
        startAutoRefresh()
    }
    
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val statsResult = getAdminStatsUseCase()
                
                if (statsResult.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        adminStats = statsResult.getOrNull(),
                        lastUpdated = System.currentTimeMillis(),
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = statsResult.exceptionOrNull()?.message ?: "Ошибка загрузки статистики"
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
        loadCurrentAdmin()
        loadDashboardData()
    }
    
    private fun loadCurrentAdmin() {
        viewModelScope.launch {
            try {
                val result = adminRepository.getCurrentAdmin()
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        currentAdmin = result.getOrNull()
                    )
                }
            } catch (e: Exception) {
                // Логируем ошибку, но не показываем пользователю
                // так как это второстепенная функция
            }
        }
    }
    
    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            // Первая загрузка данных
            loadDashboardData()
            
            // Автообновление каждые 30 секунд
            while (true) {
                delay(30_000) // 30 секунд
                
                // Обновляем только если нет ошибки и не идет загрузка
                if (!_uiState.value.isLoading && _uiState.value.error == null) {
                    loadDashboardDataSilently()
                }
            }
        }
    }
    
    private suspend fun loadDashboardDataSilently() {
        try {
            val statsResult = getAdminStatsUseCase()
            
            if (statsResult.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    adminStats = statsResult.getOrNull(),
                    lastUpdated = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            // Игнорируем ошибки автообновления
            // чтобы не мешать пользователю
        }
    }
    
    fun observeStatsFlow() {
        viewModelScope.launch {
            getAdminStatsUseCase.getStatsFlow()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Ошибка получения статистики"
                    )
                }
                .collect { stats ->
                    _uiState.value = _uiState.value.copy(
                        adminStats = stats,
                        lastUpdated = System.currentTimeMillis(),
                        error = null
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        autoRefreshJob?.cancel()
    }
} 