/**
 * @file: NotificationRepository.kt
 * @description: Repository интерфейс для управления уведомлениями
 * @dependencies: Notification entities, Flow
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.repositories

import com.pizzanat.app.domain.entities.Notification
import com.pizzanat.app.domain.entities.NotificationSettings
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    
    /**
     * Получение всех уведомлений пользователя
     */
    fun getAllNotifications(): Flow<List<Notification>>
    
    /**
     * Получение непрочитанных уведомлений
     */
    fun getUnreadNotifications(): Flow<List<Notification>>
    
    /**
     * Получение количества непрочитанных уведомлений
     */
    fun getUnreadCount(): Flow<Int>
    
    /**
     * Отметка уведомления как прочитанное
     */
    suspend fun markAsRead(notificationId: String): Result<Unit>
    
    /**
     * Отметка всех уведомлений как прочитанные
     */
    suspend fun markAllAsRead(): Result<Unit>
    
    /**
     * Удаление уведомления
     */
    suspend fun deleteNotification(notificationId: String): Result<Unit>
    
    /**
     * Очистка всех уведомлений
     */
    suspend fun clearAllNotifications(): Result<Unit>
    
    /**
     * Сохранение уведомления
     */
    suspend fun saveNotification(notification: Notification): Result<Unit>
    
    /**
     * Получение настроек уведомлений
     */
    suspend fun getNotificationSettings(): Result<NotificationSettings>
    
    /**
     * Сохранение настроек уведомлений
     */
    suspend fun saveNotificationSettings(settings: NotificationSettings): Result<Unit>
    
    /**
     * Регистрация FCM токена
     */
    suspend fun registerFcmToken(token: String): Result<Unit>
    
    /**
     * Подписка на уведомления о заказе
     */
    suspend fun subscribeToOrderUpdates(orderId: Long): Result<Unit>
    
    /**
     * Отписка от уведомлений о заказе
     */
    suspend fun unsubscribeFromOrderUpdates(orderId: Long): Result<Unit>
} 