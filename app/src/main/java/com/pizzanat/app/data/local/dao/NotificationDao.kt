/**
 * @file: NotificationDao.kt
 * @description: DAO интерфейс для работы с уведомлениями в Room
 * @dependencies: Room DAO, Flow
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.local.dao

import androidx.room.*
import com.pizzanat.app.data.local.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    
    /**
     * Получение всех уведомлений отсортированных по дате (новые первые)
     */
    @Query("SELECT * FROM notifications ORDER BY created_at DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>
    
    /**
     * Получение непрочитанных уведомлений
     */
    @Query("SELECT * FROM notifications WHERE is_read = 0 ORDER BY created_at DESC")
    fun getUnreadNotifications(): Flow<List<NotificationEntity>>
    
    /**
     * Получение количества непрочитанных уведомлений
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE is_read = 0")
    fun getUnreadCount(): Flow<Int>
    
    /**
     * Получение уведомлений по типу
     */
    @Query("SELECT * FROM notifications WHERE type = :type ORDER BY created_at DESC")
    fun getNotificationsByType(type: String): Flow<List<NotificationEntity>>
    
    /**
     * Получение уведомлений по заказу
     */
    @Query("SELECT * FROM notifications WHERE order_id = :orderId ORDER BY created_at DESC")
    fun getNotificationsByOrderId(orderId: Long): Flow<List<NotificationEntity>>
    
    /**
     * Получение уведомления по ID
     */
    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: String): NotificationEntity?
    
    /**
     * Вставка уведомления
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)
    
    /**
     * Вставка нескольких уведомлений
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)
    
    /**
     * Отметка уведомления как прочитанное
     */
    @Query("UPDATE notifications SET is_read = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String)
    
    /**
     * Отметка всех уведомлений как прочитанные
     */
    @Query("UPDATE notifications SET is_read = 1")
    suspend fun markAllAsRead()
    
    /**
     * Отметка уведомлений заказа как прочитанные
     */
    @Query("UPDATE notifications SET is_read = 1 WHERE order_id = :orderId")
    suspend fun markOrderNotificationsAsRead(orderId: Long)
    
    /**
     * Удаление уведомления
     */
    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotification(notificationId: String)
    
    /**
     * Удаление всех уведомлений
     */
    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()
    
    /**
     * Удаление старых уведомлений (старше указанной даты)
     */
    @Query("DELETE FROM notifications WHERE created_at < :timestamp")
    suspend fun deleteOldNotifications(timestamp: Long)
    
    /**
     * Удаление прочитанных уведомлений
     */
    @Query("DELETE FROM notifications WHERE is_read = 1")
    suspend fun deleteReadNotifications()
    
    /**
     * Получение количества всех уведомлений
     */
    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun getTotalCount(): Int
} 