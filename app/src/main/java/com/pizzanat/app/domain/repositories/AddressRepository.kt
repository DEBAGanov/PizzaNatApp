/**
 * @file: AddressRepository.kt
 * @description: Интерфейс репозитория для работы с адресами и геолокацией
 * @dependencies: Domain entities
 * @created: 2025-01-23
 */
package com.pizzanat.app.domain.repositories

import com.pizzanat.app.domain.entities.AddressSuggestion
import com.pizzanat.app.domain.entities.SimpleAddressSuggestion
import com.pizzanat.app.domain.entities.AddressValidation
import com.pizzanat.app.domain.entities.DeliveryEstimate

interface AddressRepository {
    
    /**
     * Получение подсказок адресов на основе введенного текста (старый формат)
     * @param query Введенный пользователем текст
     * @param limit Максимальное количество подсказок
     * @return Список подсказок адресов
     */
    suspend fun getAddressSuggestions(
        query: String,
        limit: Int = 10
    ): Result<List<AddressSuggestion>>
    
    /**
     * Валидация адреса доставки
     * @param address Адрес для валидации
     * @return Результат валидации с возможными исправлениями
     */
    suspend fun validateDeliveryAddress(
        address: String
    ): Result<AddressValidation>
    
    /**
     * Расчет стоимости и времени доставки по адресу
     * @param address Адрес доставки
     * @return Оценка доставки (время, стоимость, возможность доставки)
     */
    suspend fun getDeliveryEstimate(
        address: String
    ): Result<DeliveryEstimate>
} 