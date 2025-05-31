/**
 * @file: Category.kt
 * @description: Доменная модель категории продуктов
 * @dependencies: Нет внешних зависимостей
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.entities

data class Category(
    val id: Long,
    val name: String,
    val description: String,
    val imageUrl: String
) 