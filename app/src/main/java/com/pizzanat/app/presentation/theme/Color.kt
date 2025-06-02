/**
 * @file: Color.kt
 * @description: Цветовая схема приложения PizzaNat в стиле Fox Whiskers
 * @dependencies: Compose Material3
 * @created: 2024-12-19
 * @updated: 2024-12-20 - Адаптация под стиль Fox Whiskers (серый фон, белые карточки)
 */
package com.pizzanat.app.presentation.theme

import androidx.compose.ui.graphics.Color

// Основные цвета бренда
val PizzaRed = Color(0xFFE53E3E)
val PizzaRedDark = Color(0xFFC53030)
val PizzaOrange = Color(0xFFFF8C00)
val PizzaYellow = Color(0xFFFFC107)

// Желтые цвета в стиле Fox Whiskers
val YellowBright = Color(0xFFFFD54F)         // Основной желтый для кнопок и акцентов
val YellowDark = Color(0xFFFFB300)           // Темнее для hover состояний
val YellowLight = Color(0xFFFFF9C4)          // Очень светлый для второстепенных элементов
val YellowAccent = Color(0xFFFFCA28)         // Акцентный для активных элементов
val YellowButton = Color(0xFFFFD54F)         // Цвет кнопок как на Fox Whiskers
val YellowContainer = Color(0xFFFFF59D)      // Для плашек и заголовков

// Fox Whiskers специфичные цвета
val FoxGrayBackground = Color(0xFFF0F0F0)    // Основной серый фон как на скриншотах
val FoxWhiteCard = Color(0xFFFFFFFF)         // Чисто белые карточки
val FoxGraySearch = Color(0xFFE8E8E8)        // Серая строка поиска
val FoxGrayLight = Color(0xFFF5F5F5)         // Светло-серый для разделителей
val FoxGrayMedium = Color(0xFFD0D0D0)        // Средний серый для границ

// Специальные цвета для Fox Whiskers дизайна
val SearchBarGray = Color(0xFFE8E8E8)        // Серая строка поиска как на скриншотах
val CardShadow = Color(0x1A000000)           // Тень для карточек
val QuantityButtonYellow = Color(0xFFFFD54F) // Желтый для круглых кнопок +/-
val CategoryPlateYellow = Color(0xFFFFD54F)  // Желтые плашки категорий

// Нейтральные цвета для Fox Whiskers стиля
val Gray50 = Color(0xFFFAFAFA)               
val Gray100 = Color(0xFFF5F5F5)              
val Gray200 = Color(0xFFEEEEEE)              
val Gray300 = Color(0xFFE0E0E0)              
val Gray400 = Color(0xFFBDBDBD)              
val Gray500 = Color(0xFF9E9E9E)              
val Gray600 = Color(0xFF757575)              
val Gray700 = Color(0xFF616161)              
val Gray800 = Color(0xFF424242)              
val Gray900 = Color(0xFF212121)              

// Фоны и поверхности в стиле Fox Whiskers
val AppBackground = FoxGrayBackground        // Серый фон приложения как на скриншотах
val CardBackground = FoxWhiteCard            // Чисто белые карточки на сером фоне
val SurfaceVariant = FoxGrayLight            // Альтернативные поверхности
val OnSurfaceVariant = Color(0xFF424242)     // Текст на альтернативных поверхностях

// Системные цвета
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFF9800)
val Error = Color(0xFFF44336)
val Info = Color(0xFF2196F3) 