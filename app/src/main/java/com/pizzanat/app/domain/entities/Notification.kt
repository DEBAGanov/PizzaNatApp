/**
 * @file: Notification.kt
 * @description: Domain entity для уведомлений
 * @dependencies: None
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.entities

import java.time.LocalDateTime

/**
 * Типы уведомлений
 */
enum class NotificationType {
    ORDER_STATUS_CHANGED,
    DELIVERY_UPDATE,
    PROMOTION,
    SYSTEM,
    REMINDER
}

/**
 * Уведомление
 */
data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val orderId: Long? = null,
    val actionUrl: String? = null,
    val imageUrl: String? = null
) {
    val displayMessage: String
        get() = when (type) {
            NotificationType.ORDER_STATUS_CHANGED -> "Статус заказа изменен: $message"
            NotificationType.DELIVERY_UPDATE -> "Обновление доставки: $message"
            NotificationType.PROMOTION -> "Специальное предложение: $message"
            NotificationType.SYSTEM -> "Системное сообщение: $message"
            NotificationType.REMINDER -> "Напоминание: $message"
        }
}

/**
 * Настройки уведомлений пользователя
 */
data class NotificationSettings(
    val pushNotificationsEnabled: Boolean = true,
    val orderStatusEnabled: Boolean = true,
    val deliveryUpdatesEnabled: Boolean = true,
    val promotionsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "08:00"
) 