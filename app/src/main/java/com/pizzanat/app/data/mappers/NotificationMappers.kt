/**
 * @file: NotificationMappers.kt
 * @description: Мапперы для преобразования Notification DTO в доменные модели
 * @dependencies: Notification DTOs, Domain entities
 * @created: 2024-12-20
 */
package com.pizzanat.app.data.mappers

import com.pizzanat.app.data.local.entities.NotificationEntity
import com.pizzanat.app.domain.entities.Notification
import com.pizzanat.app.domain.entities.NotificationSettings
import com.pizzanat.app.domain.entities.NotificationType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import com.pizzanat.app.data.remote.dto.*
import java.time.format.DateTimeFormatter

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

/**
 * Преобразование NotificationDto в доменную модель Notification
 */
fun NotificationDto.toDomain(): Notification {
    return Notification(
        id = this.id,
        title = this.title,
        message = this.message,
        type = parseNotificationType(this.type),
        isRead = this.isRead,
        orderId = this.orderId,
        createdAt = parseDateTime(this.createdAt),
        imageUrl = this.imageUrl,
        actionUrl = this.actionUrl
    )
}

/**
 * Преобразование списка NotificationDto в доменные модели
 */
fun List<NotificationDto>.toDomainFromDto(): List<Notification> {
    return this.map { it.toDomain() }
}

/**
 * Преобразование NotificationsPageDto в доменную модель
 */
fun NotificationsPageDto.toDomain(): List<Notification> {
    return this.content.toDomainFromDto()
}

/**
 * Преобразование NotificationSettingsDto в доменную модель
 */
fun NotificationSettingsDto.toDomain(): NotificationSettings {
    return NotificationSettings(
        pushNotificationsEnabled = this.pushNotificationsEnabled,
        orderStatusEnabled = this.orderStatusEnabled,
        deliveryUpdatesEnabled = this.deliveryUpdatesEnabled,
        promotionsEnabled = this.promotionsEnabled,
        soundEnabled = this.soundEnabled,
        vibrationEnabled = this.vibrationEnabled,
        quietHoursEnabled = this.quietHoursEnabled,
        quietHoursStart = this.quietHoursStart,
        quietHoursEnd = this.quietHoursEnd
    )
}

/**
 * Преобразование доменной модели NotificationSettings в DTO
 */
fun NotificationSettings.toDto(): NotificationSettingsDto {
    return NotificationSettingsDto(
        pushNotificationsEnabled = this.pushNotificationsEnabled,
        orderStatusEnabled = this.orderStatusEnabled,
        deliveryUpdatesEnabled = this.deliveryUpdatesEnabled,
        promotionsEnabled = this.promotionsEnabled,
        soundEnabled = this.soundEnabled,
        vibrationEnabled = this.vibrationEnabled,
        quietHoursEnabled = this.quietHoursEnabled,
        quietHoursStart = this.quietHoursStart,
        quietHoursEnd = this.quietHoursEnd
    )
}

/**
 * Создание FcmTokenDto для регистрации токена
 */
fun createFcmTokenDto(
    token: String,
    deviceId: String? = null
): FcmTokenDto {
    return FcmTokenDto(
        token = token,
        deviceId = deviceId,
        platform = "android"
    )
}

/**
 * Создание BulkNotificationActionDto для массовых операций
 */
fun createBulkActionDto(
    action: String,
    notificationIds: List<String>? = null
): BulkNotificationActionDto {
    return BulkNotificationActionDto(
        action = action,
        notificationIds = notificationIds
    )
}

/**
 * Парсинг строкового типа уведомления в enum
 */
private fun parseNotificationType(type: String): NotificationType {
    return when (type.uppercase()) {
        "ORDER_STATUS", "ORDER_STATUS_CHANGED" -> NotificationType.ORDER_STATUS_CHANGED
        "DELIVERY_UPDATE" -> NotificationType.DELIVERY_UPDATE
        "PROMOTION" -> NotificationType.PROMOTION
        "SYSTEM" -> NotificationType.SYSTEM
        "REMINDER" -> NotificationType.REMINDER
        else -> NotificationType.SYSTEM
    }
}

/**
 * Парсинг ISO 8601 даты в LocalDateTime
 */
private fun parseDateTime(dateTimeString: String): LocalDateTime {
    return try {
        // Поддержка различных форматов ISO 8601
        when {
            dateTimeString.contains('T') && dateTimeString.contains('Z') -> {
                // 2024-12-20T10:30:00Z
                LocalDateTime.parse(dateTimeString.removeSuffix("Z"), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
            dateTimeString.contains('T') && dateTimeString.contains('+') -> {
                // 2024-12-20T10:30:00+03:00
                LocalDateTime.parse(dateTimeString.substringBefore('+'), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
            dateTimeString.contains('T') -> {
                // 2024-12-20T10:30:00
                LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
            else -> {
                // Fallback: текущее время
                LocalDateTime.now()
            }
        }
    } catch (e: Exception) {
        // В случае ошибки парсинга возвращаем текущее время
        LocalDateTime.now()
    }
} 