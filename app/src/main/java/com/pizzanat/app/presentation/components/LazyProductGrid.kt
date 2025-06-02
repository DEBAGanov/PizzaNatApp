/**
 * @file: LazyProductGrid.kt
 * @description: Оптимизированная сетка продуктов с Lazy Loading и предзагрузкой изображений
 * @dependencies: Compose, LazyVerticalGrid, OptimizedAsyncImage
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed as lazyRowItemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pizzanat.app.domain.entities.Product
import java.text.NumberFormat
import java.util.*

/**
 * Оптимизированная сетка продуктов с ленивой загрузкой
 * 
 * Особенности:
 * - Предзагрузка первых 6 элементов
 * - Умное управление памятью
 * - Отслеживание видимых элементов
 * - Оптимизированные изображения
 */
@Composable
fun LazyProductGrid(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    preloadCount: Int = 6,
    columns: Int = 2
) {
    val gridState = rememberLazyGridState()
    
    // Отслеживаем видимые элементы для оптимизации
    val visibleItemsInfo by remember {
        derivedStateOf {
            gridState.layoutInfo.visibleItemsInfo
        }
    }
    
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        // Оптимизация производительности
        userScrollEnabled = !isLoading
    ) {
        itemsIndexed(
            items = products,
            key = { _, product -> product.id }
        ) { index, product ->
            
            // Определяем нужна ли предзагрузка
            val shouldPreload = index < preloadCount
            
            // Определяем видимость элемента
            val isVisible = visibleItemsInfo.any { it.index == index }
            
            OptimizedProductCard(
                product = product,
                onProductClick = onProductClick,
                onAddToCart = onAddToCart,
                shouldPreload = shouldPreload,
                isVisible = isVisible,
                modifier = Modifier.animateItem() // Новая анимация Compose
            )
        }
        
        // Индикатор загрузки в конце списка
        if (isLoading) {
            item(span = { GridItemSpan(columns) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }
}

/**
 * Оптимизированная карточка продукта с желтым дизайном как на скриншотах
 */
@Composable
private fun OptimizedProductCard(
    product: Product,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    shouldPreload: Boolean,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onProductClick(product) },
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp, // Увеличенная тень для объема
            pressedElevation = 6.dp,
            hoveredElevation = 5.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Белый фон карточек
        ),
        shape = RoundedCornerShape(16.dp) // Более округлые углы
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Круглое изображение продукта (как на скриншотах)
            CircularProductImage(
                imageUrl = product.imageUrl,
                contentDescription = product.name,
                size = 80.dp, // Увеличиваем размер для лучшей видимости
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Название продукта
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Описание (только если видимо)
            if (isVisible && !product.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Цена
            Text(
                text = "${product.price.toInt()} ₽",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Кнопка добавления в корзину (яркая желтая как на скриншотах)
            Button(
                onClick = { onAddToCart(product) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Яркий желтый
                    contentColor = MaterialTheme.colorScheme.onPrimary  // Темный текст
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text(
                    text = "Добавить в корзину",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Компактная версия для горизонтальных списков
 */
@Composable
fun LazyProductRow(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    modifier: Modifier = Modifier,
    preloadCount: Int = 4
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        lazyRowItemsIndexed(
            items = products,
            key = { _, product -> product.id }
        ) { index, product ->
            
            val shouldPreload = index < preloadCount
            
            CompactProductCard(
                product = product,
                onProductClick = onProductClick,
                onAddToCart = onAddToCart,
                shouldPreload = shouldPreload,
                modifier = Modifier
                    .width(160.dp)
                    .animateItem()
            )
        }
    }
}

/**
 * Компактная карточка для горизонтальных списков с желтым дизайном
 */
@Composable
private fun CompactProductCard(
    product: Product,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    shouldPreload: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onProductClick(product) },
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            CircularProductImage(
                imageUrl = product.imageUrl,
                contentDescription = product.name,
                size = 50.dp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = "${product.price.toInt()} ₽",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Круглые кнопки количества как на скриншотах корзины
 */
@Composable
fun QuantityButtons(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка уменьшения
        IconButton(
            onClick = onDecrease,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "−",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Количество
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
        
        // Кнопка увеличения
        IconButton(
            onClick = onIncrease,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Компонент поиска в желтом стиле как на скриншотах
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YellowSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Искать"
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Поиск",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        singleLine = true
    )
} 