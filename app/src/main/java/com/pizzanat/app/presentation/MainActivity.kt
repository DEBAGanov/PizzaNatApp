/**
 * @file: MainActivity.kt
 * @description: Главная активность приложения с Jetpack Compose
 * @dependencies: Compose, Hilt, Navigation
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pizzanat.app.presentation.navigation.PizzaNatNavigation
import com.pizzanat.app.presentation.theme.PizzaNatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PizzaNatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PizzaNatNavigation()
                }
            }
        }
    }
} 