/**
 * @file: ProfileViewModel.kt
 * @description: ViewModel для экрана профиля пользователя
 * @dependencies: Hilt, ViewModel, Coroutines
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.entities.Order
import com.pizzanat.app.domain.entities.User
import com.pizzanat.app.domain.usecases.order.GetUserOrdersUseCase
import com.pizzanat.app.domain.usecases.user.GetCurrentUserUseCase
import com.pizzanat.app.domain.usecases.user.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val userOrders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val isOrdersLoading: Boolean = false,
    val isLoggingOut: Boolean = false,
    val error: String? = null,
    val ordersError: String? = null,
    val logoutCompleted: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserOrdersUseCase: GetUserOrdersUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val userResult = getCurrentUserUseCase()
                if (userResult.isSuccess) {
                    val user = userResult.getOrNull()
                    Log.d("ProfileViewModel", "Пользователь загружен: ID=${user?.id}, email=${user?.email}")
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        isLoading = false
                    )

                    // Загружаем заказы пользователя
                    if (user != null) {
                        Log.d("ProfileViewModel", "Загружаем заказы для пользователя с ID: ${user.id}")
                        loadUserOrders(user.id)
                    } else {
                        Log.w("ProfileViewModel", "Пользователь null - заказы не загружаем")
                    }
                } else {
                    val error = userResult.exceptionOrNull()?.message ?: "Ошибка загрузки профиля"
                    Log.e("ProfileViewModel", "Ошибка загрузки пользователя: $error")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Исключение при загрузке профиля: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Неожиданная ошибка: ${e.message}"
                )
            }
        }
    }

    private fun loadUserOrders(userId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOrdersLoading = true, ordersError = null)

            try {
                Log.d("ProfileViewModel", "Начинаем загрузку заказов для пользователя: $userId")
                getUserOrdersUseCase(userId).collect { orders ->
                    Log.d("ProfileViewModel", "Загружено заказов из Flow: ${orders.size}")
                    orders.forEach { order ->
                        Log.d("ProfileViewModel", "Заказ: ID=${order.id}, статус=${order.status}, сумма=${order.totalAmount}")
                    }
                    _uiState.value = _uiState.value.copy(
                        userOrders = orders,
                        isOrdersLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Исключение при загрузке заказов: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isOrdersLoading = false,
                    ordersError = "Ошибка загрузки заказов: ${e.message}"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoggingOut = true, error = null)

            try {
                Log.d("ProfileViewModel", "Начинаем выход из аккаунта")
                val result = logoutUseCase()
                if (result.isSuccess) {
                    Log.d("ProfileViewModel", "Выход из аккаунта успешен")
                    _uiState.value = _uiState.value.copy(
                        isLoggingOut = false,
                        logoutCompleted = true
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Ошибка выхода из аккаунта"
                    Log.e("ProfileViewModel", "Ошибка выхода: $error")
                    _uiState.value = _uiState.value.copy(
                        isLoggingOut = false,
                        error = error
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Исключение при выходе: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoggingOut = false,
                    error = "Ошибка выхода: ${e.message}"
                )
            }
        }
    }

    fun refreshProfile() {
        loadUserProfile()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearOrdersError() {
        _uiState.value = _uiState.value.copy(ordersError = null)
    }

    fun resetLogoutState() {
        _uiState.value = _uiState.value.copy(logoutCompleted = false)
    }
}