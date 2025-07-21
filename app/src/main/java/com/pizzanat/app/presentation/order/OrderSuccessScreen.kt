/**
 * @file: OrderSuccessScreen.kt
 * @description: Экран успешного оформления заказа в виде кассового чека
 * @dependencies: Compose Material3, Order entities
 * @created: 2024-12-20
 * @updated: 2024-12-25 - Переделан в стиле кассового чека
 */
package com.pizzanat.app.presentation.order

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizzanat.app.domain.entities.*
import com.pizzanat.app.presentation.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSuccessScreen(
    orderId: Long,
    onNavigateToHome: () -> Unit = {},
    onViewOrderDetails: () -> Unit = {},
    viewModel: OrderSuccessViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Добавляем диагностическое логирование
    android.util.Log.d("OrderSuccessScreen", "🎬 Экран инициализирован:")
    android.util.Log.d("OrderSuccessScreen", "  orderId: $orderId")
    android.util.Log.d("OrderSuccessScreen", "  ViewModel инжектирован: ${viewModel != null}")
    android.util.Log.d("OrderSuccessScreen", "  UiState: isLoading=${uiState.isLoading}, order=${uiState.order != null}, error='${uiState.error}'")
    
    // Загружаем данные заказа при создании экрана
    LaunchedEffect(orderId) {
        android.util.Log.d("OrderSuccessScreen", "🔄 LaunchedEffect запущен для заказа #$orderId")
        android.util.Log.d("OrderSuccessScreen", "🔄 Вызываем viewModel.loadOrder($orderId)")
        viewModel.loadOrder(orderId)
    }
    
    when {
        uiState.isLoading -> {
            // Показываем индикатор загрузки
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FoxGrayBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "Загрузка данных заказа...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        uiState.order != null -> {
            // Показываем успешный экран с данными заказа
            OrderSuccessContent(
                order = uiState.order!!,
                onNavigateToHome = onNavigateToHome,
                onViewOrderDetails = onViewOrderDetails,
                error = uiState.error
            )
        }
        
        else -> {
            // Показываем ошибку
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FoxGrayBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Ошибка загрузки заказа",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = uiState.error ?: "Произошла неизвестная ошибка",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onNavigateToHome,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CategoryPlateYellow,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("На главную")
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderSuccessContent(
    order: Order,
    onNavigateToHome: () -> Unit,
    onViewOrderDetails: () -> Unit,
    error: String? = null
) {
    // Логирование данных заказа для отладки
    android.util.Log.d("OrderSuccessScreen", "🧾 Отображение заказа:")
    android.util.Log.d("OrderSuccessScreen", "  ID: ${order.id}")
    android.util.Log.d("OrderSuccessScreen", "  Товаров: ${order.items.size}")
    order.items.forEachIndexed { index, item ->
        android.util.Log.d("OrderSuccessScreen", "    Товар ${index + 1}: ${item.productName} - ${item.quantity} шт. по ${item.productPrice} ₽")
    }
    android.util.Log.d("OrderSuccessScreen", "  Сумма товаров: ${order.totalAmount}")
    android.util.Log.d("OrderSuccessScreen", "  Доставка: ${order.deliveryCost}")
    android.util.Log.d("OrderSuccessScreen", "  ИТОГО: ${order.grandTotal}")
    android.util.Log.d("OrderSuccessScreen", "  Способ оплаты: ${order.paymentMethod.displayName}")
    android.util.Log.d("OrderSuccessScreen", "  📍 Адрес доставки: '${order.deliveryAddress}'")
    android.util.Log.d("OrderSuccessScreen", "  �� Телефон: '${order.customerPhone}'")
    android.util.Log.d("OrderSuccessScreen", "  👤 Получатель: '${order.customerName}'")
    android.util.Log.d("OrderSuccessScreen", "  🚛 Способ доставки: ${order.deliveryMethod.displayName}")
    
    // Анимация для иконки успеха
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "success_icon_scale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FoxGrayBackground)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp), // Отступ снизу для кнопки
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Иконка успеха с анимацией
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale)
                    .background(
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Заказ успешно оформлен",
                    modifier = Modifier.size(50.dp),
                    tint = Color.White
                )
            }
            
            // Заголовок успеха
            Text(
                text = "Заказ успешно оформлен!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF4CAF50)
            )
            
            // Номер заказа
                    Text(
                text = "Номер заказа: #${order.id}",
                style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Кассовый чек
            ReceiptCard(order = order)
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Фиксированная кнопка "На главную" внизу экрана
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            FoxGrayBackground.copy(alpha = 0.8f),
                            FoxGrayBackground
                        )
                    )
                )
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp, bottom = 16.dp)
            ) {
                Button(
                    onClick = onNavigateToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                    containerColor = CategoryPlateYellow,
                        contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "На главную",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
        }
    }
}

