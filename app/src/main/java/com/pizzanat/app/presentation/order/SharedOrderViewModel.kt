/**
 * @file: SharedOrderViewModel.kt  
 * @description: Общий ViewModel для передачи данных заказа между экранами
 * @dependencies: Hilt, ViewModel
 * @created: 2024-12-25
 */
package com.pizzanat.app.presentation.order

import androidx.lifecycle.ViewModel
import com.pizzanat.app.domain.entities.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton 
class SharedOrderStorage @Inject constructor() {
    private var lastCreatedOrder: Order? = null
    
    fun saveOrder(order: Order) {
        android.util.Log.d("SharedOrderStorage", "📦 Сохраняем заказ #${order.id} с ${order.items.size} товарами")
        android.util.Log.d("SharedOrderStorage", "  Сумма товаров: ${order.totalAmount}")
        android.util.Log.d("SharedOrderStorage", "  Доставка: ${order.deliveryCost}")
        android.util.Log.d("SharedOrderStorage", "  ИТОГО: ${order.grandTotal}")
        lastCreatedOrder = order
    }
    
    fun getOrder(orderId: Long): Order? {
        val order = lastCreatedOrder
        android.util.Log.d("SharedOrderStorage", "📋 Запрос заказа #$orderId")
        android.util.Log.d("SharedOrderStorage", "  Сохраненный заказ: ${if (order != null) "#${order.id}" else "нет"}")
        
        return if (order?.id == orderId) {
            android.util.Log.d("SharedOrderStorage", "✅ Возвращаем сохраненный заказ")
            order
        } else {
            android.util.Log.d("SharedOrderStorage", "❌ ID не совпадает, возвращаем null")
            null
        }
    }
    
    fun clearOrder() {
        android.util.Log.d("SharedOrderStorage", "🗑️ Очищаем сохраненный заказ")
        lastCreatedOrder = null
    }
} 