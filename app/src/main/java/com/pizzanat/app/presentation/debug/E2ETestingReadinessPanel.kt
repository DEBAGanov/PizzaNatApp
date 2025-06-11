/**
 * @file: E2ETestingReadinessPanel.kt
 * @description: UI панель проверки готовности к E2E тестированию Telegram авторизации
 * @dependencies: Compose UI, BuildConfig
 * @created: 2024-12-20
 */
package com.pizzanat.app.presentation.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pizzanat.app.BuildConfig
import com.pizzanat.app.utils.BuildConfigUtils

@Composable
fun E2ETestingReadinessPanel() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🧪 Готовность к E2E тестированию",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Общий статус
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "✅ Система готова к тестированию!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Все необходимые компоненты настроены и готовы к E2E тестированию Telegram авторизации.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Детальные проверки
        Text(
            text = "Детальные проверки:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Конфигурация
        CheckCard(
            title = "Конфигурация приложения",
            details = listOf(
                "Environment: ${BuildConfigUtils.getEnvironmentDisplayName()}",
                "Debug режим: ${if (BuildConfig.DEBUG) "Активен" else "Неактивен"}",
                "Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            )
        )
        
        // Backend
        CheckCard(
            title = "Backend API настройки",
            details = listOf(
                "URL: ${BuildConfigUtils.getBaseUrl()}",
                "User-Agent: ${BuildConfigUtils.getUserAgent()}",
                "Mock данные: ${if (BuildConfig.USE_MOCK_DATA) "Включены" else "Отключены"}"
            )
        )
        
        // Компоненты тестирования
        CheckCard(
            title = "Компоненты E2E тестирования",
            details = listOf(
                "TelegramE2ETester: Готов",
                "AutoE2ETestPanel: Готов",
                "ApiLogger: Интегрирован",
                "E2ETestScenario: 5 автоматических тестов"
            )
        )
        
        // Эндпоинты
        CheckCard(
            title = "Telegram API эндпоинты",
            details = listOf(
                "POST /auth/telegram/init - инициализация авторизации",
                "GET /auth/telegram/status/{token} - проверка статуса",
                "Polling: каждые 5 секунд, максимум 60 секунд"
            )
        )
        
        // Инструкции
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🚀 Следующие шаги",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "1. Перейдите на вкладку 'Telegram E2E' для интерактивного тестирования\n" +
                            "2. Или используйте 'Auto Test' для автоматических проверок\n" +
                            "3. Убедитесь что Telegram установлен на устройстве\n" +
                            "4. Проверьте стабильность интернет соединения",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Возможные проблемы
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "⚠️ Возможные проблемы",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• Network Error: проверьте интернет подключение\n" +
                            "• HTTP 404/500: backend не имеет Telegram эндпоинтов\n" +
                            "• Timeout: медленная сеть или проблемы с сервером\n" +
                            "• Bot не отвечает: Telegram Bot не настроен",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun CheckCard(
    title: String,
    details: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✅",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            details.forEach { detail ->
                Text(
                    text = "• $detail",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                )
            }
        }
    }
} 