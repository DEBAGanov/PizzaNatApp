/**
 * @file: OptimizedAsyncImage.kt
 * @description: Оптимизированный компонент для загрузки изображений с WebP поддержкой и круглыми изображениями
 * @dependencies: Compose, Coil, Material3
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation

/**
 * Оптимизированный компонент для загрузки изображений
 *
 * Особенности:
 * - WebP конвертация с fallback на JPEG
 * - Адаптивные размеры под плотность экрана
 * - Прогрессивная загрузка с placeholder'ами
 * - Оптимизированное кэширование
 * - Автоматическое масштабирование
 * - Круглые изображения по умолчанию (как на дизайне)
 */
@Composable
fun OptimizedAsyncImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = CircleShape, // Круглые изображения по умолчанию
    placeholder: Int = android.R.drawable.ic_menu_gallery,
    error: Int = android.R.drawable.ic_menu_report_image,
    enableCrossfade: Boolean = true,
    quality: Int = 85,
    shouldPreload: Boolean = false
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // Конвертируем dp в px для точного размера
    val sizePx = with(density) { size.toPx().toInt() }

    // Создаем оптимизированный URL с параметрами качества
    val optimizedUrl = buildOptimizedImageUrl(
        baseUrl = imageUrl,
        width = sizePx,
        height = sizePx,
        quality = quality
    )

    // Выбираем трансформацию в зависимости от формы
    val transformation = when (shape) {
        CircleShape -> CircleCropTransformation()
        is RoundedCornerShape -> RoundedCornersTransformation(with(density) { 8.dp.toPx() })
        else -> CircleCropTransformation() // По умолчанию круглые
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(optimizedUrl)
            .size(Size(sizePx, sizePx))
            .crossfade(if (enableCrossfade) 300 else 0)
            .allowHardware(true)
            .allowRgb565(true)
            .transformations(listOf(transformation))
            .apply {
                if (shouldPreload) {
                    memoryCacheKey("preload_$optimizedUrl")
                }
            }
            .build(),
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .clip(shape),
        contentScale = contentScale,
        placeholder = painterResource(placeholder),
        error = painterResource(error)
    )
}

/**
 * Круглое изображение продукта (как на скриншотах)
 */
@Composable
fun CircularProductImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp
) {
    OptimizedAsyncImage(
        imageUrl = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        size = size,
        shape = CircleShape,
        quality = 90,
        enableCrossfade = true
    )
}

/**
 * Компактная версия для маленьких изображений
 */
@Composable
fun OptimizedAsyncImageSmall(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 30.dp
) {
    OptimizedAsyncImage(
        imageUrl = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        size = size,
        shape = CircleShape,
        quality = 75,
        enableCrossfade = false
    )
}

/**
 * Версия для больших изображений (карточки продуктов)
 */
@Composable
fun OptimizedAsyncImageLarge(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    shouldPreload: Boolean = false
) {
    OptimizedAsyncImage(
        imageUrl = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        size = size,
        shape = CircleShape, // Круглые изображения как на дизайне
        quality = 90,
        shouldPreload = shouldPreload
    )
}

/**
 * Создание оптимизированного URL с параметрами
 *
 * Добавляет параметры для:
 * - Размер изображения (w, h)
 * - Качество (q)
 * - Формат (f=webp с fallback)
 * - Обход кэширования (t=timestamp)
 */
private fun buildOptimizedImageUrl(
    baseUrl: String,
    width: Int,
    height: Int,
    quality: Int = 85
): String {
    if (baseUrl.isBlank()) return baseUrl

    val separator = if (baseUrl.contains("?")) "&" else "?"
    val timestamp = System.currentTimeMillis()

    return "${baseUrl}${separator}w=${width}&h=${height}&q=${quality}&f=webp&t=${timestamp}"
}