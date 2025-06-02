/**
 * @file: FoxProductCard.kt
 * @description: Карточки товаров в стиле Fox Whiskers (белые карточки с тенью, круглые изображения)
 * @dependencies: Compose, Material3, FoxCircularProductImage, FoxQuantitySelector
 * @created: 2024-12-20
 */
package com.pizzanat.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import com.pizzanat.app.domain.entities.Product
import java.text.NumberFormat
import java.util.*

/**
 * Карточка товара в стиле Fox Whiskers для сетки
 */
@Composable
fun FoxProductCard(
    product: Product,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    modifier: Modifier = Modifier,
    quantity: Int = 0,
    onQuantityChange: ((Int) -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onProductClick(product) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Круглое изображение товара
            FoxCircularProductImageMedium(
                imageUrl = product.imageUrl,
                contentDescription = product.name,
                size = 80.dp
            )
            
            // Название товара
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Описание (если есть)
            if (!product.description.isNullOrBlank()) {
                Text(
                    text = product.description!!,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Цена
            Text(
                text = "${NumberFormat.getNumberInstance(Locale("ru", "RU")).format(product.price)} ₽",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Управление количеством или кнопка добавить
            if (quantity > 0 && onQuantityChange != null) {
                FoxQuantitySelector(
                    quantity = quantity,
                    onIncrement = { onQuantityChange(quantity + 1) },
                    onDecrement = { onQuantityChange(maxOf(0, quantity - 1)) },
                    buttonSize = 28.dp
                )
            } else {
                Button(
                    onClick = { onAddToCart(product) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Добавить",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Горизонтальная карточка товара для списков (например, в корзине)
 */
@Composable
fun FoxProductCardHorizontal(
    product: Product,
    quantity: Int,
    onProductClick: (Product) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onRemove: ((Product) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onProductClick(product) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Круглое изображение
            FoxCircularProductImageMedium(
                imageUrl = product.imageUrl,
                contentDescription = product.name,
                size = 60.dp
            )
            
            // Информация о товаре
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (!product.description.isNullOrBlank()) {
                    Text(
                        text = product.description!!,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale("ru", "RU")).format(product.price)} ₽",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Управление количеством
            FoxQuantitySelector(
                quantity = quantity,
                onIncrement = { onQuantityChange(quantity + 1) },
                onDecrement = { 
                    if (quantity > 1) {
                        onQuantityChange(quantity - 1)
                    } else {
                        onRemove?.invoke(product)
                    }
                },
                buttonSize = 32.dp
            )
        }
    }
} 