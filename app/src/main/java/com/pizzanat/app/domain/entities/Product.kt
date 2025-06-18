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
    val discountedPrice: Double? = null, // Цена со скидкой из API
    val categoryId: Long,
    val imageUrl: String?, // Изменено на nullable для совместимости с API
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
        get() = discountedPrice != null && discountPercent != null
        
    /**
     * Размер скидки в рублях
     */
    val discountAmount: Double
        get() = if (hasDiscount && discountedPrice != null) {
            price - discountedPrice
        } else 0.0
        
    /**
     * Финальная цена (с учетом скидки)
     */
    val finalPrice: Double
        get() = discountedPrice ?: price
} 