/**
 * @file: ApiMappers.kt
 * @description: Mapper функции для преобразования API DTO в Domain модели
 * @dependencies: Domain entities, API DTOs
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.mappers

import com.pizzanat.app.data.remote.dto.*
import com.pizzanat.app.domain.entities.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Product mappers
fun ProductDto.toDomain(): Product {
    // Временное логирование для отладки
    android.util.Log.d("ProductMapper", "Mapping product: id=$id, name=$name")
    android.util.Log.d("ProductMapper", "Original imageUrl: $imageUrl")
    
    val product = Product(
        id = id,
        name = name,
        description = description,
        price = discountedPrice ?: price,
        categoryId = categoryId,
        imageUrl = imageUrl,
        available = available
    )
    
    android.util.Log.d("ProductMapper", "Mapped product imageUrl: ${product.imageUrl}")
    return product
}

fun CategoryDto.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        description = description,
        imageUrl = imageUrl
    )
}

fun List<ProductDto>.toProductDomain(): List<Product> {
    return map { it.toDomain() }
}

fun List<CategoryDto>.toCategoryDomain(): List<Category> {
    return map { it.toDomain() }
}

// Order mappers
fun OrderDto.toDomain(): Order {
    return Order(
        id = id,
        userId = userId,
        items = items.map { it.toOrderItem() },
        totalAmount = totalAmount,
        status = OrderStatus.valueOf(status.uppercase()),
        deliveryMethod = DeliveryMethod.DELIVERY, // Временное значение по умолчанию
        deliveryAddress = deliveryAddress,
        deliveryCost = deliveryFee,
        paymentMethod = PaymentMethod.CASH, // Временное значение по умолчанию
        customerPhone = contactPhone,
        customerName = contactName,
        notes = notes,
        createdAt = parseDateTime(createdAt),
        estimatedDeliveryTime = estimatedDeliveryTime?.let { parseDateTime(it) }
    )
}

fun OrderItemDto.toOrderItem(): OrderItem {
    return OrderItem(
        id = id,
        productId = productId,
        productName = productName,
        productPrice = productPrice,
        quantity = quantity
    )
}

fun List<OrderDto>.toOrderDomain(): List<Order> {
    return map { it.toDomain() }
}

// Domain to API mappers
fun CartItem.toCreateOrderItemRequest(): CreateOrderItemRequest {
    return CreateOrderItemRequest(
        productId = productId,
        quantity = quantity,
        selectedOptions = selectedOptions
    )
}

fun List<CartItem>.toCreateOrderRequest(
    deliveryAddress: String,
    contactPhone: String,
    contactName: String,
    notes: String
): CreateOrderRequest {
    return CreateOrderRequest(
        items = map { it.toCreateOrderItemRequest() },
        deliveryAddress = deliveryAddress,
        contactPhone = contactPhone,
        contactName = contactName,
        notes = notes
    )
}

fun OrderStatus.toApiString(): String {
    return name.uppercase()
}

// Utility functions
private fun parseDateTime(dateTimeString: String): LocalDateTime {
    return try {
        LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    } catch (e: Exception) {
        // Fallback для разных форматов даты
        try {
            LocalDateTime.parse(dateTimeString.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now() // Fallback значение
        }
    }
} 