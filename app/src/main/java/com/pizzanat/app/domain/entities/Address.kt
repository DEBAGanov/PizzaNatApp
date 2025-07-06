/**
 * @file: Address.kt
 * @description: Доменные сущности для работы с адресами и геолокацией
 * @dependencies: None
 * @created: 2025-01-23
 */
package com.pizzanat.app.domain.entities

/**
 * Подсказка адреса
 */
data class AddressSuggestion(
    val value: String,                    // Полный адрес для отображения
    val unrestricted_value: String,       // Полный адрес с почтовым индексом
    val data: AddressData                 // Структурированные данные адреса
)

/**
 * Структурированные данные адреса
 */
data class AddressData(
    val postalCode: String?,              // Почтовый индекс
    val country: String?,                 // Страна
    val region: String?,                  // Регион/область
    val city: String?,                    // Город
    val street: String?,                  // Улица
    val house: String?,                   // Номер дома
    val apartment: String?,               // Квартира/офис
    val latitude: Double?,                // Широта
    val longitude: Double?                // Долгота
)

/**
 * Результат валидации адреса
 */
data class AddressValidation(
    val isValid: Boolean,
    val message: String?,
    val suggestions: List<AddressSuggestion>?
)

/**
 * Координаты точки
 */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

/**
 * Полный адрес с координатами
 */
data class FullAddress(
    val formattedAddress: String,
    val addressData: AddressData,
    val coordinates: GeoPoint?
) 

/**
 * Простая подсказка адреса (для API backend)
 */
data class SimpleAddressSuggestion(
    val shortAddress: String,
    val fullAddress: String? = null,
    val city: String? = null,
    val street: String? = null,
    val house: String? = null,
    val coordinates: GeoPoint? = null
)

/**
 * Расчет стоимости доставки
 */
data class DeliveryEstimate(
    val deliveryCost: Double,             // Стоимость доставки в рублях
    val zoneName: String,                 // Название зоны доставки
    val isDeliveryFree: Boolean,          // Бесплатная ли доставка
    val deliveryAvailable: Boolean,       // Доступна ли доставка
    val city: String,                     // Город
    val estimatedTime: String? = null,    // Время доставки
    val minOrderAmount: Double? = null,   // Минимальная сумма заказа
    val freeDeliveryThreshold: Double? = null // Порог бесплатной доставки
)

/**
 * Зона доставки Волжск
 */
enum class VolzhskDeliveryZone(
    val zoneName: String,
    val baseCost: Double,
    val freeDeliveryThreshold: Double
) {
    DRUZHBA("Дружба", 100.0, 800.0),              // Самая дешевая зона
    CENTRAL("Центральный", 200.0, 1000.0),        // Центральная зона
    MASHINOSTROITEL("Машиностроитель", 200.0, 1000.0),
    VDK("ВДК", 200.0, 1000.0),
    NORTHERN("Северный", 200.0, 1000.0),
    GORGAZ("Горгаз", 200.0, 1000.0),
    ZARYA("Заря", 250.0, 1200.0),                 // Дороже центра
    PROMUZELL("Промузел", 300.0, 1500.0),         // Самая дорогая зона
    PRIBREZHNY("Прибрежный", 300.0, 1500.0),      // Самая дорогая зона
    STANDARD("Стандартная зона", 200.0, 1000.0);  // Fallback зона
    
    /**
     * Рассчитать стоимость доставки
     */
    fun calculateDeliveryCost(orderAmount: Double): Double {
        return if (orderAmount >= freeDeliveryThreshold) 0.0 else baseCost
    }
} 