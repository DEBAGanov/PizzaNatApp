/**
 * @file: NotificationDto.kt
 * @description: DTO для работы с Notification API
 * @dependencies: Gson, Notification entities
 * @created: 2024-12-20
 */
package com.pizzanat.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO для уведомления
 */
data class NotificationDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("message") 
    val message: String,
    
    @SerializedName("type")
    val type: String, // ORDER_STATUS, DELIVERY_UPDATE, PROMOTION, SYSTEM
    
    @SerializedName("isRead")
    val isRead: Boolean = false,
    
    @SerializedName("orderId")
    val orderId: Long? = null,
    
    @SerializedName("createdAt")
    val createdAt: String, // ISO 8601 format
    
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    
    @SerializedName("actionUrl")
    val actionUrl: String? = null,
    
    @SerializedName("priority")
    val priority: String = "NORMAL" // HIGH, NORMAL, LOW
)

/**
 * DTO для настроек уведомлений
 */
data class NotificationSettingsDto(
    @SerializedName("pushNotificationsEnabled")
    val pushNotificationsEnabled: Boolean = true,
    
    @SerializedName("orderStatusEnabled")
    val orderStatusEnabled: Boolean = true,
    
    @SerializedName("deliveryUpdatesEnabled")
    val deliveryUpdatesEnabled: Boolean = true,
    
    @SerializedName("promotionsEnabled")
    val promotionsEnabled: Boolean = true,
    
    @SerializedName("soundEnabled")
    val soundEnabled: Boolean = true,
    
    @SerializedName("vibrationEnabled")
    val vibrationEnabled: Boolean = true,
    
    @SerializedName("quietHoursEnabled")
    val quietHoursEnabled: Boolean = false,
    
    @SerializedName("quietHoursStart")
    val quietHoursStart: String = "22:00",
    
    @SerializedName("quietHoursEnd")
    val quietHoursEnd: String = "08:00"
)

/**
 * DTO для регистрации FCM токена
 */
data class FcmTokenDto(
    @SerializedName("token")
    val token: String,
    
    @SerializedName("deviceId")
    val deviceId: String? = null,
    
    @SerializedName("platform")
    val platform: String = "android"
)

/**
 * DTO для подписки на уведомления заказа
 */
data class OrderSubscriptionDto(
    @SerializedName("orderId")
    val orderId: Long,
    
    @SerializedName("enabled")
    val enabled: Boolean = true
)

/**
 * DTO для пагинированного списка уведомлений
 */
data class NotificationsPageDto(
    @SerializedName("content")
    val content: List<NotificationDto>,
    
    @SerializedName("totalElements")
    val totalElements: Long,
    
    @SerializedName("totalPages")
    val totalPages: Int,
    
    @SerializedName("size")
    val size: Int,
    
    @SerializedName("number")
    val number: Int,
    
    @SerializedName("first")
    val first: Boolean,
    
    @SerializedName("last")
    val last: Boolean,
    
    @SerializedName("numberOfElements")
    val numberOfElements: Int
)

/**
 * DTO для массовых операций с уведомлениями
 */
data class BulkNotificationActionDto(
    @SerializedName("action")
    val action: String, // MARK_READ, DELETE
    
    @SerializedName("notificationIds")
    val notificationIds: List<String>? = null // null для всех уведомлений
) 