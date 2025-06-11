/**
 * @file: DebugPanel.kt
 * @description: Панель отладки для разработчиков (только в debug сборке)
 * @dependencies: Compose, BuildConfig, Hilt
 * @created: 2024-12-20
 */
package com.pizzanat.app.presentation.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizzanat.app.BuildConfig
import com.pizzanat.app.utils.BuildConfigUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugPanel(
    modifier: Modifier = Modifier
) {
    // Показываем только в debug режиме
    if (!BuildConfig.DEBUG) return
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // Заголовок
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
                        contentDescription = "Debug Panel",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Debug Panel",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        // Информация о сборке
        item {
            DebugInfoCard(
                title = "Информация о сборке",
                icon = Icons.Default.Info
            ) {
                DebugInfoRow("Версия", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                DebugInfoRow("Среда", BuildConfigUtils.getEnvironmentDisplayName())
                DebugInfoRow("Build Type", BuildConfig.BUILD_TYPE)
                DebugInfoRow("Application ID", BuildConfig.APPLICATION_ID)
                DebugInfoRow("Debug Mode", if (BuildConfig.DEBUG) "Включен" else "Выключен")
            }
        }
        
        // Настройки API
        item {
            DebugInfoCard(
                title = "Настройки API",
                icon = Icons.Default.Settings
            ) {
                DebugInfoRow("Base URL", BuildConfig.BASE_API_URL)
                DebugInfoRow("Mock данные", if (BuildConfig.USE_MOCK_DATA) "Включены" else "Выключены")
                DebugInfoRow("User Agent", BuildConfigUtils.getUserAgent())
                DebugInfoRow("Analytics", if (BuildConfigUtils.isAnalyticsEnabled()) "Включен" else "Выключен")
                DebugInfoRow("Crash Reporting", if (BuildConfigUtils.isCrashReportingEnabled()) "Включен" else "Выключен")
            }
        }
        
        // Системная информация
        item {
            DebugInfoCard(
                title = "Системная информация",
                icon = Icons.Default.Settings
            ) {
                DebugInfoRow("Android Version", android.os.Build.VERSION.RELEASE)
                DebugInfoRow("API Level", android.os.Build.VERSION.SDK_INT.toString())
                DebugInfoRow("Device", "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                DebugInfoRow("ABI", android.os.Build.SUPPORTED_ABIS.first())
            }
        }
        
        // Действия
        item {
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
                        text = "Действия",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Button(
                        onClick = {
                            BuildConfigUtils.logCurrentConfiguration()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Вывести конфигурацию в лог")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            // TODO: Implement force crash for testing
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Тест Crash Reporting (TODO)")
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugInfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            content()
        }
    }
}

@Composable
private fun DebugInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
} 