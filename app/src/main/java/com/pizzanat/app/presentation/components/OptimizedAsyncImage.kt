/**
 * @file: OptimizedAsyncImage.kt
 * @description: Оптимизированные компоненты изображений с кэшированием и fallback
 * @dependencies: Coil, Compose
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy

/**
 * Оптимизированное изображение с кэшированием и обработкой ошибок
 */
@Composable
fun OptimizedAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

/**
 * Круглое изображение продукта среднего размера для Fox Whiskers стиля
 */
@Composable
fun FoxCircularProductImageMedium(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback для продуктов без изображения
            Text(
                text = "🍕",
                fontSize = (size.value / 3).sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Специальный компонент для изображений категорий с улучшенной обработкой
 */
@Composable
fun FoxCircularCategoryImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(true)
                    .allowHardware(false) // Отключаем hardware acceleration для лучшей совместимости
                    .build(),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp), // Небольшой отступ внутри круга
                contentScale = ContentScale.Fit,
                onError = {
                    android.util.Log.e("FoxCircularCategoryImage", "Ошибка загрузки изображения: $imageUrl", it.result.throwable)
                }
            )
        } else {
            // Fallback иконки для разных категорий
            val fallbackEmoji = when (contentDescription?.lowercase()) {
                "пиццы" -> "🍕"
                "бургеры" -> "🍔"
                "напитки" -> "🥤"
                "десерты" -> "🍰"
                "закуски" -> "🍿"
                "салаты" -> "🥗"
                else -> "🍽️"
            }
            Text(
                text = fallbackEmoji,
                fontSize = (size.value / 2.5).sp
            )
        }
    }
}

/**
 * Большое круглое изображение продукта для детального экрана
 */
@Composable
fun FoxCircularProductImageLarge(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp
) {
    Card(
        modifier = modifier.size(size),
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .crossfade(true)
                        .build(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "🍕",
                    fontSize = (size.value / 4).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}