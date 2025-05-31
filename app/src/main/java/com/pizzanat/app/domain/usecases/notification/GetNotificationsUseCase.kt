/**
 * @file: GetNotificationsUseCase.kt
 * @description: Use case для получения уведомлений пользователя
 * @dependencies: NotificationRepository
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.notification

import com.pizzanat.app.domain.entities.Notification
import com.pizzanat.app.domain.repositories.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    
    /**
     * Получение всех уведомлений
     */
    operator fun invoke(): Flow<List<Notification>> {
        return notificationRepository.getAllNotifications()
    }
    
    /**
     * Получение только непрочитанных уведомлений
     */
    fun getUnread(): Flow<List<Notification>> {
        return notificationRepository.getUnreadNotifications()
    }
    
    /**
     * Получение количества непрочитанных уведомлений
     */
    fun getUnreadCount(): Flow<Int> {
        return notificationRepository.getUnreadCount()
    }
} 