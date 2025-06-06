/**
 * @file: AdminOrdersScreen.kt
 * @description: Экран управления заказами в админ панели
 * @dependencies: Compose, Hilt, AdminOrdersViewModel
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.admin.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pizzanat.app.domain.entities.Order
import com.pizzanat.app.domain.entities.OrderStatus
import com.pizzanat.app.domain.entities.OrderItem
import com.pizzanat.app.domain.entities.DeliveryMethod
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminOrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showStatusDialog by remember { mutableStateOf<Pair<Order, OrderStatus>?>(null) }
    
    // Показываем сообщение о результате теста API
    uiState.testApiSuccess?.let { message ->
        LaunchedEffect(message) {
            // Сообщение автоматически очистится через 3 секунды в ViewModel
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = "Управление заказами",
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
                // Тестовая кнопка для отладки API
                IconButton(onClick = viewModel::testApiOrders) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Тестировать API"
                    )
                }
                IconButton(onClick = viewModel::refreshOrders) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Обновить"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        
        // Показываем сообщение о тестировании API
        uiState.testApiSuccess?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "🧪 $message",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        when {
            uiState.isLoading -> {
                LoadingContent()
            }
            
            uiState.error != null -> {
                ErrorContent(
                    error = uiState.error ?: "Неизвестная ошибка",
                    onRetry = viewModel::loadOrders,
                    onDismiss = viewModel::clearError
                )
            }
            
            else -> {
                OrdersContent(
                    uiState = uiState,
                    onStatusFilterChanged = viewModel::filterByStatus,
                    onSearchQueryChanged = viewModel::searchOrders,
                    onStatusChangeRequest = { order, status ->
                        showStatusDialog = order to status
                    },
                    onRefresh = viewModel::refreshOrders
                )
            }
        }
    }
    
    // Status Change Dialog
    showStatusDialog?.let { (order, newStatus) ->
        StatusChangeDialog(
            order = order,
            newStatus = newStatus,
            isUpdating = uiState.updatingOrderId == order.id,
            onConfirm = {
                viewModel.updateOrderStatus(order.id, newStatus)
                showStatusDialog = null
            },
            onDismiss = { showStatusDialog = null }
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Text(
                text = "Загрузка заказов...",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Ошибка загрузки заказов",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Понятно")
                }
                TextButton(onClick = onRetry) {
                    Text("Повторить")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrdersContent(
    uiState: AdminOrdersUiState,
    onStatusFilterChanged: (OrderStatus?) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onStatusChangeRequest: (Order, OrderStatus) -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Показываем индикатор обновления
        if (uiState.isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Фильтры и поиск
        FiltersSection(
            selectedStatus = uiState.selectedStatusFilter,
            searchQuery = uiState.searchQuery,
            onStatusFilterChanged = onStatusFilterChanged,
            onSearchQueryChanged = onSearchQueryChanged
        )
        
        // Список заказов
        val orders = uiState.filteredOrders
        if (orders.isEmpty()) {
            EmptyOrdersContent()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(orders) { order ->
                    AdminOrderCard(
                        order = order,
                        isUpdating = uiState.updatingOrderId == order.id,
                        onStatusChangeRequest = onStatusChangeRequest
                    )
                }
            }
        }
    }
}

@Composable
private fun FiltersSection(
    selectedStatus: OrderStatus?,
    searchQuery: String,
    onStatusFilterChanged: (OrderStatus?) -> Unit,
    onSearchQueryChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            label = { Text("Поиск по клиенту, телефону, адресу...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Поиск"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Очистить"
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Status Filters
        Text(
            text = "Фильтр по статусу:",
            style = MaterialTheme.typography.labelMedium
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    onClick = { onStatusFilterChanged(null) },
                    label = { Text("Все") },
                    selected = selectedStatus == null
                )
            }
            
            items(OrderStatus.values()) { status ->
                FilterChip(
                    onClick = { onStatusFilterChanged(status) },
                    label = { Text(getStatusDisplayName(status)) },
                    selected = selectedStatus == status
                )
            }
        }
    }
}

@Composable
private fun EmptyOrdersContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = "Заказы не найдены",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Попробуйте изменить фильтры",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminOrderCard(
    order: Order,
    isUpdating: Boolean,
    onStatusChangeRequest: (Order, OrderStatus) -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок заказа с админскими функциями
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Заказ #${order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    
                    // Кликабельный статус для изменения
                    AdminStatusChip(
                        status = order.status,
                        onClick = { showStatusMenu = true }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Информация о заказе
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AdminOrderInfoRow(
                    icon = Icons.Default.DateRange,
                    label = "Дата",
                    value = order.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                )
                
                AdminOrderInfoRow(
                    icon = Icons.Default.Star,
                    label = "Сумма",
                    value = "${order.totalAmount}₽"
                )
                
                AdminOrderInfoRow(
                    icon = Icons.Default.Person,
                    label = "Клиент",
                    value = order.customerName
                )
                
                AdminOrderInfoRow(
                    icon = Icons.Default.Phone,
                    label = "Телефон",
                    value = order.customerPhone
                )
                
                if (order.deliveryMethod == DeliveryMethod.DELIVERY && !order.deliveryAddress.isNullOrBlank()) {
                    AdminOrderInfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Адрес",
                        value = order.deliveryAddress
                    )
                }
                
                AdminOrderInfoRow(
                    icon = Icons.Default.AccountCircle,
                    label = "Оплата",
                    value = order.paymentMethod?.displayName ?: "Не указана"
                )
                
                AdminOrderInfoRow(
                    icon = Icons.Default.Home,
                    label = "Доставка",
                    value = order.deliveryMethod?.displayName ?: "Не указана"
                )
            }
            
            // Товары в заказе
            if (order.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Товары:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    order.items.forEach { item ->
                        AdminOrderItemRow(item = item)
                    }
                }
            }
            
            if (!order.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Комментарий: ${order.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                )
            }
        }
    }
    
    // Dropdown menu для изменения статуса
    DropdownMenu(
        expanded = showStatusMenu,
        onDismissRequest = { showStatusMenu = false }
    ) {
        OrderStatus.values().forEach { status ->
            if (status != order.status) {
                DropdownMenuItem(
                    text = { Text(getStatusDisplayName(status)) },
                    onClick = {
                        onStatusChangeRequest(order, status)
                        showStatusMenu = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AdminStatusChip(
    status: OrderStatus,
    onClick: () -> Unit
) {
    val (backgroundColor, contentColor) = when (status) {
        OrderStatus.PENDING -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        OrderStatus.CONFIRMED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        OrderStatus.PREPARING -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        OrderStatus.READY -> MaterialTheme.colorScheme.successContainer to MaterialTheme.colorScheme.onSuccessContainer
        OrderStatus.DELIVERING -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        OrderStatus.DELIVERED -> MaterialTheme.colorScheme.successContainer to MaterialTheme.colorScheme.onSuccessContainer
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = status.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Изменить статус",
                modifier = Modifier.size(16.dp),
                tint = contentColor
            )
        }
    }
}

@Composable
private fun AdminOrderInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.width(70.dp)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AdminOrderItemRow(item: OrderItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.productName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${item.productPrice}₽ × ${item.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Text(
            text = "${item.totalPrice}₽",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StatusChangeDialog(
    order: Order,
    newStatus: OrderStatus,
    isUpdating: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить статус заказа") },
        text = {
            Text(
                "Изменить статус заказа #${order.id} на \"${getStatusDisplayName(newStatus)}\"?"
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Подтвердить")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

private fun getStatusDisplayName(status: OrderStatus): String {
    return when (status) {
        OrderStatus.PENDING -> "Ожидает подтверждения"
        OrderStatus.CONFIRMED -> "Подтвержден"
        OrderStatus.PREPARING -> "Готовится"
        OrderStatus.READY -> "Готов"
        OrderStatus.DELIVERING -> "Доставляется"
        OrderStatus.DELIVERED -> "Доставлен"
        OrderStatus.CANCELLED -> "Отменен"
    }
}

// Расширение ColorScheme для дополнительных цветов
private val ColorScheme.successContainer: androidx.compose.ui.graphics.Color
    get() = androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.12f)

private val ColorScheme.onSuccessContainer: androidx.compose.ui.graphics.Color
    get() = androidx.compose.ui.graphics.Color(0xFF1B5E20) 