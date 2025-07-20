/**
 * @file: GetOrderByIdUseCase.kt
 * @description: Use Case для получения заказа по ID
 * @dependencies: OrderRepository
 * @created: 2024-12-25
 */
package com.pizzanat.app.domain.usecases.order

import com.pizzanat.app.domain.entities.Order
import com.pizzanat.app.domain.repositories.OrderRepository
import javax.inject.Inject

class GetOrderByIdUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: Long): Result<Order?> {
        return try {
            orderRepository.getOrderById(orderId)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка при получении заказа #$orderId: ${e.message}"))
        }
    }
} 