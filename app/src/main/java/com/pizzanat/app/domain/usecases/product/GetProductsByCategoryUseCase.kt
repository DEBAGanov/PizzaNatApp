/**
 * @file: GetProductsByCategoryUseCase.kt
 * @description: Use Case для получения списка продуктов по категории с пагинацией
 * @dependencies: ProductRepository, Product entity
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.product

import com.pizzanat.app.domain.entities.Product
import com.pizzanat.app.domain.repositories.ProductRepository
import javax.inject.Inject

class GetProductsByCategoryUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(
        categoryId: Long,
        page: Int = 0,
        size: Int = 20
    ): Result<List<Product>> {
        return try {
            productRepository.getProductsByCategory(categoryId, page, size)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка при получении продуктов: ${e.message}"))
        }
    }
} 