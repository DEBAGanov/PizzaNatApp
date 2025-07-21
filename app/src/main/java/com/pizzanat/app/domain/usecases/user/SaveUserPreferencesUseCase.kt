/**
 * @file: SaveUserPreferencesUseCase.kt
 * @description: UseCase для сохранения пользовательских предпочтений (последние введенные данные для заказов)
 * @dependencies: DataStore
 * @created: 2024-12-25
 */
package com.pizzanat.app.domain.usecases.user

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * UseCase для сохранения и получения пользовательских предпочтений для заказов
 */
class SaveUserPreferencesUseCase @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    
    companion object {
        private val LAST_DELIVERY_ADDRESS_KEY = stringPreferencesKey("last_delivery_address")
        private val LAST_CUSTOMER_PHONE_KEY = stringPreferencesKey("last_customer_phone")
        private val LAST_CUSTOMER_NAME_KEY = stringPreferencesKey("last_customer_name")
    }
    
    /**
     * Сохранение последнего использованного адреса доставки
     */
    suspend fun saveLastDeliveryAddress(address: String) {
        if (address.isNotBlank()) {
            Log.d("SaveUserPreferences", "💾 Сохраняем последний адрес: '$address'")
            dataStore.edit { preferences ->
                preferences[LAST_DELIVERY_ADDRESS_KEY] = address
            }
        }
    }
    
    /**
     * Сохранение последнего использованного телефона
     */
    suspend fun saveLastCustomerPhone(phone: String) {
        if (phone.isNotBlank() && phone != "+7") {
            Log.d("SaveUserPreferences", "💾 Сохраняем последний телефон: '$phone'")
            dataStore.edit { preferences ->
                preferences[LAST_CUSTOMER_PHONE_KEY] = phone
            }
        }
    }
    
    /**
     * Сохранение последнего использованного имени
     */
    suspend fun saveLastCustomerName(name: String) {
        if (name.isNotBlank()) {
            Log.d("SaveUserPreferences", "💾 Сохраняем последнее имя: '$name'")
            dataStore.edit { preferences ->
                preferences[LAST_CUSTOMER_NAME_KEY] = name
            }
        }
    }
    
    /**
     * Сохранение всех данных заказа одновременно (вызывается при успешном заказе)
     */
    suspend fun saveOrderData(deliveryAddress: String, customerPhone: String, customerName: String) {
        Log.d("SaveUserPreferences", "💾 Сохраняем данные последнего заказа:")
        Log.d("SaveUserPreferences", "  Адрес: '$deliveryAddress'")
        Log.d("SaveUserPreferences", "  Телефон: '$customerPhone'")
        Log.d("SaveUserPreferences", "  Имя: '$customerName'")
        
        dataStore.edit { preferences ->
            if (deliveryAddress.isNotBlank()) {
                preferences[LAST_DELIVERY_ADDRESS_KEY] = deliveryAddress
            }
            if (customerPhone.isNotBlank() && customerPhone != "+7") {
                preferences[LAST_CUSTOMER_PHONE_KEY] = customerPhone
            }
            if (customerName.isNotBlank()) {
                preferences[LAST_CUSTOMER_NAME_KEY] = customerName
            }
        }
        
        Log.d("SaveUserPreferences", "✅ Данные последнего заказа сохранены")
    }
    
    /**
     * Получение последнего использованного адреса
     */
    suspend fun getLastDeliveryAddress(): String {
        val address = dataStore.data.first()[LAST_DELIVERY_ADDRESS_KEY] ?: ""
        Log.d("SaveUserPreferences", "📋 Последний адрес: '$address'")
        return address
    }
    
    /**
     * Получение последнего использованного телефона
     */
    suspend fun getLastCustomerPhone(): String {
        val phone = dataStore.data.first()[LAST_CUSTOMER_PHONE_KEY] ?: "+7"
        Log.d("SaveUserPreferences", "📋 Последний телефон: '$phone'")
        return phone
    }
    
    /**
     * Получение последнего использованного имени
     */
    suspend fun getLastCustomerName(): String {
        val name = dataStore.data.first()[LAST_CUSTOMER_NAME_KEY] ?: ""
        Log.d("SaveUserPreferences", "📋 Последнее имя: '$name'")
        return name
    }
} 