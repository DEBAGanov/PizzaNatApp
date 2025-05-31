/**
 * @file: NotificationsScreen.kt
 * @description: UI экран уведомлений с Material3 дизайном
 * @dependencies: Compose, Material3, Hilt ViewModel
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.notifications

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pizzanat.app.domain.entities.Notification
import com.pizzanat.app.domain.entities.NotificationType
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Уведомления",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
            },
            actions = {
                // Кнопка обновления
                IconButton(
                    onClick = { viewModel.refresh() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Обновить"
                    )
                }
                
                // Кнопка "Отметить все как прочитанные"
                if (uiState.unreadCount > 0) {
                    IconButton(
                        onClick = { viewModel.markAllAsRead() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Отметить все как прочитанные"
                        )
                    }
                }
                
                // Кнопка очистки
                IconButton(
                    onClick = { viewModel.clearAllNotifications() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Очистить все уведомления"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Фильтры по типам уведомлений
        if (uiState.notifications.isNotEmpty()) {
            NotificationFilters(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { viewModel.filterByType(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        // Основной контент
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                uiState.filteredNotifications.isEmpty() -> {
                    EmptyNotificationsContent(
                        hasAnyNotifications = uiState.notifications.isNotEmpty(),
                        selectedFilter = uiState.selectedFilter
                    )
                }
                else -> {
                    NotificationsList(
                        notifications = uiState.filteredNotifications,
                        onNotificationClick = { notification ->
                            if (!notification.isRead) {
                                viewModel.markAsRead(notification.id)
                            }
                        },
                        onNotificationDelete = { notification ->
                            viewModel.deleteNotification(notification.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationFilters(
    selectedFilter: NotificationType?,
    onFilterSelected: (NotificationType?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        // Фильтр "Все"
        item {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { onFilterSelected(null) },
                label = { Text("Все") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
        
        // Фильтры по типам
        items(NotificationType.values()) { type ->
            val icon = when (type) {
                NotificationType.ORDER_STATUS_CHANGED -> Icons.Default.ShoppingCart
                NotificationType.DELIVERY_UPDATE -> Icons.Default.Star
                NotificationType.PROMOTION -> Icons.Default.Star
                NotificationType.SYSTEM -> Icons.Default.Settings
                NotificationType.REMINDER -> Icons.Default.Notifications
            }
            
            val label = when (type) {
                NotificationType.ORDER_STATUS_CHANGED -> "Заказы"
                NotificationType.DELIVERY_UPDATE -> "Доставка"
                NotificationType.PROMOTION -> "Акции"
                NotificationType.SYSTEM -> "Система"
                NotificationType.REMINDER -> "Напоминания"
            }
            
            FilterChip(
                selected = selectedFilter == type,
                onClick = { onFilterSelected(type) },
                label = { Text(label) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotificationsList(
    notifications: List<Notification>,
    onNotificationClick: (Notification) -> Unit,
    onNotificationDelete: (Notification) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = notifications,
            key = { it.id }
        ) { notification ->
            NotificationCard(
                notification = notification,
                onClick = { onNotificationClick(notification) },
                onDelete = { onNotificationDelete(notification) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 2.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Иконка типа уведомления
            NotificationTypeIcon(
                type = notification.type,
                isRead = notification.isRead,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            // Контент уведомления
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Заголовок
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (notification.isRead) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Сообщение
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                // Время создания
                Text(
                    text = notification.createdAt.format(
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Кнопка удаления
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Удалить уведомление",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
            
            // Индикатор непрочитанного уведомления
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun NotificationTypeIcon(
    type: NotificationType,
    isRead: Boolean,
    modifier: Modifier = Modifier
) {
    val icon = when (type) {
        NotificationType.ORDER_STATUS_CHANGED -> Icons.Default.ShoppingCart
        NotificationType.DELIVERY_UPDATE -> Icons.Default.Star
        NotificationType.PROMOTION -> Icons.Default.Star
        NotificationType.SYSTEM -> Icons.Default.Settings
        NotificationType.REMINDER -> Icons.Default.Notifications
    }
    
    val containerColor = when (type) {
        NotificationType.ORDER_STATUS_CHANGED -> Color(0xFF4CAF50)
        NotificationType.DELIVERY_UPDATE -> Color(0xFF2196F3)
        NotificationType.PROMOTION -> Color(0xFFFF9800)
        NotificationType.SYSTEM -> Color(0xFF9C27B0)
        NotificationType.REMINDER -> Color(0xFFF44336)
    }
    
    val finalColor = if (isRead) {
        containerColor.copy(alpha = 0.6f)
    } else {
        containerColor
    }
    
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(finalColor.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = finalColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun LoadingContent() {
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
                text = "Загружаем уведомления...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyNotificationsContent(
    hasAnyNotifications: Boolean,
    selectedFilter: NotificationType?
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = if (hasAnyNotifications) {
                    Icons.Default.Search
                } else {
                    Icons.Default.Notifications
                },
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            
            Text(
                text = if (hasAnyNotifications) {
                    when (selectedFilter) {
                        NotificationType.ORDER_STATUS_CHANGED -> "Нет уведомлений о заказах"
                        NotificationType.DELIVERY_UPDATE -> "Нет уведомлений о доставке"
                        NotificationType.PROMOTION -> "Нет уведомлений об акциях"
                        NotificationType.SYSTEM -> "Нет системных уведомлений"
                        NotificationType.REMINDER -> "Нет напоминаний"
                        null -> "Нет уведомлений"
                    }
                } else {
                    "У вас пока нет уведомлений"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = if (hasAnyNotifications) {
                    "Попробуйте выбрать другой фильтр"
                } else {
                    "Здесь будут отображаться уведомления о статусе заказов, акциях и других важных событиях"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
} 