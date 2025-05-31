/**
 * @file: NotificationTypeConverter.kt
 * @description: TypeConverter для преобразования NotificationType для Room
 * @dependencies: Room TypeConverter
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.local.converters

import androidx.room.TypeConverter
import com.pizzanat.app.domain.entities.NotificationType

class NotificationTypeConverter {
    
    @TypeConverter
    fun fromNotificationType(type: NotificationType): String {
        return type.name
    }
    
    @TypeConverter
    fun toNotificationType(typeString: String): NotificationType {
        return try {
            NotificationType.valueOf(typeString)
        } catch (e: IllegalArgumentException) {
            NotificationType.SYSTEM // fallback значение
        }
    }
} 