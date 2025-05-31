/**
 * @file: ManageProductsUseCase.kt
 * @description: Use cases для управления продуктами в админ панели
 * @dependencies: AdminRepository, Product
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.admin

import com.pizzanat.app.domain.entities.Product
import com.pizzanat.app.domain.repositories.AdminRepository
import javax.inject.Inject

class GetAllProductsUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(): Result<List<Product>> {
        return try {
            adminRepository.getAllProducts()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class CreateProductUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(product: Product): Result<Product> {
        return try {
            // Валидация данных
            if (product.name.isBlank()) {
                return Result.failure(Exception("Название продукта не может быть пустым"))
            }
            if (product.price <= 0) {
                return Result.failure(Exception("Цена должна быть больше нуля"))
            }
            if (product.categoryId <= 0) {
                return Result.failure(Exception("Необходимо выбрать категорию"))
            }
            
            adminRepository.createProduct(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class UpdateProductUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(product: Product): Result<Product> {
        return try {
            // Валидация данных
            if (product.id <= 0) {
                return Result.failure(Exception("Неверный ID продукта"))
            }
            if (product.name.isBlank()) {
                return Result.failure(Exception("Название продукта не может быть пустым"))
            }
            if (product.price <= 0) {
                return Result.failure(Exception("Цена должна быть больше нуля"))
            }
            if (product.categoryId <= 0) {
                return Result.failure(Exception("Необходимо выбрать категорию"))
            }
            
            adminRepository.updateProduct(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class DeleteProductUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(productId: Long): Result<Unit> {
        return try {
            if (productId <= 0) {
                return Result.failure(Exception("Неверный ID продукта"))
            }
            
            adminRepository.deleteProduct(productId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 