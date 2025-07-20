/**
 * @file: OrderMappers.kt
 * @description: Маппер функции для преобразования между Entity и Domain объектами заказов
 * @dependencies: Order, OrderEntity, OrderItemEntity
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.mappers

import com.pizzanat.app.data.local.entities.OrderEntity
import com.pizzanat.app.data.local.entities.OrderItemEntity
import com.pizzanat.app.data.remote.dto.AdminOrdersPageResponse
import com.pizzanat.app.data.remote.dto.CreateOrderRequest
import com.pizzanat.app.data.remote.dto.OrderDto
import com.pizzanat.app.data.remote.dto.OrderItemDto
import com.pizzanat.app.domain.entities.Order
import com.pizzanat.app.domain.entities.OrderItem
import com.pizzanat.app.domain.entities.OrderStatus
import com.pizzanat.app.domain.entities.DeliveryMethod
import com.pizzanat.app.domain.entities.PaymentMethod
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Преобразование OrderEntity в Order (Domain)
 */
fun OrderEntity.toDomain(orderItems: List<OrderItemEntity> = emptyList()): Order {
    return Order(
        id = id,
        userId = userId,
        items = orderItems.map { it.toDomain() },
        status = status,
        totalAmount = totalAmount,
        deliveryMethod = deliveryMethod,
        deliveryAddress = deliveryAddress,
        deliveryCost = deliveryCost,
        paymentMethod = paymentMethod,
        customerPhone = customerPhone,
        customerName = customerName,
        notes = notes,
        createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.systemDefault()),
        updatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(updatedAt), ZoneId.systemDefault()),
        estimatedDeliveryTime = estimatedDeliveryTime?.let { 
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) 
        }
    )
}

/**
 * Преобразование OrderItemEntity в OrderItem (Domain)
 */
fun OrderItemEntity.toDomain(): OrderItem {
    return OrderItem(
        id = id,
        productId = productId,
        productName = productName,
        productPrice = productPrice,
        quantity = quantity
    )
}

/**
 * Преобразование Order (Domain) в OrderEntity
 */
