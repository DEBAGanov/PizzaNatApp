/**
 * @file: ProductMappers.kt
 * @description: Маппер функции для преобразования DTO в доменные модели продуктов
 * @dependencies: Domain entities, DTO classes
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.mappers

import com.pizzanat.app.data.network.dto.CategoryDto
import com.pizzanat.app.data.network.dto.ProductDto
import com.pizzanat.app.domain.entities.Category
import com.pizzanat.app.domain.entities.Product

/**
 * Преобразование CategoryDto в Category
 */
fun CategoryDto.toDomain(): Category {
    return Category(
        id = this.id,
        name = this.name,
        description = this.description ?: "",
        imageUrl = this.imageUrl ?: ""
    )
}

/**
 * Преобразование ProductDto в Product
 */
fun ProductDto.toDomain(): Product {
    return Product(
        id = this.id,
        name = this.name,
        description = this.description ?: "",
        price = this.price,
        imageUrl = this.imageUrl ?: "",
        categoryId = this.categoryId,
        available = this.available
    )
} 