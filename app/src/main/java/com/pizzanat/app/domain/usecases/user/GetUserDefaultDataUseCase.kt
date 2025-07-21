/**
 * @file: GetUserDefaultDataUseCase.kt
 * @description: UseCase для получения данных пользователя по умолчанию для автозаполнения формы заказа
 * @dependencies: AuthRepository, OrderRepository
 * @created: 2024-12-25
 */
package com.pizzanat.app.domain.usecases.user

import android.util.Log
import com.pizzanat.app.domain.repositories.AuthRepository
import com.pizzanat.app.domain.repositories.OrderRepository
import javax.inject.Inject

/**
 * Данные пользователя по умолчанию для автозаполнения
 */
data class UserDefaultData(
    val customerName: String = "",
    val customerPhone: String = "+7",
    val deliveryAddress: String = ""
)

/**
 * UseCase для получения данных пользователя по умолчанию из профиля, сохраненных предпочтений и последнего заказа
 */
class GetUserDefaultDataUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
    private val saveUserPreferencesUseCase: SaveUserPreferencesUseCase
) {
    
    suspend operator fun invoke(): Result<UserDefaultData> {
        return try {
            Log.d("GetUserDefaultDataUseCase", "🔄 Получение данных пользователя по умолчанию...")
            
            // Получаем данные текущего пользователя
            val currentUser = authRepository.getCurrentUser()
            Log.d("GetUserDefaultDataUseCase", "👤 Текущий пользователь: ${currentUser?.username}")
            
            var customerName = ""
            var customerPhone = "+7"
            var deliveryAddress = ""
            
            // Заполняем данные по приоритету: Сохраненные предпочтения > Профиль > История заказов
            
            // 1. Получаем сохраненные предпочтения (последние введенные данные) - высший приоритет
            Log.d("GetUserDefaultDataUseCase", "📋 Получаем сохраненные предпочтения...")
            val savedAddress = saveUserPreferencesUseCase.getLastDeliveryAddress()
            val savedPhone = saveUserPreferencesUseCase.getLastCustomerPhone() 
            val savedName = saveUserPreferencesUseCase.getLastCustomerName()
            
            Log.d("GetUserDefaultDataUseCase", "💾 Сохраненные предпочтения:")
            Log.d("GetUserDefaultDataUseCase", "  Адрес: '$savedAddress'")
            Log.d("GetUserDefaultDataUseCase", "  Телефон: '$savedPhone'")
            Log.d("GetUserDefaultDataUseCase", "  Имя: '$savedName'")
            
            // 2. Заполняем данные из профиля пользователя (если нет сохраненных)
            if (currentUser != null) {
                // Имя: предпочтения > профиль
                if (savedName.isNotBlank()) {
                    customerName = savedName
                    Log.d("GetUserDefaultDataUseCase", "📝 Используем сохраненное имя: '$customerName'")
                } else {
                    customerName = "${currentUser.firstName} ${currentUser.lastName}".trim()
                    Log.d("GetUserDefaultDataUseCase", "📝 Используем имя из профиля: '$customerName'")
                }
                
                // Телефон: предпочтения > профиль
                if (savedPhone.isNotBlank() && savedPhone != "+7") {
                    customerPhone = savedPhone
                    Log.d("GetUserDefaultDataUseCase", "📱 Используем сохраненный телефон: '$customerPhone'")
                } else if (currentUser.phone.isNotBlank() && currentUser.phone != "+7") {
                    customerPhone = currentUser.phone
                    Log.d("GetUserDefaultDataUseCase", "📱 Используем телефон из профиля: '$customerPhone'")
                }
                
                // Адрес: предпочтения > история заказов
                if (savedAddress.isNotBlank()) {
                    deliveryAddress = savedAddress
                    Log.d("GetUserDefaultDataUseCase", "🏠 Используем сохраненный адрес: '$deliveryAddress'")
                } else {
                    // 3. Fallback: получаем адрес из последнего заказа
                    Log.d("GetUserDefaultDataUseCase", "🔍 Ищем адрес в истории заказов...")
                    try {
                        val ordersResult = orderRepository.getUserOrders(currentUser.id)
                        if (ordersResult.isSuccess) {
                            val orders = ordersResult.getOrNull() ?: emptyList()
                            Log.d("GetUserDefaultDataUseCase", "📦 Найдено заказов: ${orders.size}")
                            
                            // Берем адрес из последнего заказа
                            val lastOrder = orders.firstOrNull()
                            if (lastOrder != null && lastOrder.deliveryAddress.isNotBlank()) {
                                deliveryAddress = lastOrder.deliveryAddress
                                Log.d("GetUserDefaultDataUseCase", "🏠 Используем адрес из последнего заказа #${lastOrder.id}: '$deliveryAddress'")
                            } else {
                                Log.d("GetUserDefaultDataUseCase", "⚠️ Нет предыдущих заказов с адресом")
                            }
                        } else {
                            Log.w("GetUserDefaultDataUseCase", "⚠️ Ошибка загрузки заказов: ${ordersResult.exceptionOrNull()?.message}")
                        }
                    } catch (e: Exception) {
                        Log.w("GetUserDefaultDataUseCase", "⚠️ Исключение при загрузке заказов: ${e.message}")
                        // Не критично - продолжаем без адреса
                    }
                }
            } else {
                Log.w("GetUserDefaultDataUseCase", "⚠️ Пользователь не найден")
                // Используем только сохраненные предпочтения
                customerName = savedName
                customerPhone = if (savedPhone != "+7") savedPhone else "+7"
                deliveryAddress = savedAddress
            }
            
            val result = UserDefaultData(
                customerName = customerName,
                customerPhone = customerPhone,
                deliveryAddress = deliveryAddress
            )
            
            Log.d("GetUserDefaultDataUseCase", "✅ Данные по умолчанию сформированы:")
            Log.d("GetUserDefaultDataUseCase", "  Имя: '$customerName'")
            Log.d("GetUserDefaultDataUseCase", "  Телефон: '$customerPhone'")
            Log.d("GetUserDefaultDataUseCase", "  Адрес: '$deliveryAddress'")
            
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e("GetUserDefaultDataUseCase", "❌ Ошибка получения данных по умолчанию: ${e.message}")
            
            // Возвращаем пустые значения при ошибке
            Result.success(UserDefaultData())
        }
    }
} 