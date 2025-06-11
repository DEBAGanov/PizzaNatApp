/**
 * @file: TelegramE2ETester.kt
 * @description: E2E тестер для проверки полной цепочки Telegram авторизации
 * @dependencies: Compose, TelegramAuthViewModel, BuildConfig
 * @created: 2024-12-20
 */
package com.pizzanat.app.presentation.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pizzanat.app.BuildConfig
import com.pizzanat.app.presentation.auth.telegram.TelegramAuthViewModel
import com.pizzanat.app.presentation.auth.telegram.TelegramAuthUiState
import com.pizzanat.app.utils.BuildConfigUtils
import com.pizzanat.app.utils.ApiLogger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramE2ETester(
    modifier: Modifier = Modifier,
    viewModel: TelegramAuthViewModel = hiltViewModel()
) {
    // Показываем только в debug режиме
    if (!BuildConfig.DEBUG) return

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Заголовок E2E тестера
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "E2E Tester",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Column {
                        Text(
                            text = "Telegram E2E Тестер",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Backend: ${BuildConfigUtils.getBaseUrl()}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Статус соединения
        item {
            ConnectionStatusCard(uiState = uiState)
        }

        // Управление тестом
        item {
            TestControlCard(
                uiState = uiState,
                onStartAuth = { viewModel.startTelegramAuth() },
                onCheckStatus = { viewModel.checkAuthStatus() },
                onReset = { viewModel.resetAuth() }
            )
        }

        // Информация об авторизации
        if (uiState.authToken != null || uiState.telegramAuthUrl != null) {
            item {
                AuthInfoCard(
                    uiState = uiState,
                    context = context
                )
            }
        }

        // Логи и отладка
        item {
            ApiLogsCard()
        }

        // Детальные логи API
        item {
            DebugLogsCard(uiState = uiState)
        }
    }
}

@Composable
private fun ConnectionStatusCard(uiState: TelegramAuthUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                uiState.isAuthSuccessful -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                uiState.error != null -> Color(0xFFF44336).copy(alpha = 0.1f)
                uiState.isLoading || uiState.isPolling -> Color(0xFFFF9800).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        uiState.isAuthSuccessful -> Icons.Default.CheckCircle
                        uiState.error != null -> Icons.Default.Warning
                        else -> Icons.Default.Refresh
                    },
                    contentDescription = "Status",
                    tint = when {
                        uiState.isAuthSuccessful -> Color(0xFF4CAF50)
                        uiState.error != null -> Color(0xFFF44336)
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
                Text(
                    text = "Статус E2E соединения",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = when {
                    uiState.isAuthSuccessful -> "✅ Авторизация успешно завершена!"
                    uiState.error != null -> "❌ Ошибка: ${uiState.error}"
                    uiState.isPolling -> "🔄 Ожидание подтверждения в Telegram..."
                    uiState.isLoading -> "⏳ Инициализация авторизации..."
                    uiState.telegramAuthUrl != null -> "📱 Готов к авторизации в Telegram"
                    else -> "⚪ Готов к началу тестирования"
                },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TestControlCard(
    uiState: TelegramAuthUiState,
    onStartAuth: () -> Unit,
    onCheckStatus: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Управление тестом",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            // Кнопка старта авторизации
            Button(
                onClick = onStartAuth,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && !uiState.isAuthSuccessful
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Запустить Telegram авторизацию")
            }

            // Кнопка проверки статуса
            OutlinedButton(
                onClick = onCheckStatus,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.authToken != null && !uiState.isLoading
            ) {
                Text("Проверить статус авторизации")
            }

            // Кнопка сброса
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сбросить тест")
            }
        }
    }
}

@Composable
private fun AuthInfoCard(
    uiState: TelegramAuthUiState,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Информация об авторизации",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            if (uiState.authToken != null) {
                InfoRow(
                    label = "Auth Token:",
                    value = uiState.authToken.take(20) + "...",
                    onCopy = {
                        copyToClipboard(context, "Auth Token", uiState.authToken)
                    }
                )
            }

            if (uiState.telegramAuthUrl != null) {
                InfoRow(
                    label = "Telegram URL:",
                    value = uiState.telegramAuthUrl.take(40) + "...",
                    onCopy = {
                        copyToClipboard(context, "Telegram URL", uiState.telegramAuthUrl)
                    },
                    onOpen = {
                        openTelegramUrl(context, uiState.telegramAuthUrl)
                    }
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    onCopy: () -> Unit,
    onOpen: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Copy",
                    modifier = Modifier.size(16.dp)
                )
            }

            if (onOpen != null) {
                IconButton(
                    onClick = onOpen,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Open",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DebugLogsCard(uiState: TelegramAuthUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Debug информация",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = """
                    Loading: ${uiState.isLoading}
                    Polling: ${uiState.isPolling}
                    Success: ${uiState.isAuthSuccessful}
                    Has Token: ${uiState.authToken != null}
                    Has URL: ${uiState.telegramAuthUrl != null}
                    Environment: ${BuildConfigUtils.getEnvironmentDisplayName()}
                """.trimIndent(),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ApiLogsCard() {
    val apiLogs by ApiLogger.logs.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "API Логи (${apiLogs.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                if (apiLogs.isNotEmpty()) {
                    TextButton(
                        onClick = { ApiLogger.clearLogs() }
                    ) {
                        Text("Очистить")
                    }
                }
            }

            if (apiLogs.isEmpty()) {
                Text(
                    text = "Логи API запросов появятся здесь...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                apiLogs.takeLast(5).forEach { log ->
                    ApiLogItem(log = log)
                }

                if (apiLogs.size > 5) {
                    Text(
                        text = "... и еще ${apiLogs.size - 5} запросов",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ApiLogItem(log: com.pizzanat.app.utils.ApiLogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (log.isSuccess) {
                Color(0xFF4CAF50).copy(alpha = 0.05f)
            } else {
                Color(0xFFF44336).copy(alpha = 0.05f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                                         Icon(
                         imageVector = if (log.isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = if (log.isSuccess) "Success" else "Error",
                        tint = if (log.isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = "${log.method} ${log.url}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = log.timestamp,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (log.statusCode != null) "Status: ${log.statusCode}" else "Network Error",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = log.formattedDuration,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (log.error != null) {
                Text(
                    text = "Error: ${log.error}",
                    fontSize = 11.sp,
                    color = Color(0xFFF44336),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label скопирован в буфер обмена", Toast.LENGTH_SHORT).show()
}

private fun openTelegramUrl(context: Context, url: String) {
    try {
        // Пробуем открыть в Telegram app
        val telegramIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        telegramIntent.setPackage("org.telegram.messenger")
        context.startActivity(telegramIntent)
    } catch (e: Exception) {
        try {
            // Fallback к браузеру
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Не удалось открыть URL: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}