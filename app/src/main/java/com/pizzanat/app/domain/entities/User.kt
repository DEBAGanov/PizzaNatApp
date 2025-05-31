/**
 * @file: User.kt
 * @description: Доменная модель пользователя
 * @dependencies: Нет внешних зависимостей
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.entities

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String
) 