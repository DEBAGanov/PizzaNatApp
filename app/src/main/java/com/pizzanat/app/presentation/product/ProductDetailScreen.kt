/**
 * @file: ProductDetailScreen.kt
 * @description: Экран детальной информации о продукте в стиле Fox Whiskers
 * @dependencies: Compose, Hilt, FoxCircularProductImage, FoxQuantitySelector
 * @created: 2024-12-19
 * @updated: 2024-12-20 - Переход на стиль Fox Whiskers + FloatingCartButton
 */
package com.pizzanat.app.presentation.product

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pizzanat.app.domain.entities.Product
import com.pizzanat.app.presentation.theme.PizzaNatTheme
import com.pizzanat.app.presentation.theme.CategoryPlateYellow
import com.pizzanat.app.presentation.components.FoxCircularProductImageLarge
import com.pizzanat.app.presentation.components.FloatingCartButton
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Long = 0L,
    onNavigateBack: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    // Загружаем продукт при первой загрузке экрана
    LaunchedEffect(productId) {
        if (productId > 0L) {
            viewModel.loadProduct(productId)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Серый фон в стиле Fox Whiskers
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
        ) {
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error ?: "Неизвестная ошибка",
                        onRetry = { viewModel.loadProduct(productId) },
                        onNavigateBack = onNavigateBack
                    )
                }
                
                uiState.product == null -> {
                    NotFoundContent(onNavigateBack = onNavigateBack)
                }
                
                else -> {
                    ProductContent(
                        product = uiState.product!!,
                        uiState = uiState,
                        onNavigateBack = onNavigateBack,
                        onNavigateToCart = onNavigateToCart,
                        onAddToCart = viewModel::addToCart,
                        onHideSuccess = viewModel::hideAddToCartSuccess
                    )
                }
            }
        }

        // Floating кнопка корзины только когда продукт загружен
        if (uiState.product != null) {
            FloatingCartButton(
                onNavigateToCart = onNavigateToCart,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun LoadingContent() {
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
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Загрузка продукта...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit
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
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "😕",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = "Ошибка загрузки",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Повторить")
                    }
                    OutlinedButton(onClick = onNavigateBack) {
                        Text("Назад")
                    }
                }
            }
        }
    }
}

@Composable
private fun NotFoundContent(onNavigateBack: () -> Unit) {
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
                    text = "🔍",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = "Продукт не найден",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Назад")
                }
            }
        }
    }
}

@Composable
private fun ProductContent(
    product: Product,
    uiState: ProductDetailUiState,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    onAddToCart: () -> Unit,
    onHideSuccess: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Желтая плашка заголовка как в Fox Whiskers
        ProductTopBar(
            productName = product.name,
            onNavigateBack = onNavigateBack,
            onNavigateToCart = onNavigateToCart
        )
        
        // Content с прокруткой
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Круглое изображение продукта в центре
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                FoxCircularProductImageLarge(
                    imageUrl = product.imageUrl,
                    contentDescription = product.name,
                    size = 200.dp
                )
            }
            
            // Карточка с информацией о продукте
            ProductInfoCard(product = product)
            
            // Карточка с описанием (если есть)
            if (!product.description.isNullOrBlank()) {
                ProductDescriptionCard(description = product.description)
            }
            
            // Кнопка добавления в корзину
            AddToCartButton(
                isLoading = uiState.isAddingToCart,
                onAddToCart = onAddToCart
            )
            
            // Сообщение об успешном добавлении
            SuccessMessage(
                showSuccess = uiState.showAddToCartSuccess,
                onHideSuccess = onHideSuccess
            )
            
            // Отступ снизу для floating кнопки
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ProductTopBar(
    productName: String,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit
) {
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
                    text = productName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1
                )
            }
            
            IconButton(onClick = onNavigateToCart) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Корзина",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
private fun ProductInfoCard(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "${NumberFormat.getNumberInstance(Locale("ru", "RU")).format(product.price)} ₽",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ProductDescriptionCard(description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Описание",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun AddToCartButton(
    isLoading: Boolean,
    onAddToCart: () -> Unit
) {
    Button(
        onClick = onAddToCart,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.Black
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.Black,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.Black
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (isLoading) "Добавление..." else "Добавить в корзину",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SuccessMessage(
    showSuccess: Boolean,
    onHideSuccess: () -> Unit
) {
    AnimatedVisibility(
        visible = showSuccess,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "✅ Товар добавлен в корзину",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onHideSuccess) {
                    Text(
                        text = "✕",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
    
    // Auto-hide success message after 3 seconds
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            kotlinx.coroutines.delay(3000)
            onHideSuccess()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductDetailScreenPreview() {
    PizzaNatTheme {
        ProductDetailScreen()
    }
} 