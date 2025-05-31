/**
 * @file: AdminModule.kt
 * @description: Hilt модуль для DI админ панели
 * @dependencies: AdminRepository, Hilt
 * @created: 2024-12-19
 */
package com.pizzanat.app.di

import com.pizzanat.app.data.repositories.MockAdminRepositoryImpl
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
        mockAdminRepositoryImpl: MockAdminRepositoryImpl
    ): AdminRepository
} 