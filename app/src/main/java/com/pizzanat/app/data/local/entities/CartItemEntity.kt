/**
 * @file: CartItemEntity.kt
 * @description: Room entity для элементов корзины
 * @dependencies: Room database
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.pizzanat.app.data.local.converters.MapConverter

@Entity(tableName = "cart_items")
@TypeConverters(MapConverter::class)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val productId: Long,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String,
    val quantity: Int,
    val selectedOptions: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) 