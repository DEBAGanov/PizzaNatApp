/**
 * @file: NotificationsViewModel.kt
 * @description: ViewModel для экрана уведомлений
 * @dependencies: Hilt, ViewModel, Coroutines
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.entities.Notification
import com.pizzanat.app.domain.entities.NotificationType
import com.pizzanat.app.domain.usecases.notification.GetNotificationsUseCase
import com.pizzanat.app.domain.usecases.notification.ManageNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val manageNotificationsUseCase: ManageNotificationsUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "NotificationsViewModel"
    }
    
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()
    
    init {
        loadNotifications()
        loadUnreadCount()
    }
    
    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            getNotificationsUseCase()
                .catch { exception ->
                    Log.e(TAG, "Ошибка загрузки уведомлений: ${exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ошибка загрузки уведомлений: ${exception.message}"
                    )
                }
                .collect { notifications ->
                    Log.d(TAG, "Загружено уведомлений: ${notifications.size}")
                    _uiState.value = _uiState.value.copy(
                        notifications = notifications,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
    
    private fun loadUnreadCount() {
        viewModelScope.launch {
            getNotificationsUseCase.getUnreadCount()
                .catch { exception ->
                    Log.e(TAG, "Ошибка загрузки количества непрочитанных: ${exception.message}")
                }
                .collect { count ->
                    _uiState.value = _uiState.value.copy(unreadCount = count)
                }
        }
    }
    
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val result = manageNotificationsUseCase.markAsRead(notificationId)
                if (result.isFailure) {
                    Log.e(TAG, "Ошибка отметки уведомления как прочитанное: ${result.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(
                        error = "Ошибка отметки уведомления: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при отметке уведомления: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка отметки уведомления: ${e.message}"
                )
            }
        }
    }
    
    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val result = manageNotificationsUseCase.markAllAsRead()
                if (result.isFailure) {
                    Log.e(TAG, "Ошибка отметки всех уведомлений: ${result.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(
                        error = "Ошибка отметки всех уведомлений: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при отметке всех уведомлений: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка отметки всех уведомлений: ${e.message}"
                )
            }
        }
    }
    
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                val result = manageNotificationsUseCase.deleteNotification(notificationId)
                if (result.isFailure) {
                    Log.e(TAG, "Ошибка удаления уведомления: ${result.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(
                        error = "Ошибка удаления уведомления: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при удалении уведомления: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка удаления уведомления: ${e.message}"
                )
            }
        }
    }
    
    fun clearAllNotifications() {
        viewModelScope.launch {
            try {
                val result = manageNotificationsUseCase.clearAllNotifications()
                if (result.isFailure) {
                    Log.e(TAG, "Ошибка очистки уведомлений: ${result.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(
                        error = "Ошибка очистки уведомлений: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при очистке уведомлений: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка очистки уведомлений: ${e.message}"
                )
            }
        }
    }
    
    fun filterByType(type: NotificationType?) {
        _uiState.value = _uiState.value.copy(selectedFilter = type)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun refresh() {
        loadNotifications()
        loadUnreadCount()
    }
}

/**
 * UI состояние экрана уведомлений
 */
data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: NotificationType? = null
) {
    val filteredNotifications: List<Notification>
        get() = if (selectedFilter != null) {
            notifications.filter { it.type == selectedFilter }
        } else {
            notifications
        }
} 