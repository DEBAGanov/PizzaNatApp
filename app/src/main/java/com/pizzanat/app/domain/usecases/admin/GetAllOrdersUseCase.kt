/**
 * @file: GetAllOrdersUseCase.kt
 * @description: Use case для получения всех заказов для админ панели
 * @dependencies: AdminRepository, Order
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.admin

import com.pizzanat.app.domain.entities.Order
import com.pizzanat.app.domain.entities.OrderStatus
import com.pizzanat.app.domain.repositories.AdminRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllOrdersUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(page: Int = 0, size: Int = 20): Result<List<Order>> {
        return try {
            adminRepository.getAllOrders(page, size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getAllOrdersFlow(): Flow<List<Order>> {
        return adminRepository.getAllOrdersFlow()
    }
    
    suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>> {
        return try {
            adminRepository.getOrdersByStatus(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 