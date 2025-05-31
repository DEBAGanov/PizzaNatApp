/**
 * @file: UpdateOrderStatusUseCase.kt
 * @description: Use case для обновления статуса заказа администратором
 * @dependencies: AdminRepository, Order, OrderStatus
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.admin

import com.pizzanat.app.domain.entities.Order
import com.pizzanat.app.domain.entities.OrderStatus
import com.pizzanat.app.domain.repositories.AdminRepository
import javax.inject.Inject

class UpdateOrderStatusUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(orderId: Long, newStatus: OrderStatus): Result<Order> {
        if (orderId <= 0) {
            return Result.failure(Exception("Некорректный ID заказа"))
        }
        
        return try {
            adminRepository.updateOrderStatus(orderId, newStatus)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 