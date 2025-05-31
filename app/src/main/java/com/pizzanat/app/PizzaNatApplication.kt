/**
 * @file: PizzaNatApplication.kt
 * @description: Основной класс приложения с инициализацией Hilt DI
 * @dependencies: Hilt Android
 * @created: 2024-12-19
 */
package com.pizzanat.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PizzaNatApplication : Application() 