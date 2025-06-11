/**
 * @file: HomeScreen.kt
 * @description: Главный экран приложения в стиле Fox Whiskers
 * @dependencies: Compose, Hilt, FoxSearchBar, FoxCircularProductImage
 * @created: 2024-12-19
 * @updated: 2024-12-20 - Переход на стиль Fox Whiskers (серый фон, белые карточки)
 */
package com.pizzanat.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.pizzanat.app.R
import com.pizzanat.app.domain.entities.Category
import com.pizzanat.app.presentation.theme.PizzaNatTheme
import com.pizzanat.app.presentation.theme.YellowBright
import com.pizzanat.app.presentation.theme.CardShadow
import com.pizzanat.app.presentation.theme.CategoryPlateYellow
import com.pizzanat.app.presentation.components.BadgedIcon
import com.pizzanat.app.presentation.components.OptimizedAsyncImage
import com.pizzanat.app.presentation.components.FoxCircularProductImageMedium
import com.pizzanat.app.presentation.components.FoxSearchBar
import com.pizzanat.app.domain.usecases.notification.GetNotificationsUseCase
import com.pizzanat.app.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCategory: (Category) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToDebug: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Серый фон Fox Whiskers
            .statusBarsPadding()
    ) {
        // Top App Bar с желтой плашкой как в Fox Whiskers
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = CategoryPlateYellow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🍕 PizzaNat",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
            /*        IconButton(onClick = viewModel::refresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Обновить",
                            tint = Color.Black
                        )
                    }
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedIcon(
                            icon = Icons.Default.Notifications,
                            contentDescription = "Уведомления",
                            badgeCount = uiState.unreadNotificationsCount,
                            iconTint = Color.Black
                        )
                    }
                    IconButton(onClick = onNavigateToCart) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Корзина",
                            tint = Color.Black
                        )
                    }*/
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Профиль",
                            tint = Color.Black
                        )
                    }
                    IconButton(onClick = onNavigateToAdmin) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Админ панель",
                            tint = Color.Black
                        )
                    }
                    // Debug кнопка - только в debug режиме
                    if (BuildConfig.DEBUG) {
                        IconButton(onClick = onNavigateToDebug) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Debug панель",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        }

        // Серая строка поиска как в Fox Whiskers
        FoxSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = {
                if (it.isNotEmpty()) {
                    onNavigateToSearch()
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = "Искать"
        )

        // Content
        when {
            uiState.isLoading && uiState.categories.isEmpty() -> {
                LoadingContent()
            }
            uiState.error != null && uiState.categories.isEmpty() -> {
                ErrorContent(
                    error = uiState.error ?: "Неизвестная ошибка",
                    onRetry = viewModel::loadCategories,
                    onDismissError = viewModel::clearError
                )
            }
            else -> {
                CategoriesContent(
                    categories = uiState.categories,
                    isRefreshing = uiState.isRefreshing,
                    onCategoryClick = { category ->
                        viewModel.onCategorySelected(category)
                        onNavigateToCategory(category)
                    },
                    error = uiState.error,
                    onDismissError = viewModel::clearError
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Загрузка категорий...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onDismissError: () -> Unit
) {
    LaunchedEffect(error) {
        // Автоматически скрываем ошибку через 5 секунд
        kotlinx.coroutines.delay(5000)
        onDismissError()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "😕",
                    style = MaterialTheme.typography.displayMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Упс! Что-то пошло не так",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Попробовать снова")
                }
            }
        }
    }
}

@Composable
private fun CategoriesContent(
    categories: List<Category>,
    isRefreshing: Boolean,
    onCategoryClick: (Category) -> Unit,
    error: String?,
    onDismissError: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (error != null) {
            LaunchedEffect(error) {
                kotlinx.coroutines.delay(3000)
                onDismissError()
            }
        }

        if (isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Сетка категорий в стиле Fox Whiskers
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                FoxCategoryCard(
                    category = category,
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}

@Composable
private fun FoxCategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Круглое изображение категории
            FoxCircularProductImageMedium(
                imageUrl = category.imageUrl,
                contentDescription = category.name,
                size = 80.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Название категории
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PizzaNatTheme {
        HomeScreen()
    }
}