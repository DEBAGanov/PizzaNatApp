/**
 * @file: DebugNavigation.kt
 * @description: Навигация для debug инструментов и тестеров
 * @dependencies: Compose Navigation, Debug Components
 * @created: 2024-12-20
 */
package com.pizzanat.app.presentation.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizzanat.app.BuildConfig

enum class DebugTab {
    SYSTEM_INFO,
    READINESS_CHECK,
    TELEGRAM_E2E,
    AUTO_E2E_TEST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugNavigation(
    modifier: Modifier = Modifier
) {
    // Показываем только в debug режиме
    if (!BuildConfig.DEBUG) return

    var selectedTab by remember { mutableStateOf(DebugTab.SYSTEM_INFO) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Tab Row для переключения между разделами
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == DebugTab.SYSTEM_INFO,
                onClick = { selectedTab = DebugTab.SYSTEM_INFO },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "System Info",
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Система")
                    }
                }
            )

            Tab(
                selected = selectedTab == DebugTab.READINESS_CHECK,
                onClick = { selectedTab = DebugTab.READINESS_CHECK },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🧪")
                        Text("Готовность")
                    }
                }
            )

            Tab(
                selected = selectedTab == DebugTab.TELEGRAM_E2E,
                onClick = { selectedTab = DebugTab.TELEGRAM_E2E },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Telegram E2E",
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Telegram E2E")
                    }
                }
            )

            Tab(
                selected = selectedTab == DebugTab.AUTO_E2E_TEST,
                onClick = { selectedTab = DebugTab.AUTO_E2E_TEST },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Auto E2E Test",
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Auto Test")
                    }
                }
            )
        }

        // Контент выбранной вкладки
        when (selectedTab) {
            DebugTab.SYSTEM_INFO -> {
                DebugPanel(modifier = Modifier.fillMaxSize())
            }
            DebugTab.READINESS_CHECK -> {
                E2ETestingReadinessPanel()
            }
            DebugTab.TELEGRAM_E2E -> {
                TelegramE2ETester(modifier = Modifier.fillMaxSize())
            }
            DebugTab.AUTO_E2E_TEST -> {
                AutoE2ETestPanel(modifier = Modifier.fillMaxSize())
            }
        }
    }
}