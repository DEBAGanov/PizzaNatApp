/**
 * @file: DeliveryApiService.kt
 * @description: API сервис для работы с доставкой и зональной системой Волжск
 * @dependencies: Retrofit, Backend API integration
 * @created: 2025-01-23
 * @updated: 2025-01-04 - Интеграция с зональной системой доставки
 */
package com.pizzanat.app.data.remote.api

import com.pizzanat.app.data.remote.dto.AddressSuggestionDto
import com.pizzanat.app.data.remote.dto.AddressValidationDto
import com.pizzanat.app.data.remote.dto.DeliveryEstimateDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DeliveryApiService {
    
    /**
     * Получение подсказок адресов через интеграцию с Яндекс.Картами
     * Backend проксирует запросы к Yandex Geocoder API
     */
    @GET("delivery/address-suggestions")
    suspend fun getAddressSuggestions(
        @Query("query") query: String,
        @Query("limit") limit: Int = 10
    ): Response<List<AddressSuggestionDto>>
    
    /**
     * Валидация адреса доставки
     */
    @GET("delivery/validate-address")
    suspend fun validateDeliveryAddress(
        @Query("address") address: String
    ): Response<AddressValidationDto>
    
    /**
     * Расчет стоимости и времени доставки с учетом зональной системы Волжск
     * @param address Адрес доставки
     * @param orderAmount Сумма заказа для расчета бесплатной доставки
     */
    @GET("delivery/estimate")
    suspend fun getDeliveryEstimate(
        @Query("address") address: String,
        @Query("orderAmount") orderAmount: Double
    ): Response<DeliveryEstimateDto>
}