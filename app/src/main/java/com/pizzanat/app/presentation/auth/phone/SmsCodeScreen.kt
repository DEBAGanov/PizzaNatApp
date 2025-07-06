/**
 * @file: SmsCodeScreen.kt
 * @description: Экран ввода SMS кода для подтверждения номера телефона
 * @dependencies: Compose Material3, SmsCodeTextField компонент
 * @created: 2024-12-20
 */
package com.pizzanat.app.presentation.auth.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pizzanat.app.presentation.theme.PizzaNatTheme
import com.pizzanat.app.presentation.theme.CategoryPlateYellow
import com.pizzanat.app.utils.SmsRetrieverHelper
import kotlinx.coroutines.delay
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.text
import android.app.NotificationManager
import android.service.notification.StatusBarNotification

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsCodeScreen(
    phoneNumber: String = "",
    onNavigateBack: () -> Unit = {},
    onAuthSuccess: () -> Unit = {},
    viewModel: SmsCodeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    
    // SMS Retriever Helper
    val smsRetrieverHelper = remember {
        SmsRetrieverHelper(context) { smsCode ->
            Log.d("SmsCodeScreen", "📱 Получен SMS код: $smsCode")
            viewModel.onSmsCodeAutoFilled(smsCode)
        }
    }
    
    // Устанавливаем номер телефона в ViewModel
    LaunchedEffect(phoneNumber) {
        if (phoneNumber.isNotBlank()) {
            viewModel.setPhoneNumber(phoneNumber)
        }
    }
    
    // Запуск SMS Retriever при открытии экрана
    LaunchedEffect(Unit) {
        Log.d("SmsCodeScreen", "🚀 Запуск SMS Retriever для автоматического заполнения кода")
        smsRetrieverHelper.startSmsRetriever()
        
        // Альтернативный метод - проверка уведомлений
        try {
            Log.d("SmsCodeScreen", "🔔 Проверка уведомлений на наличие SMS кодов...")
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val activeNotifications = notificationManager.activeNotifications
                Log.d("SmsCodeScreen", "📱 Найдено уведомлений: ${activeNotifications.size}")
                
                for (notification in activeNotifications) {
                    val title = notification.notification.extras?.getString("android.title") ?: ""
                    val text = notification.notification.extras?.getString("android.text") ?: ""
                    val bigText = notification.notification.extras?.getString("android.bigText") ?: ""
                    
                    val fullText = "$title $text $bigText"
                    Log.d("SmsCodeScreen", "🔍 Проверка уведомления: $fullText")
                    
                    // Ищем 4-значный код в уведомлении
                    val codePattern = "\\b\\d{4}\\b".toRegex()
                    val foundCode = codePattern.find(fullText)?.value
                    
                    if (foundCode != null && uiState.smsCode.isEmpty()) {
                        Log.d("SmsCodeScreen", "🎉 Найден SMS код в уведомлении: $foundCode")
                        viewModel.onSmsCodeAutoFilled(foundCode)
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("SmsCodeScreen", "⚠️ Ошибка проверки уведомлений", e)
        }
    }
    
    // Остановка SMS Retriever при закрытии экрана
    DisposableEffect(Unit) {
        onDispose {
            Log.d("SmsCodeScreen", "🛑 Остановка SMS Retriever")
            smsRetrieverHelper.stopSmsRetriever()
        }
    }
    
    // Автофокус на поле ввода кода
    LaunchedEffect(Unit) {
        delay(300) // Небольшая задержка для плавности
        focusRequester.requestFocus()
    }
    
    // Обработка успешной аутентификации
    LaunchedEffect(uiState.isAuthSuccessful) {
        Log.d("SmsCodeScreen", "🔄 isAuthSuccessful состояние изменилось: ${uiState.isAuthSuccessful}")
        if (uiState.isAuthSuccessful) {
            Log.d("SmsCodeScreen", "🎉 Авторизация успешна! Вызываем onAuthSuccess()")
            onAuthSuccess()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Top Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = CategoryPlateYellow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.Black
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Подтверждение номера",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Иконка сообщения
            Icon(
                imageVector = Icons.Default.MailOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            // Заголовок и описание
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Введите код из SMS",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Мы отправили 4-значный код на номер\n$phoneNumber",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // Поле ввода SMS кода
            SmsCodeTextField(
                value = uiState.smsCode,
                onValueChange = viewModel::onSmsCodeChanged,
                isError = uiState.codeError != null,
                modifier = Modifier.focusRequester(focusRequester)
            )
            
            // Показ ошибки
            if (uiState.codeError != null) {
                Text(
                    text = uiState.codeError ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            
            // Кнопка подтверждения
            Button(
                onClick = viewModel::verifySmsCode,
                enabled = !uiState.isLoading && uiState.smsCode.length == 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Подтвердить",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Кнопка повторной отправки кода
            ResendCodeButton(
                countdown = uiState.resendCountdown,
                onResendCode = viewModel::resendSmsCode,
                enabled = !uiState.isLoading
            )
            
            // Показ общей ошибки
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error ?: "",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun SmsCodeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    
    // Проверка буфера обмена на SMS код при создании
    LaunchedEffect(Unit) {
        try {
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val clipText = clipData.getItemAt(0).text?.toString() ?: ""
                val smsCodePattern = "\\b\\d{4}\\b".toRegex()
                val foundCode = smsCodePattern.find(clipText)?.value
                
                if (foundCode != null && value.isEmpty()) {
                    Log.d("SmsCodeTextField", "📋 Найден код в буфере обмена: $foundCode")
                    onValueChange(foundCode)
                }
            }
        } catch (e: Exception) {
            Log.w("SmsCodeTextField", "Ошибка проверки буфера обмена", e)
        }
    }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        repeat(4) { index ->
            val char = value.getOrNull(index)?.toString() ?: ""
            
            Card(
                modifier = Modifier.size(56.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isError) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = if (index == value.length) {
                    CardDefaults.outlinedCardBorder(enabled = true)
                } else {
                    CardDefaults.outlinedCardBorder(enabled = true)
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (index == 0) {
                        // Скрытое поле ввода для получения фокуса и автозаполнения
                        BasicTextField(
                            value = value,
                            onValueChange = { newValue ->
                                // Фильтруем только цифры и ограничиваем до 4 символов
                                val filtered = newValue.filter { it.isDigit() }.take(4)
                                onValueChange(filtered)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                                autoCorrect = false
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .size(1.dp)
                                .semantics {
                                    contentDescription = "SMS verification code"
                                    // Подсказка для автозаполнения
                                    text = AnnotatedString("SMS code")
                                }
                        )
                    }
                    
                    Text(
                        text = char,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isError) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResendCodeButton(
    countdown: Int,
    onResendCode: () -> Unit,
    enabled: Boolean
) {
    if (countdown > 0) {
        Text(
            text = "Повторная отправка кода через $countdown сек",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    } else {
        TextButton(
            onClick = onResendCode,
            enabled = enabled
        ) {
            Text(
                text = "Отправить код повторно",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SmsCodeScreenPreview() {
    PizzaNatTheme {
        SmsCodeScreen(phoneNumber = "+7 (999) 123-45-67")
    }
} 