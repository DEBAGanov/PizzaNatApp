/**
 * @file: CircularProductImage.kt
 * @description: Компоненты для круглых изображений товаров в стиле Fox Whiskers
 * @dependencies: Compose, Coil, Material3
 * @created: 2024-12-20
 */
package com.pizzanat.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy

/**
 * Круглое изображение товара в стиле Fox Whiskers - маленький размер
 */
@Composable
fun FoxCircularProductImageSmall(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp
) {
    FoxCircularProductImage(
        imageUrl = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        size = size
    )
}

/**
 * Круглое изображение товара в стиле Fox Whiskers - средний размер
 */
@Composable
fun FoxCircularProductImageMedium(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    FoxCircularProductImage(
        imageUrl = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        size = size
    )
}

/**
 * Круглое изображение товара в стиле Fox Whiskers - большой размер
 */
@Composable
fun FoxCircularProductImageLarge(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    FoxCircularProductImage(
        imageUrl = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        size = size
    )
}

/**
 * Базовый компонент круглого изображения товара в стиле Fox Whiskers
 */
@Composable
private fun FoxCircularProductImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 2.dp,
                shape = CircleShape,
                clip = false
            )
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(200)
                    .build(),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            // Плейсхолдер для пустых изображений - просто серый круг
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        CircleShape
                    )
            )
        }
    }
} 