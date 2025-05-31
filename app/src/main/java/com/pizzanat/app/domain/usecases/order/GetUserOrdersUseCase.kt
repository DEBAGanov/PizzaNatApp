/**
 * @file: GetUserOrdersUseCase.kt
 * @description: Use Case для получения заказов пользователя
 * @dependencies: OrderRepository
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.order

import com.pizzanat.app.domain.entities.Order
import com.pizzanat.app.domain.repositories.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserOrdersUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    operator fun invoke(userId: Long): Flow<List<Order>> {
        return orderRepository.getUserOrdersFlow(userId)
    }
    
    suspend fun getUserOrders(userId: Long): Result<List<Order>> {
        return try {
            orderRepository.getUserOrders(userId)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка при получении заказов: ${e.message}"))
        }
    }
} 