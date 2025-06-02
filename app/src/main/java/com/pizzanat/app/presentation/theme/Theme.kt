/**
 * @file: Theme.kt
 * @description: Основная тема приложения с ярким желтым дизайном как на скриншотах
 * @dependencies: Compose Material3, Color scheme
 * @created: 2024-12-19
 * @updated: 2024-12-19 - Обновлена для ярких желтых цветов
 */
package com.pizzanat.app.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = YellowBright,
    onPrimary = Gray900,
    primaryContainer = YellowDark,
    onPrimaryContainer = Gray900,
    secondary = YellowAccent,
    onSecondary = Gray900,
    tertiary = PizzaOrange,
    background = Gray900,
    onBackground = Gray100,
    surface = Gray800,
    onSurface = Gray100,
    error = Error,
    onError = Gray50
)

// Яркая светлая цветовая схема с желтыми акцентами как на скриншотах
private val LightColorScheme = lightColorScheme(
    primary = YellowBright,                    // Яркий желтый для основных кнопок
    onPrimary = Gray900,                       // Черный текст на желтом
    primaryContainer = YellowContainer,        // Светло-желтый для контейнеров
    onPrimaryContainer = Gray800,              // Темный текст на контейнерах
    secondary = YellowAccent,                  // Акцентный желтый для вторичных элементов
    onSecondary = Gray900,                     // Черный текст на акцентном
    tertiary = YellowDark,                     // Темнее желтый для особых элементов
    onTertiary = Gray900,                      // Черный текст на третичном
    background = AppBackground,                // Очень светлый фон приложения
    onBackground = Gray900,                    // Черный текст на фоне
    surface = CardBackground,                  // Чисто белые карточки
    onSurface = Gray900,                       // Черный текст на поверхности
    surfaceVariant = SurfaceVariant,           // Альтернативные поверхности
    onSurfaceVariant = OnSurfaceVariant,       // Серый текст на альтернативных поверхностях
    surfaceContainerHighest = Gray100,         // Самые высокие контейнеры
    outline = Gray300,                         // Светлые границы
    outlineVariant = Gray200,                  // Очень светлые границы
    scrim = Color(0x80000000),                 // Затемнение для модальных окон
    error = Error,                            // Красный для ошибок
    onError = Color.White                     // Белый текст на ошибках
)

@Composable
fun PizzaNatTheme(
    darkTheme: Boolean = false, // По умолчанию светлая тема с желтыми акцентами
    // Dynamic color отключен для консистентного желтого дизайна
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme // Всегда используем яркую желтую схему
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
} 