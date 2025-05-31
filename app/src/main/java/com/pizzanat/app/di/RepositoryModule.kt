/**
 * @file: RepositoryModule.kt
 * @description: DI модуль для связывания интерфейсов репозиториев с реализациями
 * @dependencies: Hilt, Repository interfaces and implementations
 * @created: 2024-12-19
 */
package com.pizzanat.app.di

import com.pizzanat.app.data.repositories.AuthRepositoryImpl
import com.pizzanat.app.data.repositories.ProductRepositoryImpl
import com.pizzanat.app.data.repositories.OrderRepositoryImpl
import com.pizzanat.app.data.repositories.CartRepositoryImpl
import com.pizzanat.app.data.repositories.MockNotificationRepositoryImpl
import com.pizzanat.app.domain.repositories.AuthRepository
import com.pizzanat.app.domain.repositories.CartRepository
import com.pizzanat.app.domain.repositories.OrderRepository
import com.pizzanat.app.domain.repositories.ProductRepository
import com.pizzanat.app.domain.repositories.NotificationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
    
    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository
    
    @Binds
    @Singleton
    abstract fun bindCartRepository(
        cartRepositoryImpl: CartRepositoryImpl
    ): CartRepository
    
    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        orderRepositoryImpl: OrderRepositoryImpl
    ): OrderRepository
    
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        mockNotificationRepositoryImpl: MockNotificationRepositoryImpl
    ): NotificationRepository
} 