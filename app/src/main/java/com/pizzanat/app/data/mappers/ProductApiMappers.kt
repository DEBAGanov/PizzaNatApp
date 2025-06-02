/**
 * @file: ProductApiMappers.kt
 * @description: Мапперы для преобразования между Product API DTO и domain entities
 * @dependencies: ProductDto, CategoryDto, Product, Category
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.mappers

import com.pizzanat.app.data.remote.dto.CategoryDto
import com.pizzanat.app.data.remote.dto.ProductDto
import com.pizzanat.app.data.remote.dto.ProductsPageResponse
import com.pizzanat.app.domain.entities.Category
import com.pizzanat.app.domain.entities.Product

/**
 * Преобразование ProductDto в Product (domain)
 */
fun ProductDto.toDomain(): Product {
    return Product(
        id = id,
        name = name,
        description = description,
        price = discountedPrice ?: price, // Используем скидочную цену если есть
        originalPrice = if (discountedPrice != null) price else null,
        categoryId = categoryId,
        imageUrl = imageUrl,
        available = available,
        specialOffer = specialOffer,
        discountPercent = discountPercent,
        weight = weight
    )
}

/**
 * Преобразование CategoryDto в Category (domain)
 */
fun CategoryDto.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        description = description,
        imageUrl = imageUrl
    )
}

/**
 * Преобразование списка ProductDto в список Product (domain)
 */
fun List<ProductDto>.toProductDomain(): List<Product> {
    return map { it.toDomain() }
}

/**
 * Преобразование списка CategoryDto в список Category (domain)
 */
fun List<CategoryDto>.toCategoryDomain(): List<Category> {
    return map { it.toDomain() }
}

/**
 * Преобразование ProductsPageResponse в список Product (domain)
 * Извлекает контент из пагинированного ответа
 */
fun ProductsPageResponse.toProductsDomain(): List<Product> {
    return content.toProductDomain()
} 