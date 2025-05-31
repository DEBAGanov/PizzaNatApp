/**
 * @file: GetProductByIdUseCase.kt
 * @description: Use Case для получения детальной информации о продукте по ID
 * @dependencies: ProductRepository, Product entity
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.product

import com.pizzanat.app.domain.entities.Product
import com.pizzanat.app.domain.repositories.ProductRepository
import javax.inject.Inject

class GetProductByIdUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(productId: Long): Result<Product> {
        return try {
            productRepository.getProductById(productId)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка при получении продукта: ${e.message}"))
        }
    }
} 