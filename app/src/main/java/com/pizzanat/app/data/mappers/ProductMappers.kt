/**
 * @file: ProductMappers.kt
 * @description: Маппер функции для преобразования DTO в доменные модели продуктов
 * @dependencies: Domain entities, DTO classes
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.mappers

import com.pizzanat.app.data.remote.dto.CategoryDto
import com.pizzanat.app.data.remote.dto.ProductDto
import com.pizzanat.app.data.remote.dto.ProductsPageResponse
import com.pizzanat.app.domain.entities.Category
import com.pizzanat.app.domain.entities.Product

/**
 * Преобразование CategoryDto в Category
 */
fun CategoryDto.toDomain(): Category {
    return Category(
        id = this.id,
        name = this.name,
        description = this.description,
        imageUrl = this.imageUrl
    )
}

/**
 * Преобразование ProductDto в Product
 */
fun ProductDto.toDomain(): Product {
    return Product(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        discountedPrice = this.discountedPrice,
        categoryId = this.categoryId,
        imageUrl = this.imageUrl,
        available = this.available,
        specialOffer = this.specialOffer,
        discountPercent = this.discountPercent,
        weight = this.weight
    )
}

/**
 * Преобразование списка CategoryDto в список Category
 */
fun List<CategoryDto>.toCategoryDomain(): List<Category> {
    return this.map { it.toDomain() }
}

/**
 * Преобразование списка ProductDto в список Product
 */
fun List<ProductDto>.toProductDomain(): List<Product> {
    return this.map { it.toDomain() }
}

/**
 * Преобразование ProductsPageResponse в список Product
 */
fun ProductsPageResponse.toProductsDomain(): List<Product> {
    return this.content.map { it.toDomain() }
} 