/**
 * @file: OrderItemEntity.kt
 * @description: Room entity для элементов заказа
 * @dependencies: Room database
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.pizzanat.app.data.local.converters.MapConverter

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"])]
)
@TypeConverters(MapConverter::class)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String,
    val quantity: Int,
    val selectedOptions: Map<String, String> = emptyMap()
) 