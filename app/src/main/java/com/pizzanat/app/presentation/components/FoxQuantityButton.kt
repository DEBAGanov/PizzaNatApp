/**
 * @file: FoxQuantityButton.kt
 * @description: Желтые круглые кнопки +/- для управления количеством в стиле Fox Whiskers
 * @dependencies: Compose, Material3
 * @created: 2024-12-20
 */
package com.pizzanat.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizzanat.app.presentation.theme.QuantityButtonYellow

/**
 * Компонент управления количеством в стиле Fox Whiskers
 * Желтые круглые кнопки + и - с числом посередине
 */
@Composable
fun FoxQuantitySelector(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 32.dp,
    enabled: Boolean = true,
    minQuantity: Int = 0
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка минус
        FoxQuantityButton(
            onClick = onDecrement,
            enabled = enabled && quantity > minQuantity,
            size = buttonSize
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Уменьшить количество",
                modifier = Modifier.size(16.dp),
                tint = Color.Black
            )
        }
        
        // Число в центре
        Text(
            text = quantity.toString(),
            modifier = Modifier.widthIn(min = 24.dp),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Кнопка плюс
        FoxQuantityButton(
            onClick = onIncrement,
            enabled = enabled,
            size = buttonSize
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Увеличить количество",
                modifier = Modifier.size(16.dp),
                tint = Color.Black
            )
        }
    }
}

/**
 * Отдельная желтая круглая кнопка в стиле Fox Whiskers
 */
@Composable
fun FoxQuantityButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    size: Dp = 32.dp,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(size),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = QuantityButtonYellow,
            contentColor = Color.Black,
            disabledContainerColor = QuantityButtonYellow.copy(alpha = 0.5f),
            disabledContentColor = Color.Black.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp,
            disabledElevation = 0.dp
        )
    ) {
        content()
    }
}

/**
 * Большая версия для экранов деталей товара
 */
@Composable
fun FoxQuantitySelectorLarge(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minQuantity: Int = 0
) {
    FoxQuantitySelector(
        quantity = quantity,
        onIncrement = onIncrement,
        onDecrement = onDecrement,
        modifier = modifier,
        buttonSize = 40.dp,
        enabled = enabled,
        minQuantity = minQuantity
    )
} 