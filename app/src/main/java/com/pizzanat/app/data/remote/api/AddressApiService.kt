/**
 * @file: AddressApiService.kt
 * @description: API сервис для работы с адресами и доставкой
 * @dependencies: AddressDto, ApiResult
 * @created: 2024-12-28
 */
package com.pizzanat.app.data.remote.api

import com.pizzanat.app.data.remote.dto.AddressSuggestionDto
import com.pizzanat.app.data.remote.dto.AddressValidationDto
import com.pizzanat.app.data.remote.dto.DeliveryEstimateDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API сервис для работы с адресами и зональной системой доставки Волжск
 */
interface AddressApiService {
    
    /**
     * Получение подсказок адресов
     * @param query Поисковый запрос (минимум 2 символа)
     * @param limit Максимальное количество результатов (по умолчанию 10)
     */
    @GET("delivery/address-suggestions")
    suspend fun getAddressSuggestions(
        @Query("query") query: String,
        @Query("limit") limit: Int = 10
    ): Response<List<AddressSuggestionDto>>
    
    /**
     * Валидация адреса
     * @param address Адрес для валидации
     */
    @GET("delivery/validate-address")
    suspend fun validateAddress(
        @Query("address") address: String
    ): Response<AddressValidationDto>
    
    /**
     * Расчет стоимости доставки по зональной системе Волжск
     * @param address Адрес доставки
     * @param orderAmount Сумма заказа для расчета бесплатной доставки
     */
    @GET("delivery/estimate")
    suspend fun getDeliveryEstimate(
        @Query("address") address: String,
        @Query("orderAmount") orderAmount: Double
    ): Response<DeliveryEstimateDto>
} 