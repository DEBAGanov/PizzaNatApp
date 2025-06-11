/**
 * @file: AutoE2ETestPanel.kt
 * @description: Панель автоматического E2E тестирования Telegram авторизации
 * @dependencies: Compose, E2ETestScenario, TelegramAuthViewModel
 * @created: 2024-12-20
 */
package com.pizzanat.app.presentation.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pizzanat.app.BuildConfig
import com.pizzanat.app.presentation.auth.telegram.TelegramAuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoE2ETestPanel(
    modifier: Modifier = Modifier,
    telegramViewModel: TelegramAuthViewModel = hiltViewModel()
) {
    // Показываем только в debug режиме
    if (!BuildConfig.DEBUG) return
    
    val scope = rememberCoroutineScope()
    var e2eTestScenario by remember { mutableStateOf<E2ETestScenario?>(null) }
    var testState by remember { mutableStateOf(E2ETestState()) }
    
    // Инициализируем E2E тестер
    LaunchedEffect(telegramViewModel) {
        e2eTestScenario = E2ETestScenario(telegramViewModel)
    }
    
    // Подписываемся на состояние тестов
    LaunchedEffect(e2eTestScenario) {
        e2eTestScenario?.testState?.collect { state ->
            testState = state
        }
    }
    
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
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Auto E2E Test",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Column {
                        Text(
                            text = "Автоматический E2E Test",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Комплексное тестирование Telegram авторизации",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        
        // Управление тестами
        item {
            TestControlSection(
                testState = testState,
                onRunTests = {
                    scope.launch {
                        e2eTestScenario?.runFullE2ETest()
                    }
                },
                onClearResults = {
                    e2eTestScenario?.clearResults()
                }
            )
        }
        
        // Прогресс тестирования
        if (testState.isRunning) {
            item {
                TestProgressSection(testState = testState)
            }
        }
        
        // Результаты тестов
        if (testState.results.isNotEmpty()) {
            item {
                TestResultsSection(testState = testState)
            }
        }
    }
}

@Composable
private fun TestControlSection(
    testState: E2ETestState,
    onRunTests: () -> Unit,
    onClearResults: () -> Unit
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
                text = "Управление тестами",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRunTests,
                    modifier = Modifier.weight(1f),
                    enabled = !testState.isRunning
                ) {
                    if (testState.isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Запустить E2E тесты")
                }
                
                OutlinedButton(
                    onClick = onClearResults,
                    enabled = testState.results.isNotEmpty() && !testState.isRunning
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Очистить")
                }
            }
        }
    }
}

@Composable
private fun TestProgressSection(testState: E2ETestState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Выполнение тестов...",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (testState.currentTest != null) {
                Text(
                    text = "Текущий тест: ${testState.currentTest}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            LinearProgressIndicator(
                progress = if (testState.totalTests > 0) {
                    testState.completedTests.toFloat() / testState.totalTests.toFloat()
                } else 0f,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "${testState.completedTests} / ${testState.totalTests} тестов завершено",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TestResultsSection(testState: E2ETestState) {
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
            val successCount = testState.results.count { it.isSuccess }
            val totalCount = testState.results.size
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Результаты тестов",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "$successCount/$totalCount пройдено",
                    fontSize = 14.sp,
                    color = if (successCount == totalCount) {
                        Color(0xFF4CAF50)
                    } else {
                        Color(0xFFF44336)
                    }
                )
            }
            
            testState.results.forEach { result ->
                TestResultItem(result = result)
            }
        }
    }
}

@Composable
private fun TestResultItem(result: E2ETestResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isSuccess) {
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
                        imageVector = if (result.isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = if (result.isSuccess) "Success" else "Failed",
                        tint = if (result.isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = result.testName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = "${result.duration}ms",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (result.apiCallsCount > 0) {
                Text(
                    text = "API вызовов: ${result.apiCallsCount}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (result.error != null) {
                Text(
                    text = "Ошибка: ${result.error}",
                    fontSize = 11.sp,
                    color = Color(0xFFF44336),
                    fontFamily = FontFamily.Monospace
                )
            }
            
            if (result.details != null) {
                Text(
                    text = result.details,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
} 