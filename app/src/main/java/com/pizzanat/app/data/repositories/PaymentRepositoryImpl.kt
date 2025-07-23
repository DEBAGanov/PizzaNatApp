/**
 * @file: PaymentRepositoryImpl.kt
 * @description: Реализация репозитория для работы с платежами ЮКасса
 * @dependencies: PaymentApiService, PaymentRepository
 * @created: 2025-01-23
 */
package com.pizzanat.app.data.repositories

import android.util.Log
import com.pizzanat.app.data.remote.api.PaymentApiService
import com.pizzanat.app.data.remote.api.ConfirmPaymentRequestDto
import com.pizzanat.app.data.mappers.toDomain
import com.pizzanat.app.data.mappers.toDto
import com.pizzanat.app.domain.entities.PaymentInfo
import com.pizzanat.app.domain.entities.CreatePaymentRequest
import com.pizzanat.app.domain.repositories.PaymentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val paymentApiService: PaymentApiService
) : PaymentRepository {
    
    companion object {
        private const val TAG = "PaymentRepository"
    }
    
    override suspend fun createPayment(request: CreatePaymentRequest): Result<PaymentInfo> = 
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Создание платежа: ${request.description}")
                Log.d(TAG, "🔄 Данные для DTO: orderId=${request.orderId}, amount=${request.amount}, method=${request.paymentMethod}")
                
                val requestDto = request.toDto()
                Log.d(TAG, "🔄 DTO запроса: $requestDto")
                
                val response = paymentApiService.createPayment(requestDto)
                
                if (response.isSuccessful) {
                    val paymentDto = response.body()
                    if (paymentDto != null) {
                        Log.d(TAG, "✅ Платеж создан: ${paymentDto.id}")
                        Result.success(paymentDto.toDomain())
                    } else {
                        Log.e(TAG, "❌ Пустой ответ при создании платежа")
                        Result.failure(Exception("Пустой ответ сервера"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val httpCode = response.code()
                    val errorMsg = "Ошибка создания платежа: HTTP $httpCode"
                    
                    Log.e(TAG, "❌ $errorMsg")
                    Log.e(TAG, "❌ Тело ошибки: $errorBody")
                    
                    // 🆕 Graceful fallback для HTTP 500 согласно памяти
                    if (httpCode == 500) {
                        Log.w(TAG, "🔄 HTTP 500 обнаружен - применяем graceful fallback")
                        // Возвращаем специальный код для PaymentViewModel
                        Result.failure(Exception("PAYMENT_SERVER_ERROR_500"))
                    } else {
                        Result.failure(Exception("$errorMsg: $errorBody"))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Исключение при создании платежа", e)
                
                // 🆕 Дополнительная обработка сетевых ошибок как потенциальный HTTP 500
                if (e.message?.contains("500") == true || e.message?.contains("Internal Server Error") == true) {
                    Log.w(TAG, "🔄 Обнаружена сетевая ошибка 500 - применяем graceful fallback")
                    Result.failure(Exception("PAYMENT_SERVER_ERROR_500"))
                } else {
                    Result.failure(e)
                }
            }
        }
    
    override suspend fun getPayment(paymentId: String): Result<PaymentInfo> = 
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔍 Получение платежа: $paymentId")
                
                val response = paymentApiService.getPayment(paymentId)
                
                if (response.isSuccessful) {
                    val paymentDto = response.body()
                    if (paymentDto != null) {
                        Log.d(TAG, "✅ Платеж получен: ${paymentDto.id}")
                        Result.success(paymentDto.toDomain())
                    } else {
                        Log.e(TAG, "❌ Пустой ответ при получении платежа")
                        Result.failure(Exception("Пустой ответ сервера"))
                    }
                } else {
                    val errorMsg = "Ошибка получения платежа: ${response.code()}"
                    Log.e(TAG, "❌ $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Исключение при получении платежа", e)
                Result.failure(e)
            }
        }
    
    override suspend fun confirmPayment(paymentId: String, paymentToken: String): Result<PaymentInfo> = 
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "✅ Подтверждение платежа: $paymentId")
                
                val response = paymentApiService.confirmPayment(
                    paymentId = paymentId,
                    request = ConfirmPaymentRequestDto(paymentToken)
                )
                
                if (response.isSuccessful) {
                    val paymentDto = response.body()
                    if (paymentDto != null) {
                        Log.d(TAG, "✅ Платеж подтвержден: ${paymentDto.id}")
                        Result.success(paymentDto.toDomain())
                    } else {
                        Log.e(TAG, "❌ Пустой ответ при подтверждении платежа")
                        Result.failure(Exception("Пустой ответ сервера"))
                    }
                } else {
                    val errorMsg = "Ошибка подтверждения платежа: ${response.code()}"
                    Log.e(TAG, "❌ $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Исключение при подтверждении платежа", e)
                Result.failure(e)
            }
        }
    
    override suspend fun cancelPayment(paymentId: String): Result<PaymentInfo> = 
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "❌ Отмена платежа: $paymentId")
                
                val response = paymentApiService.cancelPayment(paymentId)
                
                if (response.isSuccessful) {
                    val paymentDto = response.body()
                    if (paymentDto != null) {
                        Log.d(TAG, "✅ Платеж отменен: ${paymentDto.id}")
                        Result.success(paymentDto.toDomain())
                    } else {
                        Log.e(TAG, "❌ Пустой ответ при отмене платежа")
                        Result.failure(Exception("Пустой ответ сервера"))
                    }
                } else {
                    val errorMsg = "Ошибка отмены платежа: ${response.code()}"
                    Log.e(TAG, "❌ $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Исключение при отмене платежа", e)
                Result.failure(e)
            }
        }
    
    override suspend fun getOrderPayments(orderId: Long): Result<List<PaymentInfo>> = 
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📋 Получение платежей заказа: $orderId")
                
                val response = paymentApiService.getOrderPayments(orderId)
                
                if (response.isSuccessful) {
                    val paymentsDto = response.body()
                    if (paymentsDto != null) {
                        Log.d(TAG, "✅ Получено платежей: ${paymentsDto.size}")
                        Result.success(paymentsDto.map { it.toDomain() })
                    } else {
                        Log.e(TAG, "❌ Пустой ответ при получении платежей заказа")
                        Result.failure(Exception("Пустой ответ сервера"))
                    }
                } else {
                    val errorMsg = "Ошибка получения платежей заказа: ${response.code()}"
                    Log.e(TAG, "❌ $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Исключение при получении платежей заказа", e)
                Result.failure(e)
            }
        }
} 