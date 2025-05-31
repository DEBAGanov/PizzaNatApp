/**
 * @file: AdminDashboardScreen.kt
 * @description: Главный экран админ панели с статистикой и аналитикой
 * @dependencies: Compose, Hilt, AdminDashboardViewModel
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.admin.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pizzanat.app.domain.entities.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToOrders: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = "Админ панель",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = viewModel::refreshData) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Обновить"
                    )
                }
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Выйти"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Загрузка статистики...",
                            modifier = Modifier.padding(top = 16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            uiState.error != null -> {
                val errorMessage = uiState.error
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = errorMessage ?: "Неизвестная ошибка",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = viewModel::refreshData) {
                            Text("Повторить")
                        }
                    }
                }
            }
            
            else -> {
                DashboardContent(
                    adminStats = uiState.adminStats,
                    currentAdmin = uiState.currentAdmin,
                    onNavigateToOrders = onNavigateToOrders,
                    onNavigateToProducts = onNavigateToProducts
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    adminStats: AdminStats?,
    currentAdmin: AdminUser?,
    onNavigateToOrders: () -> Unit,
    onNavigateToProducts: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Приветствие
        item {
            AdminWelcomeCard(currentAdmin = currentAdmin)
        }
        
        // Основная статистика
        item {
            Text(
                text = "Основная статистика",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        item {
            adminStats?.let { stats ->
                StatsCardsGrid(stats = stats)
            }
        }
        
        // Популярные продукты
        item {
            Text(
                text = "Популярные продукты",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        item {
            adminStats?.let { stats ->
                PopularProductsSection(products = stats.popularProducts)
            }
        }
        
        // Быстрые действия
        item {
            Text(
                text = "Быстрые действия",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        item {
            QuickActionsSection(
                onNavigateToOrders = onNavigateToOrders,
                onNavigateToProducts = onNavigateToProducts,
                canManageOrders = currentAdmin?.canManageOrders() ?: false,
                canManageProducts = currentAdmin?.canManageProducts() ?: false
            )
        }
        
        // Время последнего обновления
        item {
            adminStats?.let { stats ->
                Text(
                    text = "Обновлено: ${stats.generatedAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun AdminWelcomeCard(currentAdmin: AdminUser?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Добро пожаловать!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = currentAdmin?.fullName ?: "Администратор",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = currentAdmin?.role?.displayName ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun StatsCardsGrid(stats: AdminStats) {
    val cards = listOf(
        DashboardCard(
            title = "Всего заказов",
            value = stats.totalOrders.toString(),
            subtitle = "За все время",
            trend = TrendType.UP,
            trendValue = "+${stats.todayOrders} сегодня"
        ),
        DashboardCard(
            title = "Выручка",
            value = "${String.format("%.0f", stats.totalRevenue)} ₽",
            subtitle = "За все время",
            trend = TrendType.UP,
            trendValue = "+${String.format("%.0f", stats.todayRevenue)} ₽ сегодня"
        ),
        DashboardCard(
            title = "В ожидании",
            value = stats.pendingOrders.toString(),
            subtitle = "Требуют внимания",
            trend = TrendType.NEUTRAL
        ),
        DashboardCard(
            title = "Продукты",
            value = stats.totalProducts.toString(),
            subtitle = "В ${stats.totalCategories} категориях",
            trend = TrendType.NEUTRAL
        )
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        cards.chunked(2).forEach { rowCards ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowCards.forEach { card ->
                    StatsCard(
                        card = card,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsCard(
    card: DashboardCard,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = card.value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            card.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            card.trendValue?.let { trendValue ->
                Text(
                    text = trendValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = when(card.trend) {
                        TrendType.UP -> Color(0xFF4CAF50)
                        TrendType.DOWN -> Color(0xFFF44336)
                        TrendType.NEUTRAL -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun PopularProductsSection(products: List<PopularProduct>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (products.isEmpty()) {
                Text(
                    text = "Нет данных о популярных продуктах",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                products.forEachIndexed { index, product ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Позиция
                        Box(
                            modifier = Modifier
                                .size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = when(index) {
                                        0 -> Color(0xFFFFD700) // Золото
                                        1 -> Color(0xFFC0C0C0) // Серебро  
                                        2 -> Color(0xFFCD7F32) // Бронза
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Информация о продукте
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.productName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${product.orderCount} заказов • ${String.format("%.0f", product.totalRevenue)} ₽",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        
                        // Иконка
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    if (index < products.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onNavigateToOrders: () -> Unit,
    onNavigateToProducts: () -> Unit,
    canManageOrders: Boolean,
    canManageProducts: Boolean
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        if (canManageOrders) {
            item {
                QuickActionCard(
                    title = "Заказы",
                    subtitle = "Управление заказами",
                    icon = Icons.Default.ShoppingCart,
                    onClick = onNavigateToOrders
                )
            }
        }
        
        if (canManageProducts) {
            item {
                QuickActionCard(
                    title = "Продукты",
                    subtitle = "Управление товарами",
                    icon = Icons.Default.Star,
                    onClick = onNavigateToProducts
                )
            }
        }
        
        item {
            QuickActionCard(
                title = "Аналитика",
                subtitle = "Подробные отчеты",
                icon = Icons.Default.Settings,
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
} 