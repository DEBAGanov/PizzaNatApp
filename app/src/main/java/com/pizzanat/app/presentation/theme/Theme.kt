/**
 * @file: Theme.kt
 * @description: Основная тема приложения с Material3
 * @dependencies: Compose Material3, Color scheme
 * @created: 2024-12-19
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PizzaRed,
    onPrimary = Gray50,
    primaryContainer = PizzaRedDark,
    onPrimaryContainer = Gray100,
    secondary = PizzaOrange,
    onSecondary = Gray50,
    tertiary = PizzaYellow,
    background = Gray900,
    onBackground = Gray100,
    surface = Gray800,
    onSurface = Gray100,
    error = Error,
    onError = Gray50
)

private val LightColorScheme = lightColorScheme(
    primary = PizzaRed,
    onPrimary = Gray50,
    primaryContainer = Gray100,
    onPrimaryContainer = PizzaRedDark,
    secondary = PizzaOrange,
    onSecondary = Gray50,
    tertiary = PizzaYellow,
    background = Gray50,
    onBackground = Gray900,
    surface = Gray50,
    onSurface = Gray900,
    error = Error,
    onError = Gray50
)

@Composable
fun PizzaNatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
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