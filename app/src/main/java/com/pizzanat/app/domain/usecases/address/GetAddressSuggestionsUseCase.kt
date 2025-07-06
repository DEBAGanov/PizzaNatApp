/**
 * @file: GetAddressSuggestionsUseCase.kt
 * @description: Use case для получения подсказок адресов с поддержкой зональной системы
 * @dependencies: AddressRepository, Domain entities
 * @created: 2025-01-23
 */
package com.pizzanat.app.domain.usecases.address

import com.pizzanat.app.domain.entities.SimpleAddressSuggestion
import com.pizzanat.app.domain.repositories.AddressRepository
import com.pizzanat.app.data.repositories.AddressRepositoryImpl
import javax.inject.Inject

/**
 * Use case для получения подсказок адресов
 */
class GetAddressSuggestionsUseCase @Inject constructor(
    private val addressRepository: AddressRepositoryImpl // Используем конкретную реализацию
) {
    
    /**
     * Получение подсказок адресов (новый формат)
     * @param query Поисковый запрос (минимум 2 символа)
     * @param limit Максимальное количество результатов
     */
    suspend operator fun invoke(
        query: String,
        limit: Int = 10
    ): Result<List<SimpleAddressSuggestion>> {
        return try {
            if (query.isBlank() || query.length < 2) {
                Result.success(emptyList())
            } else {
                addressRepository.getSimpleAddressSuggestions(query, limit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 