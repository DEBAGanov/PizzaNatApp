/**
 * @file: PaymentScreen.kt
 * @description: Экран выбора способа оплаты и доставки для заказа
 * @dependencies: Compose, Hilt, Material3
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pizzanat.app.R
import com.pizzanat.app.domain.entities.DeliveryMethod
import com.pizzanat.app.domain.entities.PaymentMethod
import com.pizzanat.app.presentation.checkout.OrderData
import com.pizzanat.app.presentation.theme.PizzaNatTheme
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    orderTotal: Double = 0.0,
    orderData: OrderData? = null,
    onNavigateBack: () -> Unit = {},
    onOrderCreated: (Long) -> Unit = {},
    onOrderSuccess: (com.pizzanat.app.domain.entities.Order) -> Unit = {},
    onNavigateToPayment: (String) -> Unit = {},
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    // Передаем сумму заказа в ViewModel
    LaunchedEffect(orderTotal) {
        viewModel.setOrderTotal(orderTotal)
    }
    
    // Передаем данные заказа в ViewModel
    LaunchedEffect(orderData) {
        if (orderData != null) {
            viewModel.setOrderData(orderData)
        }
    }
    
    // Обработка успешного создания заказа
    LaunchedEffect(uiState.orderCreated) {
        if (uiState.orderCreated) {
            if (uiState.createdOrder != null) {
                // Если у нас есть полная информация о заказе, переходим на экран успеха
                onOrderSuccess(uiState.createdOrder!!)
            } else if (uiState.createdOrderId != null) {
                // Fallback - переходим с ID заказа
                onOrderCreated(uiState.createdOrderId!!)
            }
            viewModel.resetOrderCreated()
        }
    }
    
    // Обработка перехода на оплату
    LaunchedEffect(uiState.needsPayment, uiState.paymentUrl) {
        if (uiState.needsPayment && !uiState.paymentUrl.isNullOrEmpty()) {
            // Переход на экран оплаты ЮКасса через WebView
            onNavigateToPayment(uiState.paymentUrl!!)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Оплата и доставка",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Message
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "⚠️ ${uiState.error}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = viewModel::clearError) {
                            Text(
                                text = "✕",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            
            // Delivery Method Section
            DeliveryMethodSection(
                selectedMethod = uiState.selectedDeliveryMethod,
                onMethodSelected = viewModel::selectDeliveryMethod
            )
            
            // Payment Method Section
            PaymentMethodSection(
                selectedMethod = uiState.selectedPaymentMethod,
                onMethodSelected = viewModel::selectPaymentMethod
            )
            
            // Order Summary
            OrderSummarySection(
                subtotal = uiState.subtotal,
                deliveryCost = uiState.deliveryCost,
                total = uiState.total,
                deliveryEstimate = uiState.deliveryEstimate,
                isCalculatingDelivery = uiState.isCalculatingDelivery,
                deliveryCalculationError = uiState.deliveryCalculationError
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Confirm Button
            Button(
                onClick = viewModel::createOrder,
                enabled = !uiState.isCreatingOrder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isCreatingOrder) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Создание заказа...")
                } else {
                    Text(
                        text = "Оформить заказ • ${NumberFormat.getCurrencyInstance(Locale("ru", "RU")).format(uiState.total)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
            }
            
            // Bottom padding
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DeliveryMethodSection(
    selectedMethod: DeliveryMethod,
    onMethodSelected: (DeliveryMethod) -> Unit
) {
    Column {
        Text(
            text = "Способ получения",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DeliveryMethod.values().forEach { method ->
                DeliveryMethodCard(
                    method = method,
                    isSelected = selectedMethod == method,
                    onSelected = { onMethodSelected(method) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeliveryMethodCard(
    method: DeliveryMethod,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val icon = when (method) {
        DeliveryMethod.DELIVERY -> Icons.Default.ShoppingCart
        DeliveryMethod.PICKUP -> Icons.Default.Home
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelected,
                role = Role.RadioButton
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
                brush = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary).brush
            )
        } else {
            CardDefaults.outlinedCardBorder()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = method.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (method.cost > 0) {
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale("ru", "RU")).format(method.cost),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )
                } else {
                    Text(
                        text = "Бесплатно",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            RadioButton(
                selected = isSelected,
                onClick = null // Обрабатывается в Card
            )
        }
    }
}

@Composable
private fun PaymentMethodSection(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit
) {
    Column {
        Text(
            text = "Способ оплаты",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentMethod.values().forEach { method ->
                PaymentMethodCard(
                    method = method,
                    isSelected = selectedMethod == method,
                    onSelected = { onMethodSelected(method) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelected,
                role = Role.RadioButton
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
                brush = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary).brush
            )
        } else {
            CardDefaults.outlinedCardBorder()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (method) {
                PaymentMethod.CARD_ON_DELIVERY -> {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                PaymentMethod.SBP -> {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sbp_logo),
                        contentDescription = null,
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = method.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f)
            )
            
            RadioButton(
                selected = isSelected,
                onClick = null // Обрабатывается в Card
            )
        }
    }
}

@Composable
private fun OrderSummarySection(
    subtotal: Double,
    deliveryCost: Double,
    total: Double,
    deliveryEstimate: com.pizzanat.app.domain.entities.DeliveryEstimate? = null,
    isCalculatingDelivery: Boolean = false,
    deliveryCalculationError: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Итого к оплате",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Subtotal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Товары:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale("ru", "RU")).format(subtotal),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            // Delivery cost with zone information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Доставка:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    // Показываем информацию о зоне доставки
                    when {
                        isCalculatingDelivery -> {
                            Text(
                                text = "Расчет стоимости...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        deliveryEstimate != null -> {
                            Text(
                                text = "Зона: ${deliveryEstimate.zoneName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                            deliveryEstimate.estimatedTime?.let { time ->
                                Text(
                                    text = "Время: $time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                        deliveryCalculationError != null -> {
                            Text(
                                text = "Стандартный тариф",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                if (isCalculatingDelivery) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = if (deliveryCost > 0) {
                            NumberFormat.getCurrencyInstance(Locale("ru", "RU")).format(deliveryCost)
                        } else {
                            "Бесплатно"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (deliveryCost > 0) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
            
            // Показываем ошибку расчета доставки, если есть
            if (deliveryCalculationError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = deliveryCalculationError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
            )
            
            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Всего:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale("ru", "RU")).format(total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentScreenPreview() {
    PizzaNatTheme {
        PaymentScreen(orderTotal = 1500.0)
    }
} 