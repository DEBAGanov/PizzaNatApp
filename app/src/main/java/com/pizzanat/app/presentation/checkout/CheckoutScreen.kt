/**
 * @file: CheckoutScreen.kt
 * @description: Экран оформления заказа в стиле Fox Whiskers
 * @dependencies: Compose, Hilt, Material3
 * @created: 2024-12-19
 * @updated: 2024-12-20 - Переход на стиль Fox Whiskers
 */
package com.pizzanat.app.presentation.checkout

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pizzanat.app.domain.entities.CartItem
import com.pizzanat.app.presentation.theme.PizzaNatTheme
import com.pizzanat.app.presentation.theme.CategoryPlateYellow
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPayment: (Double) -> Unit = {},
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // Серый фон в стиле Fox Whiskers
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Желтая плашка заголовка
        CheckoutTopBar(onNavigateBack = onNavigateBack)

        // Content
        when {
            uiState.isLoading -> {
                LoadingContent()
            }
            uiState.cartItems.isEmpty() -> {
                EmptyCartContent(onNavigateBack)
            }
            else -> {
                CheckoutContent(
                    uiState = uiState,
                    onUpdateAddress = viewModel::updateDeliveryAddress,
                    onUpdatePhone = viewModel::updateCustomerPhone,
                    onUpdateName = viewModel::updateCustomerName,
                    onUpdateNotes = viewModel::updateNotes,
                    onProceedToPayment = {
                        if (viewModel.validateFields()) {
                            // Сохраняем данные заказа в ViewModel
                            viewModel.saveOrderData()
                            // Переходим к экрану оплаты
                            onNavigateToPayment(uiState.totalPrice)
                        }
                    },
                    onClearError = viewModel::clearError,
                    focusManager = focusManager
                )
            }
        }
    }
}

@Composable
private fun CheckoutTopBar(onNavigateBack: () -> Unit) {
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color.Black
                )
            }
            
            Text(
                text = "Оформление заказа",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Загрузка корзины...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun EmptyCartContent(onNavigateBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🛒",
                    style = MaterialTheme.typography.displayLarge
                )
                
                Text(
                    text = "Корзина пуста",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Добавьте товары в корзину для оформления заказа",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("К каталогу", color = Color.Black)
                }
            }
        }
    }
}

@Composable
private fun CheckoutContent(
    uiState: CheckoutUiState,
    onUpdateAddress: (String) -> Unit,
    onUpdatePhone: (String) -> Unit,
    onUpdateName: (String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onProceedToPayment: () -> Unit,
    onClearError: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Error Message
        AnimatedVisibility(
            visible = uiState.error != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    TextButton(onClick = onClearError) {
                        Text(
                            text = "✕",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Order Summary
        OrderSummaryCard(
            cartItems = uiState.cartItems,
            totalPrice = uiState.totalPrice,
            totalItems = uiState.totalItems
        )

        // Delivery Information
        DeliveryInfoCard(
            uiState = uiState,
            onUpdateAddress = onUpdateAddress,
            onUpdatePhone = onUpdatePhone,
            onUpdateName = onUpdateName,
            onUpdateNotes = onUpdateNotes,
            focusManager = focusManager
        )

        // Proceed to Payment Button
        Button(
            onClick = onProceedToPayment,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 2.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Перейти к оплате",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun OrderSummaryCard(
    cartItems: List<CartItem>,
    totalPrice: Double,
    totalItems: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Ваш заказ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            cartItems.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
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
                            text = "${item.quantity} шт. × ${NumberFormat.getNumberInstance(Locale("ru", "RU")).format(item.productPrice)} ₽",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Text(
                        text = "${NumberFormat.getNumberInstance(Locale("ru", "RU")).format(item.totalPrice)} ₽",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Итого ($totalItems товаров):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale("ru", "RU")).format(totalPrice)} ₽",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DeliveryInfoCard(
    uiState: CheckoutUiState,
    onUpdateAddress: (String) -> Unit,
    onUpdatePhone: (String) -> Unit,
    onUpdateName: (String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Информация о доставке",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Delivery Address
            OutlinedTextField(
                value = uiState.deliveryAddress,
                onValueChange = onUpdateAddress,
                label = { Text("Адрес доставки") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.addressError != null,
                supportingText = {
                    if (uiState.addressError != null) {
                        Text(
                            text = uiState.addressError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Customer Phone
            OutlinedTextField(
                value = uiState.customerPhone,
                onValueChange = onUpdatePhone,
                label = { Text("Номер телефона") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.phoneError != null,
                supportingText = {
                    if (uiState.phoneError != null) {
                        Text(
                            text = uiState.phoneError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Customer Name
            OutlinedTextField(
                value = uiState.customerName,
                onValueChange = onUpdateName,
                label = { Text("Имя получателя") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.nameError != null,
                supportingText = {
                    if (uiState.nameError != null) {
                        Text(
                            text = uiState.nameError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notes
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = onUpdateNotes,
                label = { Text("Комментарии к заказу (необязательно)") },
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CheckoutScreenPreview() {
    PizzaNatTheme {
        CheckoutScreen()
    }
} 