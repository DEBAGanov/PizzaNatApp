/**
 * @file: PizzaNatDatabase.kt
 * @description: Room Database для приложения PizzaNat
 * @dependencies: Room, CartItemEntity, OrderEntity, OrderItemEntity, NotificationEntity
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.pizzanat.app.data.local.converters.MapConverter
import com.pizzanat.app.data.local.converters.OrderStatusConverter
import com.pizzanat.app.data.local.converters.PaymentMethodConverter
import com.pizzanat.app.data.local.converters.DeliveryMethodConverter
import com.pizzanat.app.data.local.converters.NotificationTypeConverter
import com.pizzanat.app.data.local.dao.CartDao
import com.pizzanat.app.data.local.dao.OrderDao
import com.pizzanat.app.data.local.dao.NotificationDao
import com.pizzanat.app.data.local.entities.CartItemEntity
import com.pizzanat.app.data.local.entities.OrderEntity
import com.pizzanat.app.data.local.entities.OrderItemEntity
import com.pizzanat.app.data.local.entities.NotificationEntity

@Database(
    entities = [
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        NotificationEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(
    MapConverter::class, 
    OrderStatusConverter::class,
    PaymentMethodConverter::class,
    DeliveryMethodConverter::class,
    NotificationTypeConverter::class
)
abstract class PizzaNatDatabase : RoomDatabase() {
    
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun notificationDao(): NotificationDao
    
    companion object {
        const val DATABASE_NAME = "pizzanat_database"
    }
} 