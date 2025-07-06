/**
 * @file: CreateOrderUseCase.kt
 * @description: Use Case для создания нового заказа
 * @dependencies: OrderRepository, CartRepository
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.usecases.order

import com.pizzanat.app.domain.entities.DeliveryMethod
import com.pizzanat.app.domain.entities.PaymentMethod
import com.pizzanat.app.domain.repositories.CartRepository
import com.pizzanat.app.domain.repositories.OrderRepository
import javax.inject.Inject

class CreateOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(
        userId: Long,
        deliveryAddress: String,
        customerPhone: String,
        customerName: String,
        notes: String = "",
        paymentMethod: PaymentMethod = PaymentMethod.CARD_ON_DELIVERY,
        deliveryMethod: DeliveryMethod = DeliveryMethod.DELIVERY
    ): Result<Long> {
        return try {
            // Получаем товары из корзины
            val cartItemsResult = cartRepository.getCartItems()
            if (cartItemsResult.isFailure) {
                return Result.failure(Exception("Не удалось получить товары из корзины"))
            }
            
            val cartItems = cartItemsResult.getOrNull() ?: emptyList()
            if (cartItems.isEmpty()) {
                return Result.failure(Exception("Корзина пуста"))
            }
            
            // Валидация данных
            if (deliveryAddress.isBlank()) {
                return Result.failure(Exception("Адрес доставки обязателен"))
            }
            
            if (customerPhone.isBlank()) {
                return Result.failure(Exception("Номер телефона обязателен"))
            }
            
            if (customerName.isBlank()) {
                return Result.failure(Exception("Имя получателя обязательно"))
            }
            
            // Создаем заказ
            val result = orderRepository.createOrder(
                userId = userId,
                cartItems = cartItems,
                deliveryAddress = deliveryAddress,
                customerPhone = customerPhone,
                customerName = customerName,
                notes = notes,
                paymentMethod = paymentMethod,
                deliveryMethod = deliveryMethod
            )
            
            // Если заказ создан успешно, очищаем корзину
            if (result.isSuccess) {
                cartRepository.clearCart()
            }
            
            result
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка при создании заказа: ${e.message}"))
        }
    }
} 