fun Order.toEntity(): OrderEntity {
    return OrderEntity(
        id = id,
        userId = userId,
        status = status,
        totalAmount = totalAmount,
        deliveryMethod = deliveryMethod,
        deliveryAddress = deliveryAddress,
        deliveryCost = deliveryCost,
        paymentMethod = paymentMethod,
        customerPhone = customerPhone,
        customerName = customerName,
        notes = notes,
        estimatedDeliveryTime = estimatedDeliveryTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
}

/**
 * Преобразование OrderItem в OrderItemEntity
 */
fun OrderItem.toOrderItemEntity(orderId: Long): OrderItemEntity {
    return OrderItemEntity(
        orderId = orderId,
        productId = productId,
        productName = productName,
        productPrice = productPrice,
        productImageUrl = "", // Заполним позже из CartItem
        quantity = quantity,
        selectedOptions = emptyMap() // Заполним позже из CartItem
    )
}

/**
 * Преобразование списка OrderEntity в список Order
 */
fun List<OrderEntity>.toDomain(): List<Order> {
    return map { it.toDomain() }
}

// API DTO Mappers

/**
 * Преобразование OrderDto в Order (Domain)
 */
fun OrderDto.toDomain(): Order {
    // Диагностическое логирование
    android.util.Log.d("OrderMappers", "🔄 Преобразование OrderDto в Order:")
    android.util.Log.d("OrderMappers", "  DTO.id: ${this.id}")
    android.util.Log.d("OrderMappers", "  DTO.totalAmount: ${this.totalAmount}")
    android.util.Log.d("OrderMappers", "  DTO.deliveryFee: ${this.deliveryFee}")
    android.util.Log.d("OrderMappers", "  DTO.deliveryAddress: '${this.deliveryAddress}'")
    android.util.Log.d("OrderMappers", "  DTO.contactName: '${this.contactName}'")
    android.util.Log.d("OrderMappers", "  DTO.contactPhone: '${this.contactPhone}'")
    android.util.Log.d("OrderMappers", "  DTO.items?.size: ${this.items?.size}")
    
    // 🔧 ФИКС: Восстанавливаем стоимость доставки если она 0 но есть адрес
    val actualDeliveryCost = if (this.deliveryFee == 0.0 && this.deliveryAddress.isNotBlank() && this.deliveryAddress != "Самовывоз") {
        250.0 // Стандартная стоимость доставки для Волжска
    } else {
        this.deliveryFee
    }
    
    // 🔧 НОВЫЙ ФИКС: Преобразуем товары и пересчитываем общую сумму
    val domainItems = this.items?.map { it.toDomain() } ?: emptyList()
    
    // Пересчитываем totalAmount на основе реальных цен товаров
    val calculatedTotalAmount = domainItems.sumOf { it.totalPrice }
    
    android.util.Log.d("OrderMappers", "🔧 Исправления:")
    android.util.Log.d("OrderMappers", "  Стоимость доставки: $actualDeliveryCost (было: ${this.deliveryFee})")
    android.util.Log.d("OrderMappers", "  TotalAmount из API: ${this.totalAmount}")
    android.util.Log.d("OrderMappers", "  TotalAmount пересчитанная: $calculatedTotalAmount")
    android.util.Log.d("OrderMappers", "  Разница: ${calculatedTotalAmount - this.totalAmount}")
    
    // Используем пересчитанную сумму если она сильно отличается от API
    val actualTotalAmount = if (kotlin.math.abs(calculatedTotalAmount - this.totalAmount) > 10.0) {
        android.util.Log.w("OrderMappers", "⚠️ БОЛЬШАЯ РАЗНИЦА! Используем пересчитанную сумму: $calculatedTotalAmount вместо ${this.totalAmount}")
        calculatedTotalAmount
    } else {
        this.totalAmount
    }
    
    return Order(
        id = this.id,
        userId = this.userId ?: 0L,
        items = domainItems,
        status = parseOrderStatus(this.status),
        totalAmount = actualTotalAmount, // Используем исправленную сумму
        deliveryMethod = DeliveryMethod.DELIVERY, // Backend пока не возвращает метод доставки
        deliveryAddress = this.deliveryAddress,
        deliveryCost = actualDeliveryCost, // Используем исправленную стоимость
        paymentMethod = PaymentMethod.CARD_ON_DELIVERY, // Backend НЕ возвращает paymentMethod в OrderDto
        customerPhone = this.contactPhone,
        customerName = this.contactName,
        notes = this.comment ?: "",
        createdAt = parseDateTime(this.createdAt),
        updatedAt = parseDateTime(this.updatedAt ?: this.createdAt),
        estimatedDeliveryTime = this.estimatedDeliveryTime?.let { parseDateTime(it) }
    ).also { domainOrder ->
        // Логируем результат преобразования
        android.util.Log.d("OrderMappers", "✅ Создан Domain Order:")
        android.util.Log.d("OrderMappers", "  Domain.id: ${domainOrder.id}")
        android.util.Log.d("OrderMappers", "  Domain.totalAmount: ${domainOrder.totalAmount}")
        android.util.Log.d("OrderMappers", "  Domain.deliveryCost: ${domainOrder.deliveryCost}")
        android.util.Log.d("OrderMappers", "  Domain.grandTotal: ${domainOrder.grandTotal}")
        android.util.Log.d("OrderMappers", "  Domain.items.size: ${domainOrder.items.size}")
        domainOrder.items.forEachIndexed { index, item ->
            android.util.Log.d("OrderMappers", "    Final Item ${index + 1}: ${item.productName} - ${item.quantity} × ${item.productPrice}₽ = ${item.totalPrice}₽")
        }
    }
}

/**
 * Преобразование OrderItemDto в OrderItem (Domain)
 */
fun OrderItemDto.toDomain(): OrderItem {
    // Диагностическое логирование
    android.util.Log.d("OrderItemMappers", "🔄 Преобразование OrderItemDto в OrderItem:")
    android.util.Log.d("OrderItemMappers", "  productName: '${this.productName}'")
    android.util.Log.d("OrderItemMappers", "  price: ${this.price}")
    android.util.Log.d("OrderItemMappers", "  productPrice: ${this.productPrice}")
    android.util.Log.d("OrderItemMappers", "  quantity: ${this.quantity}")
    
    // 🔧 ФИКС: Используем productPrice (реальная цена) вместо price (скидочная цена)
    val actualPrice = this.productPrice ?: this.price
    android.util.Log.d("OrderItemMappers", "  📊 Выбранная цена: $actualPrice (productPrice=${this.productPrice}, price=${this.price})")
    
    return OrderItem(
        id = this.id,
        productId = this.productId,
        productName = this.productName,
        productPrice = actualPrice, // Используем реальную цену продукта
        quantity = this.quantity
    )
}

/**
 * Преобразование AdminOrdersPageResponse в список Order (Domain)
 */
fun AdminOrdersPageResponse.toDomain(): List<Order> {
    return this.content.map { it.toDomain() }
}

/**
 * Создание CreateOrderRequest из параметров
 */
fun createOrderRequest(
    deliveryAddress: String,
    contactName: String,
    contactPhone: String,
    comment: String? = null,
    paymentMethod: String = "CASH"
): CreateOrderRequest {
    return CreateOrderRequest(
        deliveryAddress = deliveryAddress,
        contactName = contactName,
        contactPhone = contactPhone,
        comment = comment,
        paymentMethod = paymentMethod
    )
}

/**
 * Парсинг даты из строки Backend API
 */
private fun parseDateTime(dateString: String): LocalDateTime {
    return try {
        // Backend возвращает ISO 8601 формат: "2025-06-18T08:19:18.373688"
        LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    } catch (e: Exception) {
        // Fallback на текущее время
        LocalDateTime.now()
    }
}

/**
 * Парсинг статуса заказа из строки Backend API
 */
private fun parseOrderStatus(statusString: String): OrderStatus {
    return try {
        when (statusString.uppercase()) {
            "CREATED" -> OrderStatus.PENDING
            "CONFIRMED" -> OrderStatus.CONFIRMED
            "PREPARING" -> OrderStatus.PREPARING
            "READY" -> OrderStatus.READY
            "DELIVERING" -> OrderStatus.DELIVERING
            "DELIVERED" -> OrderStatus.DELIVERED
            "CANCELLED" -> OrderStatus.CANCELLED
            else -> OrderStatus.PENDING
        }
    } catch (e: Exception) {
        OrderStatus.PENDING
    }
} 