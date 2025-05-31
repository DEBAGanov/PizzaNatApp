/**
 * @file: AuthMappers.kt
 * @description: Маппер функции для преобразования DTO в доменные модели аутентификации
 * @dependencies: Domain entities, DTO classes
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.mappers

import com.pizzanat.app.data.network.dto.AuthResponseDto
import com.pizzanat.app.data.network.dto.UserDto
import com.pizzanat.app.domain.entities.AuthResponse
import com.pizzanat.app.domain.entities.User

/**
 * Преобразование UserDto в User
 */
fun UserDto.toDomain(): User {
    return User(
        id = this.id,
        username = this.username,
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        phone = this.phone
    )
}

/**
 * Генерация уникального ID на основе email
 */
private fun generateUserIdFromEmail(email: String): Long {
    return email.hashCode().toLong().let { 
        if (it < 0) -it else it // Делаем положительным числом
    }
}

/**
 * Преобразование AuthResponseDto в AuthResponse
 */
fun AuthResponseDto.toDomain(): AuthResponse {
    return AuthResponse(
        token = this.token,
        user = User(
            id = this.id ?: generateUserIdFromEmail(this.email), // Используем ID из DTO или генерируем
            username = this.username,
            email = this.email,
            firstName = this.firstName,
            lastName = this.lastName,
            phone = this.phone ?: "" // phone может быть null
        )
    )
}

/**
 * Преобразование User в UserDto
 */
fun User.toDto(): UserDto {
    return UserDto(
        id = this.id,
        username = this.username,
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        phone = this.phone
    )
} 