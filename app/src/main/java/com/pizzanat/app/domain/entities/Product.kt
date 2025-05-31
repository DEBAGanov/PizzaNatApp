/**
 * @file: Product.kt
 * @description: Доменная модель продукта
 * @dependencies: Нет внешних зависимостей
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.entities

data class Product(
    val id: Long,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val categoryId: Long,
    val available: Boolean
) 