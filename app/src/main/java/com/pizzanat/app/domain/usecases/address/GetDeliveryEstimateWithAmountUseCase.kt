/**
 * @file: GetDeliveryEstimateWithAmountUseCase.kt
 * @description: Use case для расчета стоимости доставки с учетом суммы заказа (зональная система Волжск)
 * @dependencies: AddressRepositoryImpl, DeliveryEstimate
 * @created: 2024-12-28
 */
package com.pizzanat.app.domain.usecases.address

import com.pizzanat.app.domain.entities.DeliveryEstimate
import com.pizzanat.app.data.repositories.AddressRepositoryImpl
import javax.inject.Inject

/**
 * Use case для расчета стоимости доставки с учетом суммы заказа
 * Поддерживает зональную систему доставки города Волжск
 */
class GetDeliveryEstimateWithAmountUseCase @Inject constructor(
    private val addressRepository: AddressRepositoryImpl
) {
    
    /**
     * Расчет стоимости доставки с учетом суммы заказа
     * @param address Адрес доставки
     * @param orderAmount Сумма заказа для расчета бесплатной доставки
     * @return Детальная информация о доставке с зональным тарифом
     */
    suspend operator fun invoke(
        address: String,
        orderAmount: Double
    ): Result<DeliveryEstimate> {
        return try {
            if (address.isBlank()) {
                Result.failure(IllegalArgumentException("Адрес не может быть пустым"))
            } else if (orderAmount < 0) {
                Result.failure(IllegalArgumentException("Сумма заказа не может быть отрицательной"))
            } else {
                addressRepository.getDeliveryEstimateWithAmount(
                    address = address.trim(),
                    orderAmount = orderAmount
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 