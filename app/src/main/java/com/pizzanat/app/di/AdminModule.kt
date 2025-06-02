/**
 * @file: AdminModule.kt
 * @description: Hilt модуль для DI админ панели с real API интеграцией
 * @dependencies: AdminRepository, AdminRepositoryImpl, Hilt
 * @created: 2024-12-19
 */
package com.pizzanat.app.di

import com.pizzanat.app.data.repositories.AdminRepositoryImpl
import com.pizzanat.app.domain.repositories.AdminRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdminModule {
    
    @Binds
    @Singleton
    abstract fun bindAdminRepository(
        adminRepositoryImpl: AdminRepositoryImpl
    ): AdminRepository
    
    // AuthApiService и TokenManager уже предоставляются NetworkModule
} 