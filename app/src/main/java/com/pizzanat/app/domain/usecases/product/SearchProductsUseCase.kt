/**
 * @file: SearchProductsUseCase.kt
 * @description: Use Case для поиска продуктов по текстовому запросу
 * @dependencies: ProductRepository, Product entity
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.product

import com.pizzanat.app.domain.entities.Product
import com.pizzanat.app.domain.repositories.ProductRepository
import javax.inject.Inject

class SearchProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(
        query: String,
        page: Int = 0,
        size: Int = 20
    ): Result<List<Product>> {
        return try {
            if (query.isBlank()) {
                Result.success(emptyList())
            } else {
                productRepository.searchProducts(query, page, size)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка при поиске продуктов: ${e.message}"))
        }
    }
} 