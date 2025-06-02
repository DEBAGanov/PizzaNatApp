/**
 * @file: Product.kt
 * @description: Доменная модель продукта
 * @dependencies: Нет внешних зависимостей
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.entities

data class Product(
    val id: Long = 0,
    val name: String,
    val description: String,
    val price: Double,
    val originalPrice: Double? = null, // Оригинальная цена до скидки
    val categoryId: Long,
    val imageUrl: String,
    val available: Boolean = true,
    val specialOffer: Boolean = false,
    val discountPercent: Int? = null, // Процент скидки
    val weight: Int? = null, // Вес в граммах
    val ingredients: String? = null,
    val calories: Int? = null,
    val preparationTime: Int? = null // в минутах
) {
    /**
     * Есть ли скидка на продукт
     */
    val hasDiscount: Boolean
        get() = originalPrice != null && discountPercent != null
        
    /**
     * Размер скидки в рублях
     */
    val discountAmount: Double
        get() = if (hasDiscount && originalPrice != null) {
            originalPrice - price
        } else 0.0
} 