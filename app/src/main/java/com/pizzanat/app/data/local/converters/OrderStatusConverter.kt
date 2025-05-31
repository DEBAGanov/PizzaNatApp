/**
 * @file: Converters.kt
 * @description: Room TypeConverters для Order entities
 * @dependencies: Room
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.local.converters

import androidx.room.TypeConverter
import com.pizzanat.app.domain.entities.OrderStatus
import com.pizzanat.app.domain.entities.PaymentMethod
import com.pizzanat.app.domain.entities.DeliveryMethod

class OrderStatusConverter {
    @TypeConverter
    fun fromOrderStatus(status: OrderStatus): String {
        return status.name
    }

    @TypeConverter
    fun toOrderStatus(status: String): OrderStatus {
        return OrderStatus.valueOf(status)
    }
}

class PaymentMethodConverter {
    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod): String {
        return method.name
    }

    @TypeConverter
    fun toPaymentMethod(method: String): PaymentMethod {
        return PaymentMethod.valueOf(method)
    }
}

class DeliveryMethodConverter {
    @TypeConverter
    fun fromDeliveryMethod(method: DeliveryMethod): String {
        return method.name
    }

    @TypeConverter
    fun toDeliveryMethod(method: String): DeliveryMethod {
        return DeliveryMethod.valueOf(method)
    }
} 