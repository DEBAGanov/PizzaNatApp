/**
 * @file: ManageNotificationsUseCase.kt
 * @description: Use case для управления уведомлениями (отметка как прочитанное, удаление)
 * @dependencies: NotificationRepository
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.notification

import com.pizzanat.app.domain.repositories.NotificationRepository
import javax.inject.Inject

class ManageNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    
    /**
     * Отметка уведомления как прочитанное
     */
    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return notificationRepository.markAsRead(notificationId)
    }
    
    /**
     * Отметка всех уведомлений как прочитанные
     */
    suspend fun markAllAsRead(): Result<Unit> {
        return notificationRepository.markAllAsRead()
    }
    
    /**
     * Удаление уведомления
     */
    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return notificationRepository.deleteNotification(notificationId)
    }
    
    /**
     * Очистка всех уведомлений
     */
    suspend fun clearAllNotifications(): Result<Unit> {
        return notificationRepository.clearAllNotifications()
    }
} 