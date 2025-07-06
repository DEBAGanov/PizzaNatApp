/**
 * @file: CategoryProductsScreen.kt
 * @description: Экран списка продуктов в стиле Fox Whiskers
 * @dependencies: Compose, Hilt, FoxProductCard
 * @created: 2024-12-19
 * @updated: 2024-12-20 - Переход на стиль Fox Whiskers + FloatingCartButton + Pull-to-Refresh
 */
package com.pizzanat.app.presentation.category

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.pizzanat.app.domain.entities.Product
import com.pizzanat.app.presentation.theme.PizzaNatTheme
import com.pizzanat.app.presentation.theme.YellowBright
import com.pizzanat.app.presentation.theme.CardShadow
import com.pizzanat.app.presentation.theme.CategoryPlateYellow
import com.pizzanat.app.presentation.components.LazyProductGrid
import com.pizzanat.app.presentation.components.OptimizedAsyncImage
import com.pizzanat.app.presentation.components.FoxCircularProductImageMedium
import com.pizzanat.app.presentation.components.FoxProductCard
import com.pizzanat.app.presentation.components.FloatingCartButton
import com.pizzanat.app.presentation.components.clearImageCache
import java.text.NumberFormat
import java.util.*
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun CategoryProductsScreen(
    categoryId: Long = 0L,
    categoryName: String = "",
    onNavigateBack: () -> Unit = {},
    onNavigateToProduct: (Product) -> Unit = {},
    onAddToCart: (Product) -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    viewModel: CategoryProductsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Pull-to-Refresh состояние
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = {
            // Очищаем кеш изображений при обновлении
            clearImageCache(context)
            viewModel.refresh()
        }
    )
    
    // Устанавливаем ID и название категории в ViewModel
    LaunchedEffect(categoryId, categoryName) {
        android.util.Log.d("CategoryProductsScreen", "LaunchedEffect: categoryId=$categoryId, categoryName=$categoryName")
        android.util.Log.d("CategoryProductsScreen", "Current ViewModel state categoryId: ${uiState.categoryId}")
        
        if (categoryId > 0) {
            viewModel.setCategoryId(categoryId)
        } else {
            android.util.Log.e("CategoryProductsScreen", "ОШИБКА: Получен некорректный categoryId=$categoryId")
        }
        
        if (categoryName.isNotBlank()) {
            viewModel.setCategoryName(categoryName)
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // Серый фон Fox Whiskers
                .statusBarsPadding()
        ) {
            // Top Bar с желтой плашкой как в Fox Whiskers
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад",
                                tint = Color.Black
                            )
                        }
                        
                        Text(
                            text = categoryName.ifBlank { "Продукты" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    
                    // Кнопка обновления с очисткой кеша
                    IconButton(onClick = { 
                        clearImageCache(context)
                        viewModel.refresh()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Обновить и очистить кеш",
                            tint = Color.Black
                        )
                    }
                }
            }
            
            // Content с Pull-to-Refresh
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                when {
                    uiState.isLoading && uiState.products.isEmpty() -> {
                        LoadingContent()
                    }
                    uiState.error != null && uiState.products.isEmpty() -> {
                        ErrorContent(
                            error = uiState.error ?: "Неизвестная ошибка",
                            onRetry = { 
                                clearImageCache(context)
                                viewModel.loadProducts() 
                            },
                            onDismissError = { viewModel.clearError() }
                        )
                    }
                    uiState.products.isEmpty() -> {
                        EmptyContent(
                            categoryName = categoryName,
                            onRetry = { 
                                clearImageCache(context)
                                viewModel.loadProducts() 
                            }
                        )
                    }
                    else -> {
                        // Сетка товаров в стиле Fox Whiskers
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                                                contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.products) { product ->
                                FoxProductCard(
                                    product = product,
                                    onProductClick = onNavigateToProduct,
                                    onAddToCart = { viewModel.addToCart(it) }
                                )
                            }
                        }
                    }
                }
                
                // Pull-to-Refresh индикатор
                PullRefreshIndicator(
                    refreshing = uiState.isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
            
            // Показываем ошибку как Snackbar если есть продукты
            if (uiState.error != null && uiState.products.isNotEmpty()) {
                LaunchedEffect(uiState.error) {
                    // Автоматически скрываем ошибку через 3 секунды
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearError()
                }
            }
            
            // Показываем сообщение об успешном добавлении в корзину
            if (uiState.addToCartSuccess != null) {
                LaunchedEffect(uiState.addToCartSuccess) {
                    // Сообщение автоматически скрывается в ViewModel
                }
            }
        }

        // Floating кнопка корзины
        FloatingCartButton(
            onNavigateToCart = onNavigateToCart,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
                text = "Загрузка продуктов...",
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
private fun EmptyContent(
    categoryName: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🍕",
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    text = "Пусто в категории",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (categoryName.isNotBlank()) {
                        "В категории \"$categoryName\" пока нет товаров"
                    } else {
                        "Попробуйте обновить список или выберите другую категорию"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Обновить")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = CardShadow,
                spotColor = CardShadow
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Shadow уже добавлен
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Круглое изображение продукта как на скриншотах
            FoxCircularProductImageMedium(
                imageUrl = product.imageUrl ?: "",
                contentDescription = product.name
            )
            
            // Product Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (product.description.isNotBlank()) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
                        .format(product.price),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Желтая кнопка добавления в корзину как на скриншотах
            if (product.available) {
                FilledTonalButton(
                    onClick = onAddToCart,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = YellowBright,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить в корзину",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Добавить",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Недоступен",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryProductsScreenPreview() {
    PizzaNatTheme {
        CategoryProductsScreen(categoryName = "Пиццы")
    }
} 