/**
 * @file: MainActivity.kt
 * @description: Главная активность приложения с интеграцией навигации
 * @dependencies: Hilt, Jetpack Compose, Navigation
 * @created: 2024-12-19
 */
package com.pizzanat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.pizzanat.app.presentation.navigation.PizzaNatNavigation
import com.pizzanat.app.presentation.navigation.PizzaNatRoutes
import com.pizzanat.app.presentation.theme.PizzaNatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            PizzaNatTheme {
                val navController = rememberNavController()
                
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    PizzaNatNavigation(
                        navController = navController,
                        startDestination = PizzaNatRoutes.LOGIN,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
} 