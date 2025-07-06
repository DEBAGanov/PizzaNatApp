/**
 * @file: AddressMappers.kt
 * @description: Маппер функции для преобразования DTO адресов в доменные модели
 * @dependencies: Domain entities, DTO classes
 * @created: 2025-01-23
 */
package com.pizzanat.app.data.mappers

import com.pizzanat.app.data.remote.dto.AddressSuggestionDto
import com.pizzanat.app.data.remote.dto.AddressValidationDto
import com.pizzanat.app.data.remote.dto.DeliveryEstimateDto
import com.pizzanat.app.data.remote.dto.CoordinatesDto
import com.pizzanat.app.domain.entities.SimpleAddressSuggestion
import com.pizzanat.app.domain.entities.AddressValidation
import com.pizzanat.app.domain.entities.DeliveryEstimate
import com.pizzanat.app.domain.entities.GeoPoint
import com.pizzanat.app.domain.entities.VolzhskDeliveryZone

/**
 * Преобразование AddressSuggestionDto в SimpleAddressSuggestion (Domain)
 */
fun AddressSuggestionDto.toDomain(): SimpleAddressSuggestion {
    return SimpleAddressSuggestion(
        shortAddress = this.shortAddress,
        fullAddress = this.fullAddress,
        city = this.city,
        street = this.street,
        house = this.house,
        coordinates = this.coordinates?.toDomain()
    )
}

/**
 * Преобразование CoordinatesDto в GeoPoint (Domain)
 */
fun CoordinatesDto.toDomain(): GeoPoint {
    return GeoPoint(
        latitude = this.latitude,
        longitude = this.longitude
    )
}

/**
 * Преобразование AddressValidationDto в AddressValidation (Domain)
 */
fun AddressValidationDto.toDomain(): AddressValidation {
    return AddressValidation(
        isValid = this.isValid,
        message = this.errorMessage,
        suggestions = this.suggestions?.map { it.toOldDomain() }
    )
}

/**
 * Преобразование DeliveryEstimateDto в DeliveryEstimate (Domain)
 */
fun DeliveryEstimateDto.toDomain(): DeliveryEstimate {
    val cost = this.deliveryCost.toDoubleOrNull() ?: 0.0
    
    return DeliveryEstimate(
        deliveryCost = cost,
        zoneName = this.zoneName,
        isDeliveryFree = this.isDeliveryFree,
        deliveryAvailable = this.deliveryAvailable,
        city = this.city,
        estimatedTime = this.estimatedTime,
        minOrderAmount = this.minOrderAmount,
        freeDeliveryThreshold = this.freeDeliveryThreshold
    )
}

/**
 * Преобразование SimpleAddressSuggestion в старый формат для совместимости
 */
fun AddressSuggestionDto.toOldDomain(): com.pizzanat.app.domain.entities.AddressSuggestion {
    return com.pizzanat.app.domain.entities.AddressSuggestion(
        value = this.shortAddress,
        unrestricted_value = this.fullAddress ?: this.shortAddress,
        data = com.pizzanat.app.domain.entities.AddressData(
            postalCode = null,
            country = "Россия",
            region = extractRegionFromAddress(this.fullAddress ?: this.shortAddress),
            city = this.city ?: extractCityFromAddress(this.fullAddress ?: this.shortAddress),
            street = this.street,
            house = this.house,
            apartment = null,
            latitude = this.coordinates?.latitude,
            longitude = this.coordinates?.longitude
        )
    )
}

/**
 * Определение зоны доставки по названию
 */
fun String.toDeliveryZone(): VolzhskDeliveryZone {
    return when (this.lowercase()) {
        "дружба" -> VolzhskDeliveryZone.DRUZHBA
        "центральный" -> VolzhskDeliveryZone.CENTRAL
        "машиностроитель" -> VolzhskDeliveryZone.MASHINOSTROITEL
        "вдк" -> VolzhskDeliveryZone.VDK
        "северный" -> VolzhskDeliveryZone.NORTHERN
        "горгаз" -> VolzhskDeliveryZone.GORGAZ
        "заря" -> VolzhskDeliveryZone.ZARYA
        "промузел" -> VolzhskDeliveryZone.PROMUZELL
        "прибрежный" -> VolzhskDeliveryZone.PRIBREZHNY
        else -> VolzhskDeliveryZone.STANDARD
    }
}

/**
 * Извлечение региона из полного адреса
 */
private fun extractRegionFromAddress(address: String): String? {
    return when {
        address.contains("Республика Марий Эл", ignoreCase = true) -> "Республика Марий Эл"
        address.contains("Марий Эл", ignoreCase = true) -> "Республика Марий Эл"
        else -> null
    }
}

/**
 * Извлечение города из полного адреса
 */
private fun extractCityFromAddress(address: String): String? {
    return when {
        address.contains("Волжск", ignoreCase = true) -> "Волжск"
        address.contains("г. Волжск", ignoreCase = true) -> "Волжск"
        address.contains("город Волжск", ignoreCase = true) -> "Волжск"
        else -> null
    }
} 