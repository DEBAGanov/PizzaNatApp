/**
 * @file: OptimizedAsyncImage.kt
 * @description: Оптимизированные компоненты изображений с кэшированием и fallback
 * @dependencies: Coil, Compose
 * @created: 2024-12-19
 * @updated: 2024-12-20 - Добавлена очистка кеша и улучшенная загрузка
 */
package com.pizzanat.app.presentation.components

import android.util.Log
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
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy

/**
 * Очистка кеша изображений
 */
fun clearImageCache(context: android.content.Context) {
    try {
        val imageLoader = ImageLoader(context)
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
        Log.d("OptimizedAsyncImage", "🧹 Кеш изображений очищен")
    } catch (e: Exception) {
        Log.e("OptimizedAsyncImage", "❌ Ошибка очистки кеша: ${e.message}")
    }
}

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
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(300) // Анимация перехода
            .allowHardware(false) // Отключаем для лучшей совместимости
            .listener(
                onStart = {
                    Log.d("OptimizedAsyncImage", "🖼️ Начинаем загрузку: $imageUrl")
                },
                onSuccess = { _, _ ->
                    Log.d("OptimizedAsyncImage", "✅ Изображение загружено: $imageUrl")
                },
                onError = { _, error ->
                    Log.e("OptimizedAsyncImage", "❌ Ошибка загрузки $imageUrl: ${error.throwable.message}")
                }
            )
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
    val context = LocalContext.current
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(300)
                    .allowHardware(false)
                    .listener(
                        onStart = {
                            Log.d("FoxCircularProductImageMedium", "🖼️ Загружаем изображение продукта: $imageUrl")
                        },
                        onSuccess = { _, _ ->
                            Log.d("FoxCircularProductImageMedium", "✅ Изображение продукта загружено: $imageUrl")
                        },
                        onError = { _, error ->
                            Log.e("FoxCircularProductImageMedium", "❌ Ошибка загрузки изображения продукта $imageUrl: ${error.throwable.message}")
                        }
                    )
                    .build(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback для продуктов без изображения
            Log.w("FoxCircularProductImageMedium", "⚠️ Нет URL изображения для продукта: $contentDescription")
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
    val context = LocalContext.current
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(300)
                    .allowHardware(false) // Отключаем hardware acceleration для лучшей совместимости
                    .listener(
                        onStart = {
                            Log.d("FoxCircularCategoryImage", "🖼️ Загружаем изображение категории: $imageUrl")
                        },
                        onSuccess = { _, _ ->
                            Log.d("FoxCircularCategoryImage", "✅ Изображение категории загружено: $imageUrl")
                        },
                        onError = { _, error ->
                            Log.e("FoxCircularCategoryImage", "❌ Ошибка загрузки изображения категории $imageUrl: ${error.throwable.message}")
                        }
                    )
                    .build(),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp), // Небольшой отступ внутри круга
                contentScale = ContentScale.Fit
            )
        } else {
            // Fallback иконки для разных категорий
            Log.w("FoxCircularCategoryImage", "⚠️ Нет URL изображения для категории: $contentDescription")
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
    val context = LocalContext.current
    
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
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .crossfade(300)
                        .allowHardware(false)
                        .listener(
                            onStart = {
                                Log.d("FoxCircularProductImageLarge", "🖼️ Загружаем большое изображение продукта: $imageUrl")
                            },
                            onSuccess = { _, _ ->
                                Log.d("FoxCircularProductImageLarge", "✅ Большое изображение продукта загружено: $imageUrl")
                            },
                            onError = { _, error ->
                                Log.e("FoxCircularProductImageLarge", "❌ Ошибка загрузки большого изображения продукта $imageUrl: ${error.throwable.message}")
                            }
                        )
                        .build(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Log.w("FoxCircularProductImageLarge", "⚠️ Нет URL изображения для большого продукта: $contentDescription")
                Text(
                    text = "🍕",
                    fontSize = (size.value / 4).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}