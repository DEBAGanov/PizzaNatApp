/**
 * @file: AddressDto.kt
 * @description: DTO модели для работы с API адресов и доставки
 * @dependencies: None
 * @created: 2024-12-28
 */
package com.pizzanat.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO для подсказки адреса
 */
data class AddressSuggestionDto(
    @SerializedName("shortAddress")
    val shortAddress: String,
    
    @SerializedName("fullAddress")
    val fullAddress: String? = null,
    
    @SerializedName("city")
    val city: String? = null,
    
    @SerializedName("street")
    val street: String? = null,
    
    @SerializedName("house")
    val house: String? = null,
    
    @SerializedName("coordinates")
    val coordinates: CoordinatesDto? = null
)

/**
 * DTO для координат адреса
 */
data class CoordinatesDto(
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double
)

/**
 * DTO для расчета стоимости доставки
 */
data class DeliveryEstimateDto(
    @SerializedName("deliveryCost")
    val deliveryCost: String, // API возвращает строку
    
    @SerializedName("zoneName")
    val zoneName: String,
    
    @SerializedName("isDeliveryFree")
    val isDeliveryFree: Boolean,
    
    @SerializedName("deliveryAvailable")
    val deliveryAvailable: Boolean,
    
    @SerializedName("city")
    val city: String,
    
    @SerializedName("estimatedTime")
    val estimatedTime: String? = null,
    
    @SerializedName("minOrderAmount")
    val minOrderAmount: Double? = null,
    
    @SerializedName("freeDeliveryThreshold")
    val freeDeliveryThreshold: Double? = null
)

/**
 * DTO для валидации адреса
 */
data class AddressValidationDto(
    @SerializedName("isValid")
    val isValid: Boolean,
    
    @SerializedName("normalizedAddress")
    val normalizedAddress: String? = null,
    
    @SerializedName("suggestions")
    val suggestions: List<AddressSuggestionDto>? = null,
    
    @SerializedName("errorMessage")
    val errorMessage: String? = null
) 