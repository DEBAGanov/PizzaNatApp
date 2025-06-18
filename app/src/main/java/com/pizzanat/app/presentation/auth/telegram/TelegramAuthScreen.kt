/**
 * @file: TelegramAuthScreen.kt
 * @description: Экран авторизации через Telegram с Intent интеграцией
 * @dependencies: Compose Material3, Telegram Bot API интеграция, Android Intent
 * @created: 2024-12-20
 * @updated: 2024-12-20 - Добавлена функциональность открытия Telegram app
 */
package com.pizzanat.app.presentation.auth.telegram

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pizzanat.app.presentation.theme.PizzaNatTheme
import com.pizzanat.app.presentation.theme.CategoryPlateYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramAuthScreen(
    onNavigateBack: () -> Unit = {},
    onAuthSuccess: () -> Unit = {},
    viewModel: TelegramAuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Функция для открытия Telegram
    val openTelegram = { url: String ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
            viewModel.openTelegramAuth()
        } catch (e: Exception) {
            // Если Telegram не установлен, открываем в браузере
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(webIntent)
                viewModel.openTelegramAuth()
            } catch (ex: Exception) {
                // Обработка ошибки
                viewModel.setError("Не удалось открыть Telegram. Убедитесь, что приложение установлено.")
            }
        }
    }
    
    // Обработка успешной аутентификации
    LaunchedEffect(uiState.isAuthSuccessful) {
        if (uiState.isAuthSuccessful) {
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
                    text = "Вход через Telegram",
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
            
            // Иконка Telegram
            Card(
                modifier = Modifier.size(96.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0088CC) // Telegram blue
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
            }
            
            // Заголовок и описание
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Войти через Telegram",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Быстрая и безопасная авторизация через ваш аккаунт Telegram",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // Информация о процессе
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                uiState.telegramAuthUrl != null -> {
                    TelegramAuthContent(
                        authUrl = uiState.telegramAuthUrl!!,
                        onOpenTelegram = openTelegram,
                        onRefresh = viewModel::checkAuthStatus
                    )
                }
                else -> {
                    InitialContent(onStartAuth = viewModel::startTelegramAuth)
                }
            }
            
            // Показ ошибки с альтернативными вариантами
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        // Если Telegram недоступен, предлагаем альтернативы
                        if (uiState.error?.contains("недоступна") == true || 
                            uiState.error?.contains("503") == true) {
                            
                            Divider(color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.3f))
                            
                            Text(
                                text = "Альтернативные способы входа:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                                                 OutlinedButton(
                                     onClick = { 
                                         // TODO: Переход на SMS авторизацию
                                         onNavigateBack()
                                     },
                                     modifier = Modifier.weight(1f),
                                     colors = ButtonDefaults.outlinedButtonColors(
                                         contentColor = MaterialTheme.colorScheme.onErrorContainer
                                     ),
                                     border = BorderStroke(
                                         1.dp, 
                                         MaterialTheme.colorScheme.onErrorContainer
                                     )
                                 ) {
                                     Text("SMS код", style = MaterialTheme.typography.bodySmall)
                                 }
                                
                                                                 OutlinedButton(
                                     onClick = onNavigateBack,
                                     modifier = Modifier.weight(1f),
                                     colors = ButtonDefaults.outlinedButtonColors(
                                         contentColor = MaterialTheme.colorScheme.onErrorContainer
                                     ),
                                     border = BorderStroke(
                                         1.dp, 
                                         MaterialTheme.colorScheme.onErrorContainer
                                     )
                                 ) {
                                     Text("Email/Пароль", style = MaterialTheme.typography.bodySmall)
                                 }
                            }
                        }
                    }
                }
            }
            
            // Информация о конфиденциальности
            Text(
                text = "Нажимая \"Войти через Telegram\", вы соглашаетесь с условиями использования и политикой конфиденциальности",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp)
        )
        
        Text(
            text = "Подготовка авторизации...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun InitialContent(
    onStartAuth: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Преимущества входа через Telegram:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BenefitItem("🔒 Безопасная авторизация")
            BenefitItem("⚡ Мгновенный вход")
            BenefitItem("📱 Не нужно запоминать пароли")
            BenefitItem("🔔 Уведомления о заказах")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = onStartAuth,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0088CC),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Войти через Telegram",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TelegramAuthContent(
    authUrl: String,
    onOpenTelegram: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Откройте Telegram и подтвердите авторизацию",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "1. Нажмите кнопку ниже\n2. Откроется Telegram\n3. Подтвердите авторизацию\n4. Вернитесь в приложение",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Button(
            onClick = { onOpenTelegram(authUrl) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0088CC),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Открыть Telegram",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        OutlinedButton(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Я подтвердил в Telegram")
        }
    }
}

@Composable
private fun BenefitItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TelegramAuthScreenPreview() {
    PizzaNatTheme {
        TelegramAuthScreen()
    }
} 