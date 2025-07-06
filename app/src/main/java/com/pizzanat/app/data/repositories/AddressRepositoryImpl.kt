/**
 * @file: AddressRepositoryImpl.kt
 * @description: Реализация репозитория для работы с адресами через Backend API
 * @dependencies: AddressApiService, Mappers
 * @created: 2025-01-23
 */
package com.pizzanat.app.data.repositories

import android.util.Log
import com.pizzanat.app.data.mappers.toDomain
import com.pizzanat.app.data.mappers.toOldDomain
import com.pizzanat.app.data.remote.api.AddressApiService
import com.pizzanat.app.domain.entities.AddressSuggestion
import com.pizzanat.app.domain.entities.SimpleAddressSuggestion
import com.pizzanat.app.domain.entities.AddressValidation
import com.pizzanat.app.domain.entities.DeliveryEstimate
import com.pizzanat.app.domain.repositories.AddressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepositoryImpl @Inject constructor(
    private val addressApiService: AddressApiService
) : AddressRepository {

    companion object {
        private const val TAG = "AddressRepository"
        private const val MIN_QUERY_LENGTH = 2 // Минимум 2 символа по тестам
    }

    /**
     * Получение подсказок адресов (новый метод)
     */
    suspend fun getSimpleAddressSuggestions(
        query: String,
        limit: Int = 10
    ): Result<List<SimpleAddressSuggestion>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Запрос подсказок адресов для: '$query' (лимит: $limit)")
            
            if (query.isBlank() || query.length < MIN_QUERY_LENGTH) {
                Log.d(TAG, "⚠️ Запрос слишком короткий: ${query.length} символов (минимум $MIN_QUERY_LENGTH)")
                return@withContext Result.success(emptyList())
            }

            val response = addressApiService.getAddressSuggestions(
                query = query.trim(),
                limit = limit
            )

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    val suggestions = responseBody.map { it.toDomain() }
                    Log.d(TAG, "✅ Получено ${suggestions.size} подсказок адресов")
                    suggestions.forEachIndexed { index, suggestion ->
                        Log.d(TAG, "  $index: ${suggestion.shortAddress}")
                    }
                    Result.success(suggestions)
                } else {
                    Log.w(TAG, "⚠️ Пустой ответ от сервера")
                    Result.success(emptyList())
                }
            } else {
                val errorMessage = "Ошибка получения подсказок адресов: ${response.code()}"
                Log.e(TAG, "❌ $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMsg = "HTTP ошибка при получении подсказок адресов: ${e.message}"
            Log.e(TAG, "💥 $errorMsg")
            Result.failure(Exception(errorMsg))
        } catch (e: IOException) {
            val errorMsg = "Проблема с соединением при получении подсказок адресов"
            Log.e(TAG, "🌐 $errorMsg")
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            val errorMsg = "Неожиданная ошибка при получении подсказок адресов: ${e.message}"
            Log.e(TAG, "💥 $errorMsg")
            Result.failure(Exception(errorMsg))
        }
    }

    override suspend fun getAddressSuggestions(
        query: String,
        limit: Int
    ): Result<List<AddressSuggestion>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Запрос подсказок адресов (старый формат) для: '$query'")
            
            if (query.isBlank() || query.length < MIN_QUERY_LENGTH) {
                Log.d(TAG, "⚠️ Запрос слишком короткий: ${query.length} символов")
                return@withContext Result.success(emptyList())
            }

            val response = addressApiService.getAddressSuggestions(
                query = query.trim(),
                limit = limit
            )

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    val suggestions = responseBody.map { it.toOldDomain() }
                    Log.d(TAG, "✅ Получено ${suggestions.size} подсказок адресов (старый формат)")
                    Result.success(suggestions)
                } else {
                    Log.w(TAG, "⚠️ Пустой ответ от сервера")
                    Result.success(emptyList())
                }
            } else {
                val errorMessage = "Ошибка получения подсказок адресов: ${response.code()}"
                Log.e(TAG, "❌ $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            val errorMsg = "Ошибка при получении подсказок адресов: ${e.message}"
            Log.e(TAG, "💥 $errorMsg")
            Result.failure(Exception(errorMsg))
        }
    }

    override suspend fun validateDeliveryAddress(
        address: String
    ): Result<AddressValidation> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Валидация адреса: '$address'")

            val response = addressApiService.validateAddress(address.trim())

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    val validation = responseBody.toDomain()
                    Log.d(TAG, "✅ Валидация завершена: isValid=${validation.isValid}")
                    if (validation.message != null) {
                        Log.d(TAG, "  Сообщение: ${validation.message}")
                    }
                    Result.success(validation)
                } else {
                    Log.w(TAG, "⚠️ Пустой ответ при валидации адреса")
                    Result.failure(Exception("Пустой ответ при валидации адреса"))
                }
            } else {
                val errorMessage = "Ошибка валидации адреса: ${response.code()}"
                Log.e(TAG, "❌ $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            val errorMsg = "Ошибка при валидации адреса: ${e.message}"
            Log.e(TAG, "💥 $errorMsg")
            Result.failure(Exception(errorMsg))
        }
    }

    override suspend fun getDeliveryEstimate(
        address: String
    ): Result<DeliveryEstimate> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Расчет доставки для адреса: '$address' (без суммы заказа)")

            // Используем стандартную сумму 500 рублей для расчета базовой стоимости
            return@withContext getDeliveryEstimateWithAmount(address, 500.0)
        } catch (e: Exception) {
            val errorMsg = "Ошибка при расчете доставки: ${e.message}"
            Log.e(TAG, "💥 $errorMsg")
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * Расчет стоимости доставки с учетом суммы заказа (зональная система Волжск)
     */
    suspend fun getDeliveryEstimateWithAmount(
        address: String,
        orderAmount: Double
    ): Result<DeliveryEstimate> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Расчет доставки для адреса: '$address', сумма заказа: $orderAmount ₽")

            val response = addressApiService.getDeliveryEstimate(
                address = address.trim(),
                orderAmount = orderAmount
            )

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    val estimate = responseBody.toDomain()
                    Log.d(TAG, "✅ Расчет доставки завершен:")
                    Log.d(TAG, "  Зона: ${estimate.zoneName}")
                    Log.d(TAG, "  Доступна доставка: ${estimate.deliveryAvailable}")
                    Log.d(TAG, "  Стоимость: ${estimate.deliveryCost} ₽")
                    Log.d(TAG, "  Бесплатная: ${estimate.isDeliveryFree}")
                    Log.d(TAG, "  Время: ${estimate.estimatedTime}")
                    Result.success(estimate)
                } else {
                    Log.w(TAG, "⚠️ Пустой ответ при расчете доставки")
                    Result.failure(Exception("Пустой ответ при расчете доставки"))
                }
            } else {
                val errorMessage = "Ошибка расчета доставки: ${response.code()}"
                Log.e(TAG, "❌ $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMsg = "HTTP ошибка при расчете доставки: ${e.message}"
            Log.e(TAG, "💥 $errorMsg")
            Result.failure(Exception(errorMsg))
        } catch (e: IOException) {
            val errorMsg = "Проблема с соединением при расчете доставки"
            Log.e(TAG, "🌐 $errorMsg")
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            val errorMsg = "Неожиданная ошибка при расчете доставки: ${e.message}"
            Log.e(TAG, "💥 $errorMsg")
            Result.failure(Exception(errorMsg))
        }
    }
} 