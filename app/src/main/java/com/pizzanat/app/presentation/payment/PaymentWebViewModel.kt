/**
 * @file: PaymentWebViewModel.kt
 * @description: ViewModel для обработки платежей через backend API
 * @dependencies: Hilt, ViewModel, Payment Use Cases
 * @created: 2025-01-04
 */
package com.pizzanat.app.presentation.payment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.entities.CreatePaymentRequest
import com.pizzanat.app.domain.entities.PaymentInfo
import com.pizzanat.app.domain.entities.PaymentMethod
import com.pizzanat.app.domain.usecases.payment.CreatePaymentUseCase
import com.pizzanat.app.domain.usecases.payment.ProcessPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentWebUiState(
    val isLoading: Boolean = false,
    val paymentInfo: PaymentInfo? = null,
    val paymentUrl: String? = null,
    val error: String? = null,
    val isPaymentCompleted: Boolean = false,
    val paymentResult: PaymentResult? = null
)

@HiltViewModel
class PaymentWebViewModel @Inject constructor(
    private val createPaymentUseCase: CreatePaymentUseCase,
    private val processPaymentUseCase: ProcessPaymentUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "PaymentWebViewModel"
    }
    
    private val _uiState = MutableStateFlow(PaymentWebUiState())
    val uiState: StateFlow<PaymentWebUiState> = _uiState.asStateFlow()
    
    /**
     * Создание платежа через backend API
     */
    fun createPayment(
        orderId: Long,
        amount: Double,
        paymentMethod: PaymentMethod,
        description: String,
        customerEmail: String? = null,
        customerPhone: String? = null,
        returnUrl: String? = null
    ) {
        Log.d(TAG, "Создание платежа для заказа $orderId на сумму $amount")
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val request = CreatePaymentRequest(
                    amount = amount,
                    currency = "RUB",
                    orderId = orderId,
                    paymentMethod = paymentMethod,
                    description = description,
                    customerEmail = customerEmail,
                    customerPhone = customerPhone,
                    returnUrl = returnUrl
                )
                
                val result = createPaymentUseCase(request)
                
                if (result.isSuccess) {
                    val paymentInfo = result.getOrNull()
                    Log.d(TAG, "Платеж создан: ${paymentInfo?.id}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        paymentInfo = paymentInfo,
                        paymentUrl = paymentInfo?.confirmationUrl,
                        error = null
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Ошибка создания платежа"
                    Log.e(TAG, "Ошибка создания платежа: $error")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при создании платежа", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Неожиданная ошибка: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Обработка результата платежа
     */
    fun handlePaymentResult(result: PaymentResult) {
        Log.d(TAG, "Получен результат платежа: $result")
        
        _uiState.value = _uiState.value.copy(
            paymentResult = result,
            isPaymentCompleted = true
        )
        
        // Если платеж успешен, можно дополнительно проверить статус на сервере
        if (result is PaymentResult.Success) {
            checkPaymentStatus()
        }
    }
    
    /**
     * Проверка статуса платежа на сервере
     */
    private fun checkPaymentStatus() {
        val paymentId = _uiState.value.paymentInfo?.id
        if (paymentId == null) {
            Log.w(TAG, "ID платежа не найден для проверки статуса")
            return
        }
        
        viewModelScope.launch {
            try {
                val result = processPaymentUseCase.getPaymentStatus(paymentId)
                
                if (result.isSuccess) {
                    val updatedPaymentInfo = result.getOrNull()
                    Log.d(TAG, "Статус платежа обновлен: ${updatedPaymentInfo?.status}")
                    
                    _uiState.value = _uiState.value.copy(
                        paymentInfo = updatedPaymentInfo
                    )
                } else {
                    Log.e(TAG, "Ошибка проверки статуса платежа: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при проверке статуса платежа", e)
            }
        }
    }
    
    /**
     * Отмена платежа
     */
    fun cancelPayment() {
        val paymentId = _uiState.value.paymentInfo?.id
        if (paymentId == null) {
            Log.w(TAG, "ID платежа не найден для отмены")
            return
        }
        
        Log.d(TAG, "Отмена платежа: $paymentId")
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = processPaymentUseCase.cancelPayment(paymentId)
                
                if (result.isSuccess) {
                    val cancelledPaymentInfo = result.getOrNull()
                    Log.d(TAG, "Платеж отменен: ${cancelledPaymentInfo?.status}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        paymentInfo = cancelledPaymentInfo,
                        paymentResult = PaymentResult.Cancelled,
                        isPaymentCompleted = true
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Ошибка отмены платежа"
                    Log.e(TAG, "Ошибка отмены платежа: $error")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при отмене платежа", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Неожиданная ошибка: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Очистка ошибки
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Сброс состояния
     */
    fun resetState() {
        _uiState.value = PaymentWebUiState()
    }
} 