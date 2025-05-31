/**
 * @file: NotificationEntity.kt
 * @description: Room entity для хранения уведомлений
 * @dependencies: Room, TypeConverters
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["created_at"]),
        Index(value = ["is_read"]),
        Index(value = ["type"]),
        Index(value = ["order_id"])
    ]
)
data class NotificationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "message")
    val message: String,
    
    @ColumnInfo(name = "type")
    val type: String, // Хранится как строка
    
    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long, // LocalDateTime конвертируется в Long
    
    @ColumnInfo(name = "order_id")
    val orderId: Long? = null,
    
    @ColumnInfo(name = "action_url")
    val actionUrl: String? = null,
    
    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null
) 