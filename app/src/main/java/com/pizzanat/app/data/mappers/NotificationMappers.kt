/**
 * @file: NotificationMappers.kt
 * @description: Mapper функции для преобразования между Entity и Domain моделями уведомлений
 * @dependencies: NotificationEntity, Notification, NotificationType
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.mappers

import com.pizzanat.app.data.local.entities.NotificationEntity
import com.pizzanat.app.domain.entities.Notification
import com.pizzanat.app.domain.entities.NotificationType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Преобразование NotificationEntity в Notification
 */
fun NotificationEntity.toDomain(): Notification {
    return Notification(
        id = id,
        title = title,
        message = message,
        type = NotificationType.valueOf(type),
        isRead = isRead,
        createdAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(createdAt),
            ZoneId.systemDefault()
        ),
        orderId = orderId,
        actionUrl = actionUrl,
        imageUrl = imageUrl
    )
}

/**
 * Преобразование Notification в NotificationEntity
 */
fun Notification.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = id,
        title = title,
        message = message,
        type = type.name,
        isRead = isRead,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        orderId = orderId,
        actionUrl = actionUrl,
        imageUrl = imageUrl
    )
}

/**
 * Преобразование списка NotificationEntity в список Notification
 */
fun List<NotificationEntity>.toDomain(): List<Notification> {
    return map { it.toDomain() }
}

/**
 * Преобразование списка Notification в список NotificationEntity
 */
fun List<Notification>.toEntity(): List<NotificationEntity> {
    return map { it.toEntity() }
} 