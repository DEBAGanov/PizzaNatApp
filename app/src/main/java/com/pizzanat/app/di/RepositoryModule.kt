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
        
        return if (BuildConfig.USE_MOCK_DATA && BuildConfig.DEBUG) {
            // Debug режим с mock данными для разработки UI
            MockAuthRepositoryImpl(tokenManager, userManager)
        } else {
            // Production/Staging или Debug с реальным API для тестирования интеграции
            AuthRepositoryImpl(authApiService, tokenManager, userManager)
        }
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
        return MockOrderRepositoryImpl()
    }
    
    @Provides
    @Singleton
    fun provideCartRepository(
        cartDao: CartDao,
        cartApiService: CartApiService
    ): CartRepository {
        return CartRepositoryImpl(cartDao, cartApiService)
    }
    
    @Provides
    @Singleton
    fun provideNotificationRepository(
        notificationDao: NotificationDao,
        dataStore: DataStore<Preferences>
    ): NotificationRepository {
        return MockNotificationRepositoryImpl(notificationDao, dataStore)
    }
} 