@Composable
private fun ReceiptCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Заголовок организации
                Text(
                text = "ДИМБО пицца",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "Заказ № ${order.id}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
                    )
            
                    Text(
                text = "Кассир: Система",
                        style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
                    )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Разделительная линия
                Text(
                text = "====================================",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Товары
            order.items.forEachIndexed { index, item ->
                ReceiptItem(
                    position = index + 1,
                    item = item
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Разделительная линия
                    Text(
                text = "====================================",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Промежуточные суммы
            ReceiptSummaryLine(
                label = "Товары:",
                value = NumberFormat.getNumberInstance(Locale("ru", "RU")).format(order.totalAmount)
            )
                
                if (order.deliveryCost > 0) {
                ReceiptSummaryLine(
                    label = "Доставка:",
                    value = NumberFormat.getNumberInstance(Locale("ru", "RU")).format(order.deliveryCost)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Итого - большими буквами с фоном
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ИТОГО",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.Black
                    )
                    Text(
                        text = "=${NumberFormat.getNumberInstance(Locale("ru", "RU")).format(order.grandTotal)}.00",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Способ оплаты
            Text(
                text = when (order.paymentMethod) {
                    PaymentMethod.SBP -> "СБП"
                    PaymentMethod.CARD_ON_DELIVERY -> "НАЛИЧНЫМИ"
                },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
                ),
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "=${NumberFormat.getNumberInstance(Locale("ru", "RU")).format(order.grandTotal)}.00",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Дата и время
            val currentDateTime = SimpleDateFormat("dd.MM.yy HH:mm", Locale("ru", "RU"))
                .format(Date())
            
            Text(
                text = currentDateTime,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ReceiptItem(
    position: Int,
    item: OrderItem
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Название товара
        Text(
            text = "$position. ${item.productName}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.fillMaxWidth()
            )
            
        // Количество и цена
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${item.quantity} шт x ${NumberFormat.getNumberInstance(Locale("ru", "RU")).format(item.productPrice)}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                )
            )
            Text(
                text = "${NumberFormat.getNumberInstance(Locale("ru", "RU")).format(item.totalPrice)}.00",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun ReceiptSummaryLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace
            )
        )
        Text(
            text = "$value.00",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OrderSuccessScreenPreview() {
    PizzaNatTheme {
    val sampleOrder = Order(
            id = 2,
        userId = 1,
        items = listOf(
            OrderItem(
                id = 1,
                productId = 1,
                    productName = "Маргарита",
                    productPrice = 850.0,
                    quantity = 2,
                    totalPrice = 1700.0
            ),
            OrderItem(
                id = 2,
                productId = 2,
                    productName = "Пепперони",
                    productPrice = 950.0,
                    quantity = 1,
                    totalPrice = 950.0
            )
        ),
            status = OrderStatus.PENDING,
            totalAmount = 2650.0,
        deliveryMethod = DeliveryMethod.DELIVERY,
        deliveryAddress = "ул. Пушкина, д. 10, кв. 5",
            deliveryCost = 200.0,
            paymentMethod = PaymentMethod.SBP,
            customerPhone = "+7 900 123-45-67",
            customerName = "Иван Иванов",
            notes = "Домофон не работает, звонить в дверь"
        )
        
        OrderSuccessScreen(orderId = sampleOrder.id)
    }
}