/**
 * @file: OrderEntity.kt
 * @description: Room entity для заказов с оплатой и доставкой
 * @dependencies: Room database
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.pizzanat.app.data.local.converters.MapConverter
import com.pizzanat.app.data.local.converters.OrderStatusConverter
import com.pizzanat.app.data.local.converters.PaymentMethodConverter
import com.pizzanat.app.data.local.converters.DeliveryMethodConverter
import com.pizzanat.app.domain.entities.OrderStatus
import com.pizzanat.app.domain.entities.PaymentMethod
import com.pizzanat.app.domain.entities.DeliveryMethod

@Entity(tableName = "orders")
@TypeConverters(
    MapConverter::class,
    OrderStatusConverter::class,
    PaymentMethodConverter::class,
    DeliveryMethodConverter::class
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long,
    val status: OrderStatus,
    val totalAmount: Double,
    val deliveryMethod: DeliveryMethod,
    val deliveryAddress: String = "",
    val deliveryCost: Double,
    val paymentMethod: PaymentMethod,
    val customerPhone: String,
    val customerName: String,
    val notes: String = "",
    val estimatedDeliveryTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) 