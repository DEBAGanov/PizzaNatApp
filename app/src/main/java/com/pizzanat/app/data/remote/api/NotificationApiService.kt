/**
 * @file: NotificationApiService.kt
 * @description: API сервис для работы с уведомлениями
 * @dependencies: Retrofit, Notification DTOs
 * @created: 2024-12-20
 */
package com.pizzanat.app.data.remote.api

import com.pizzanat.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface NotificationApiService {
    
    /**
     * Получение списка уведомлений пользователя с пагинацией
     */
    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "createdAt,desc"
    ): Response<NotificationsPageDto>
    
    /**
     * Получение только непрочитанных уведомлений
     */
    @GET("notifications/unread")
    suspend fun getUnreadNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<NotificationsPageDto>
    
    /**
     * Получение количества непрочитанных уведомлений
     */
    @GET("notifications/unread/count")
    suspend fun getUnreadCount(): Response<Int>
    
    /**
     * Получение конкретного уведомления по ID
     */
    @GET("notifications/{id}")
    suspend fun getNotification(
        @Path("id") notificationId: String
    ): Response<NotificationDto>
    
    /**
     * Отметка уведомления как прочитанное
     */
    @PUT("notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") notificationId: String
    ): Response<Unit>
    
    /**
     * Отметка всех уведомлений как прочитанные
     */
    @PUT("notifications/read-all")
    suspend fun markAllAsRead(): Response<Unit>
    
    /**
     * Удаление уведомления
     */
    @DELETE("notifications/{id}")
    suspend fun deleteNotification(
        @Path("id") notificationId: String
    ): Response<Unit>
    
    /**
     * Очистка всех уведомлений
     */
    @DELETE("notifications")
    suspend fun clearAllNotifications(): Response<Unit>
    
    /**
     * Массовые операции с уведомлениями
     */
    @POST("notifications/bulk")
    suspend fun bulkAction(
        @Body action: BulkNotificationActionDto
    ): Response<Unit>
    
    /**
     * Получение настроек уведомлений
     */
    @GET("notifications/settings")
    suspend fun getNotificationSettings(): Response<NotificationSettingsDto>
    
    /**
     * Сохранение настроек уведомлений
     */
    @PUT("notifications/settings")
    suspend fun saveNotificationSettings(
        @Body settings: NotificationSettingsDto
    ): Response<Unit>
    
    /**
     * Регистрация FCM токена
     */
    @POST("notifications/fcm-token")
    suspend fun registerFcmToken(
        @Body tokenDto: FcmTokenDto
    ): Response<Unit>
    
    /**
     * Подписка на уведомления о заказе
     */
    @POST("notifications/orders/{orderId}/subscribe")
    suspend fun subscribeToOrderUpdates(
        @Path("orderId") orderId: Long
    ): Response<Unit>
    
    /**
     * Отписка от уведомлений о заказе
     */
    @DELETE("notifications/orders/{orderId}/subscribe")
    suspend fun unsubscribeFromOrderUpdates(
        @Path("orderId") orderId: Long
    ): Response<Unit>
    
    /**
     * Тестовая отправка уведомления (только для разработки)
     */
    @POST("notifications/test")
    suspend fun sendTestNotification(
        @Body notification: NotificationDto
    ): Response<Unit>
} 