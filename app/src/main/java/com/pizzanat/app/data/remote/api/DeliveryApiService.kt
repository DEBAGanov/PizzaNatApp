/**
 * @file: DeliveryApiService.kt
 * @description: API интерфейс для работы с доставкой
 * @dependencies: Retrofit
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.remote.api

import com.pizzanat.app.data.remote.dto.DeliveryLocationDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DeliveryApiService {

    /**
     * Получить все пункты выдачи
     * GET /api/v1/delivery-locations
     */
    @GET("delivery-locations")
    suspend fun getDeliveryLocations(): Response<List<DeliveryLocationDto>>

    /**
     * Получить пункт доставки по ID
     * GET /api/v1/delivery-locations/{id}
     */
    @GET("delivery-locations/{id}")
    suspend fun getDeliveryLocationById(
        @Path("id") id: Long
    ): Response<DeliveryLocationDto>
}