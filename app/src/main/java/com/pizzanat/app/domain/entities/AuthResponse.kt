/**
 * @file: AuthResponse.kt
 * @description: Доменная модель ответа аутентификации
 * @dependencies: User entity
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.entities

data class AuthResponse(
    val token: String,
    val user: User
) 