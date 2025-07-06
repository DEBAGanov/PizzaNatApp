/**
 * @file: PaymentWebViewScreen.kt
 * @description: Экран с WebView для обработки платежей через веб-интерфейс ЮКасса
 * @dependencies: Compose, WebView, Navigation
 * @created: 2025-01-04
 */
package com.pizzanat.app.presentation.payment

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentWebViewScreen(
    navController: NavController,
    paymentUrl: String,
    orderId: Long = 0L,
    onPaymentResult: (PaymentResult) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var canGoBack by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Оплата заказа") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webView = this
                        setupWebView(
                            onPageFinished = { isLoading = false },
                            onCanGoBackChanged = { canGoBack = it },
                            orderId = orderId,
                            onPaymentResult = onPaymentResult
                        )
                        loadUrl(paymentUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Загрузка страницы оплаты...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.setupWebView(
    onPageFinished: () -> Unit,
    onCanGoBackChanged: (Boolean) -> Unit,
    orderId: Long,
    onPaymentResult: (PaymentResult) -> Unit
) {
    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        loadWithOverviewMode = true
        useWideViewPort = true
        setSupportZoom(true)
        builtInZoomControls = true
        displayZoomControls = false
    }
    
    webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            onPageFinished()
            onCanGoBackChanged(view?.canGoBack() == true)
            
            // Проверяем URL для определения результата платежа
            url?.let { checkPaymentResult(it, orderId, onPaymentResult) }
        }
        
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            url?.let { checkPaymentResult(it, orderId, onPaymentResult) }
            return false
        }
    }
}

private fun checkPaymentResult(url: String, orderId: Long, onPaymentResult: (PaymentResult) -> Unit) {
    when {
        url.contains("success") || url.contains("payment_success") -> {
            // Используем переданный orderId или пытаемся извлечь из URL
            val finalOrderId = if (orderId > 0) orderId else extractOrderIdFromUrl(url)
            onPaymentResult(PaymentResult.Success(finalOrderId))
        }
        url.contains("fail") || url.contains("payment_fail") -> {
            onPaymentResult(PaymentResult.Failed)
        }
        url.contains("cancel") || url.contains("payment_cancel") -> {
            onPaymentResult(PaymentResult.Cancelled)
        }
        // Можно добавить другие условия в зависимости от URL схемы вашего backend
    }
}

private fun extractOrderIdFromUrl(url: String): Long {
    // Пытаемся извлечь orderId из URL
    // Ожидаем URL вида: ...?orderId=123 или .../order/123
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
        0L
    }
}

/**
 * Результат платежа
 */
sealed class PaymentResult {
    data class Success(val orderId: Long) : PaymentResult()
    object Failed : PaymentResult()
    object Cancelled : PaymentResult()
    data class Error(val message: String) : PaymentResult()
} 