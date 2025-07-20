/**
 * @file: RepositoryModule.kt
 * @description: DI модуль для репозиториев с автоматическим выбором реализации
 * @dependencies: Hilt, BuildConfig, Repository implementations
 * @created: 2024-12-19
 * @updated: 2024-12-20 - Добавлена логика выбора реального/mock репозитория
 */
package com.pizzanat.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.pizzanat.app.BuildConfig
import com.pizzanat.app.data.local.dao.CartDao
import com.pizzanat.app.data.local.dao.NotificationDao
import com.pizzanat.app.data.network.api.AuthApiService
import com.pizzanat.app.data.remote.api.CartApiService
import com.pizzanat.app.data.remote.api.AddressApiService
import com.pizzanat.app.data.remote.api.NotificationApiService
import com.pizzanat.app.data.repositories.*
import com.pizzanat.app.domain.repositories.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    /**
     * Умный выбор AuthRepository в зависимости от окружения
     * - Production/Staging: Реальный API
     * - Debug + USE_MOCK_DATA=true: Mock данные
     * - Debug + USE_MOCK_DATA=false: Реальный API для тестирования
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        authApiService: AuthApiService,
        tokenManager: TokenManager,
        userManager: UserManager
    ): AuthRepository {
        // Всегда используем реальный API - Stage 13 интеграция
        return AuthRepositoryImpl(authApiService, tokenManager, userManager)
    }
    
    @Provides
    @Singleton
    fun provideProductRepository(
        productApiService: com.pizzanat.app.data.remote.api.ProductApiService
    ): ProductRepository {
        return ProductRepositoryImpl(productApiService)
    }
    
    @Provides
    @Singleton
    fun provideOrderRepository(
        orderApiService: com.pizzanat.app.data.remote.api.OrderApiService
    ): OrderRepository {
        // Stage 13 интеграция - используем реальный API
        return OrderRepositoryImpl(orderApiService)
    }
    
    @Provides
    @Singleton
    fun provideCartRepository(
        cartDao: CartDao,
        cartApiService: CartApiService
    ): CartRepository {
        // Всегда используем реальный API - Stage 13 интеграция
        return CartRepositoryImpl(cartDao, cartApiService)
    }
    
    @Provides
    @Singleton
    fun provideNotificationRepository(
        notificationApiService: NotificationApiService,
        notificationDao: NotificationDao,
        dataStore: DataStore<Preferences>
    ): NotificationRepository {
        // Stage 13 интеграция - готовим структуру для Backend API
        // Пока Backend API не готов (500 ошибки), используем mock
        return if (BuildConfig.DEBUG && BuildConfig.USE_MOCK_DATA) {
            MockNotificationRepositoryImpl(notificationDao, dataStore)
        } else {
            // Когда Backend API будет готов, переключаемся на реальную реализацию
            MockNotificationRepositoryImpl(notificationDao, dataStore)
            // NotificationRepositoryImpl(notificationApiService, notificationDao, dataStore)
        }
    }

    @Provides
    @Singleton
    fun provideAddressRepository(
        addressApiService: AddressApiService
    ): AddressRepository {
        return AddressRepositoryImpl(addressApiService)
    }

    @Provides
    @Singleton
    fun providePaymentRepository(
        paymentApiService: com.pizzanat.app.data.remote.api.PaymentApiService
    ): PaymentRepository {
        return com.pizzanat.app.data.repositories.PaymentRepositoryImpl(paymentApiService)
    }
    
    @Provides
    @Singleton
    fun provideSharedOrderStorage(): com.pizzanat.app.presentation.order.SharedOrderStorage {
        return com.pizzanat.app.presentation.order.SharedOrderStorage()
    }
} 