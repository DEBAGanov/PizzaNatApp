/**
 * @file: DatabaseModule.kt
 * @description: DI модуль для Room database
 * @dependencies: Hilt, Room, Database classes
 * @created: 2024-12-19
 */
package com.pizzanat.app.di

import android.content.Context
import androidx.room.Room
import com.pizzanat.app.data.local.dao.CartDao
import com.pizzanat.app.data.local.dao.OrderDao
import com.pizzanat.app.data.local.dao.NotificationDao
import com.pizzanat.app.data.local.database.PizzaNatDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun providePizzaNatDatabase(@ApplicationContext context: Context): PizzaNatDatabase {
        return Room.databaseBuilder(
            context,
            PizzaNatDatabase::class.java,
            PizzaNatDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration() // Для разработки, в продакшене нужны миграции
        .build()
    }
    
    @Provides
    fun provideCartDao(database: PizzaNatDatabase): CartDao {
        return database.cartDao()
    }
    
    @Provides
    fun provideOrderDao(database: PizzaNatDatabase): OrderDao {
        return database.orderDao()
    }
    
    @Provides
    fun provideNotificationDao(database: PizzaNatDatabase): NotificationDao {
        return database.notificationDao()
    }
} 