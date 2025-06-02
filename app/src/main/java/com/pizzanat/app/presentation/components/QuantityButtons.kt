/**
 * @file: QuantityButtons.kt
 * @description: Круглые кнопки количества с желтым фоном как на скриншотах
 * @dependencies: Compose, Material3
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizzanat.app.presentation.theme.CardShadow
import com.pizzanat.app.presentation.theme.PizzaNatTheme
import com.pizzanat.app.presentation.theme.QuantityButtonYellow

/**
 * Круглые кнопки для изменения количества товара как на скриншотах
 *
 * @param quantity Текущее количество
 * @param onDecrease Callback для уменьшения количества
 * @param onIncrease Callback для увеличения количества
 * @param minQuantity Минимальное количество (обычно 1)
 * @param maxQuantity Максимальное количество (null = без ограничений)
 */
@Composable
fun QuantityButtons(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    minQuantity: Int = 1,
    maxQuantity: Int? = null,
    buttonSize: androidx.compose.ui.unit.Dp = 36.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка уменьшения (-)
        Box(
            modifier = Modifier
                .size(buttonSize)
                .shadow(
                    elevation = 2.dp,
                    shape = CircleShape,
                    ambientColor = CardShadow,
                    spotColor = CardShadow
                )
                .clip(CircleShape)
                .background(QuantityButtonYellow)
                .clickable(enabled = quantity > minQuantity) { 
                    if (quantity > minQuantity) onDecrease() 
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "−",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black
            )
        }
        
        // Отображение количества
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 40.dp)
                .background(
                    color = QuantityButtonYellow.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
        
        // Кнопка увеличения (+)
        Box(
            modifier = Modifier
                .size(buttonSize)
                .shadow(
                    elevation = 2.dp,
                    shape = CircleShape,
                    ambientColor = CardShadow,
                    spotColor = CardShadow
                )
                .clip(CircleShape)
                .background(QuantityButtonYellow)
                .clickable(enabled = maxQuantity?.let { quantity < it } ?: true) { 
                    if (maxQuantity?.let { quantity < it } ?: true) onIncrease() 
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black
            )
        }
    }
}

/**
 * Компактная версия кнопок количества для небольших карточек
 */
@Composable
fun QuantityButtonsCompact(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    minQuantity: Int = 1,
    maxQuantity: Int? = null
) {
    QuantityButtons(
        quantity = quantity,
        onDecrease = onDecrease,
        onIncrease = onIncrease,
        modifier = modifier,
        minQuantity = minQuantity,
        maxQuantity = maxQuantity,
        buttonSize = 28.dp
    )
}

@Preview(showBackground = true)
@Composable
fun QuantityButtonsPreview() {
    PizzaNatTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuantityButtons(
                quantity = 2,
                onDecrease = { },
                onIncrease = { }
            )
            
            QuantityButtonsCompact(
                quantity = 1,
                onDecrease = { },
                onIncrease = { }
            )
        }
    }
} 