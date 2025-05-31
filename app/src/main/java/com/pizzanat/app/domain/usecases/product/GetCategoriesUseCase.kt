/**
 * @file: GetCategoriesUseCase.kt
 * @description: Use Case для получения списка категорий продуктов
 * @dependencies: ProductRepository, Category entity
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.product

import com.pizzanat.app.domain.entities.Category
import com.pizzanat.app.domain.repositories.ProductRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(): Result<List<Category>> {
        return try {
            productRepository.getCategories()
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка при получении категорий: ${e.message}"))
        }
    }
} 