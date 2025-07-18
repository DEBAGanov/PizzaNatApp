/**
 * @file: DeeplinkWebViewClient.kt
 * @description: WebViewClient для обработки deeplink'ов платежных систем ЮКасса
 * @dependencies: WebView, Intent handling, Banking apps integration
 * @created: 2025-01-23
 */
package com.pizzanat.app.presentation.payment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat.startActivity

/**
 * Кастомный WebViewClient для обработки deeplink'ов банковских приложений
 * Решает проблему net::ERR_UNKNOWN_URL_SCHEME при переходе на платежные приложения
 */
class DeeplinkWebViewClient(
    private val onPaymentResult: ((PaymentResult) -> Unit)? = null,
    private val orderId: Long = 0L,
    private val onPageFinished: (() -> Unit)? = null,
    private val onCanGoBackChanged: ((Boolean) -> Unit)? = null
) : WebViewClient() {
    
    companion object {
        private const val TAG = "DeeplinkWebViewClient"
        
        // Схемы SberPay с особой обработкой
        private val SberPaySchemes = listOf("sberpay", "sbolpay")
        
        // Все поддерживаемые схемы платежных систем
        private val AvailableSchemes = listOf(
            "tinkoffbank", "mirpay", "bank", "yoomoney"
        ) + SberPaySchemes
    }
    
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Log.d(TAG, "Страница загружена: $url")
        
        // Вызываем callback'и
        onPageFinished?.invoke()
        onCanGoBackChanged?.invoke(view?.canGoBack() == true)
        
        // Проверяем URL для определения результата платежа
        url?.let { checkPaymentResult(it) }
    }
    
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (request == null || view == null) return false
        
        val url = request.url.toString()
        Log.d(TAG, "Перехват URL: $url")
        
        try {
            // Проверяем, является ли это deeplink'ом платежной системы
            if (checkURLScheme(request, AvailableSchemes)) {
                Log.d(TAG, "Обнаружен deeplink платежной системы: $url")
                
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                
                // Особая обработка для SberPay - проверяем наличие приложения
                if (checkURLScheme(request, SberPaySchemes) && 
                    intent.resolveActivity(view.context.packageManager) == null) {
                    Log.w(TAG, "SberPay приложение не найдено на устройстве")
                    return true
                }
                
                // Пытаемся запустить приложение
                startActivity(view.context, intent, null)
                Log.d(TAG, "Запущено приложение для обработки deeplink: $url")
                
                return true
                
            } else if (URLUtil.isNetworkUrl(url)) {
                // Обычный веб-URL - пусть загружается в WebView
                Log.d(TAG, "Обычный веб-URL, загружаем в WebView: $url")
                
                // Проверяем результат платежа
                checkPaymentResult(url)
                
                return false
                
            } else {
                // Неизвестная схема - блокируем
                Log.w(TAG, "Неизвестная схема URL, блокируем: $url")
                return true
            }
            
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "Приложение не найдено (ActivityNotFoundException). URL: $url", e)
            // Можно показать пользователю сообщение о том, что нужно установить приложение
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при обработке URL: $url", e)
            return true
        }
    }
    
    /**
     * Проверяет, соответствует ли URL одной из схем платежных систем
     */
    private fun checkURLScheme(request: WebResourceRequest, schemes: List<String>): Boolean {
        val scheme = request.url.scheme?.lowercase() ?: return false
        
        return schemes.any { targetScheme ->
            scheme.startsWith(targetScheme)
        }
    }
    
    /**
     * Проверяет URL на предмет результата платежа
     */
    private fun checkPaymentResult(url: String) {
        Log.d(TAG, "Проверка результата платежа для URL: $url")
        
        when {
            url.contains("success") || url.contains("payment_success") -> {
                Log.d(TAG, "Обнаружен успешный результат платежа")
                val finalOrderId = if (orderId > 0) orderId else extractOrderIdFromUrl(url)
                onPaymentResult?.invoke(PaymentResult.Success(finalOrderId))
            }
            url.contains("fail") || url.contains("payment_fail") -> {
                Log.d(TAG, "Обнаружена ошибка платежа")
                onPaymentResult?.invoke(PaymentResult.Failed)
            }
            url.contains("cancel") || url.contains("payment_cancel") -> {
                Log.d(TAG, "Обнаружена отмена платежа")
                onPaymentResult?.invoke(PaymentResult.Cancelled)
            }
        }
    }
    
    /**
     * Извлекает ID заказа из URL
     */
    private fun extractOrderIdFromUrl(url: String): Long {
        return try {
            when {
                url.contains("orderId=") -> {
                    val regex = Regex("orderId=(\\d+)")
                    val match = regex.find(url)
                    match?.groupValues?.get(1)?.toLong() ?: 0L
                }
                url.contains("/order/") -> {
                    val regex = Regex("/order/(\\d+)")
                    val match = regex.find(url)
                    match?.groupValues?.get(1)?.toLong() ?: 0L
                }
                else -> 0L
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка извлечения orderId из URL: $url", e)
            0L
        }
    }
} 