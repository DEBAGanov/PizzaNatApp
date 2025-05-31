/**
 * @file: ManageCategoriesUseCase.kt
 * @description: Use cases для управления категориями в админ панели
 * @dependencies: AdminRepository, Category
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.admin

import com.pizzanat.app.domain.entities.Category
import com.pizzanat.app.domain.repositories.AdminRepository
import javax.inject.Inject

class GetAllCategoriesUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(): Result<List<Category>> {
        return try {
            adminRepository.getAllCategories()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class CreateCategoryUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(category: Category): Result<Category> {
        return try {
            // Валидация данных
            if (category.name.isBlank()) {
                return Result.failure(Exception("Название категории не может быть пустым"))
            }
            if (category.description.isBlank()) {
                return Result.failure(Exception("Описание категории не может быть пустым"))
            }
            
            adminRepository.createCategory(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class UpdateCategoryUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(category: Category): Result<Category> {
        return try {
            // Валидация данных
            if (category.id <= 0) {
                return Result.failure(Exception("Неверный ID категории"))
            }
            if (category.name.isBlank()) {
                return Result.failure(Exception("Название категории не может быть пустым"))
            }
            if (category.description.isBlank()) {
                return Result.failure(Exception("Описание категории не может быть пустым"))
            }
            
            adminRepository.updateCategory(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class DeleteCategoryUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(categoryId: Long): Result<Unit> {
        return try {
            if (categoryId <= 0) {
                return Result.failure(Exception("Неверный ID категории"))
            }
            
            adminRepository.deleteCategory(categoryId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 