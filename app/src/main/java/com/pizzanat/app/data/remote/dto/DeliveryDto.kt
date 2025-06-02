/**
 * @file: DeliveryDto.kt
 * @description: DTO классы для работы с доставкой
 * @dependencies: Gson
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO для пункта выдачи
 */
data class DeliveryLocationDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("workingHours")
    val workingHours: String,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("isActive")
    val isActive: Boolean
) 