/**
 * @file: GetAdminStatsUseCase.kt
 * @description: Use case для получения статистики админ панели
 * @dependencies: AdminRepository
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.admin

import com.pizzanat.app.domain.entities.AdminStats
import com.pizzanat.app.domain.repositories.AdminRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAdminStatsUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(): Result<AdminStats> {
        return try {
            adminRepository.getAdminStats()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getStatsFlow(): Flow<AdminStats> {
        return adminRepository.getAdminStatsFlow()
    }
} 