/**
 * @file: OrderSuccessScreen.kt
 * @description: Экран успешного оформления заказа с информацией о заказе
 * @dependencies: Compose Material3, Order entities
 * @created: 2024-12-20
 */
package com.pizzanat.app.presentation.order

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pizzanat.app.domain.entities.*
import com.pizzanat.app.presentation.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSuccessScreen(
    order: Order,
    onNavigateToHome: () -> Unit = {},
    onViewOrderDetails: () -> Unit = {}
) {
    // Анимация для иконки успеха
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "success_icon_scale"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoxGrayBackground)
            .statusBarsPadding()
    ) {
        // Основной контент
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Иконка успеха с анимацией
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .background(
                        color = Color(0xFF4CAF50),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Заказ успешно оформлен",
                    modifier = Modifier.size(80.dp),
                    tint = Color.White
                )
            }
            
            // Заголовок
            Text(
                text = "Заказ успешно оформлен!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF4CAF50)
            )
            
            // Номер заказа
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FoxWhiteCard
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Номер заказа",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Text(
                        text = "#${order.id}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = CategoryPlateYellow.copy(red = 0.8f, green = 0.6f, blue = 0.0f)
                    )
                    
                    Text(
                        text = "Заказ принят в обработку",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Информация о заказе
            OrderInfoCard(order = order)
            
            // Информация о доставке
            DeliveryInfoCard(order = order)
            
            // Кнопки действий
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CategoryPlateYellow.copy(red = 0.8f, green = 0.6f, blue = 0.0f),
                        contentColor = Color.Black
                    )
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
                
                OutlinedButton(
                    onClick = onViewOrderDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CategoryPlateYellow.copy(red = 0.8f, green = 0.6f, blue = 0.0f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        CategoryPlateYellow.copy(red = 0.8f, green = 0.6f, blue = 0.0f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Детали заказа",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OrderInfoCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FoxWhiteCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Заголовок карточки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Информация о заказе",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Статус заказа
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = order.status.getDisplayName(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
            
            // Товары в заказе
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Состав заказа:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                text = "${item.quantity} шт. × ${item.productPrice.toInt()}₽",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        
                        Text(
                            text = "${item.totalPrice.toInt()}₽",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            
            // Итоговая сумма
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Сумма заказа:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${order.totalAmount.toInt()}₽",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (order.deliveryCost > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Доставка:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${order.deliveryCost.toInt()}₽",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Итого:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${order.grandTotal.toInt()}₽",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CategoryPlateYellow.copy(red = 0.8f, green = 0.6f, blue = 0.0f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DeliveryInfoCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FoxWhiteCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Детали доставки",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // Способ доставки
            InfoRow(
                label = "Способ получения:",
                value = order.deliveryMethod.displayName
            )
            
            // Адрес доставки (если доставка)
            if (order.deliveryMethod == DeliveryMethod.DELIVERY && order.deliveryAddress.isNotEmpty()) {
                InfoRow(
                    label = "Адрес доставки:",
                    value = order.deliveryAddress
                )
            }
            
            // Способ оплаты
            InfoRow(
                label = "Способ оплаты:",
                value = order.paymentMethod.displayName
            )
            
            // Контактная информация
            InfoRow(
                label = "Контактное лицо:",
                value = order.customerName
            )
            
            InfoRow(
                label = "Телефон:",
                value = order.customerPhone
            )
            
            // Время заказа
            InfoRow(
                label = "Время заказа:",
                value = order.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            )
            
            // Ожидаемое время доставки
            order.estimatedDeliveryTime?.let { estimatedTime ->
                InfoRow(
                    label = "Ожидаемое время:",
                    value = estimatedTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                )
            }
            
            // Примечания
            if (order.notes.isNotEmpty()) {
                InfoRow(
                    label = "Примечания:",
                    value = order.notes
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OrderSuccessScreenPreview() {
    val sampleOrder = Order(
        id = 12345,
        userId = 1,
        items = listOf(
            OrderItem(
                id = 1,
                productId = 1,
                productName = "Пицца Маргарита",
                productPrice = 450.0,
                quantity = 2
            ),
            OrderItem(
                id = 2,
                productId = 2,
                productName = "Кола 0.5л",
                productPrice = 120.0,
                quantity = 1
            )
        ),
        status = OrderStatus.CONFIRMED,
        totalAmount = 1020.0,
        deliveryMethod = DeliveryMethod.DELIVERY,
        deliveryAddress = "ул. Пушкина, д. 10, кв. 5",
        paymentMethod = PaymentMethod.CARD_ON_DELIVERY,
        customerPhone = "+7 (999) 123-45-67",
        customerName = "Иван Петров",
        notes = "Домофон не работает, звонить по телефону",
        estimatedDeliveryTime = LocalDateTime.now().plusMinutes(45),
        createdAt = LocalDateTime.now()
    )
    
    PizzaNatTheme {
        OrderSuccessScreen(order = sampleOrder)
    }
}