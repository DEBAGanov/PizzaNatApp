/**
 * @file: CartScreen.kt
 * @description: Экран корзины в стиле Fox Whiskers
 * @dependencies: Compose, Hilt, Material3, FoxProductCard, FoxQuantitySelector
 * @created: 2024-12-19
 * @updated: 2024-12-20 - Переход на стиль Fox Whiskers
 */
package com.pizzanat.app.presentation.cart

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.pizzanat.app.domain.entities.CartItem
import com.pizzanat.app.presentation.theme.PizzaNatTheme
import com.pizzanat.app.presentation.theme.CategoryPlateYellow
import com.pizzanat.app.presentation.components.FoxProductCardHorizontal
import com.pizzanat.app.presentation.components.FoxCircularProductImageMedium
import com.pizzanat.app.presentation.components.FoxQuantitySelector
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToCheckout: () -> Unit = {},
    onNavigateToProduct: (CartItem) -> Unit = {},
    viewModel: CartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Серый фон Fox Whiskers
            .statusBarsPadding()
    ) {
        // Top Bar с желтой плашкой как в Fox Whiskers
        CartTopBar(
            onNavigateBack = onNavigateBack,
            onClearCart = viewModel::clearCart,
            isEmpty = uiState.isEmpty,
            itemCount = uiState.totalQuantity
        )
        
        // Content
        when {
            uiState.isLoading -> {
                LoadingContent()
            }
            uiState.error != null -> {
                ErrorContent(
                    error = uiState.error!!,
                    onDismissError = viewModel::clearError,
                    onRetry = viewModel::refresh
                )
            }
            uiState.isEmpty -> {
                EmptyCartContent()
            }
            else -> {
                CartContent(
                    uiState = uiState,
                    onItemClick = onNavigateToProduct,
                    onUpdateQuantity = viewModel::updateItemQuantity,
                    onRemoveItem = viewModel::removeItem,
                    onNavigateToCheckout = onNavigateToCheckout
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartTopBar(
    onNavigateBack: () -> Unit,
    onClearCart: () -> Unit,
    isEmpty: Boolean,
    itemCount: Int
) {
    var showClearDialog by remember { mutableStateOf(false) }
    
    // Желтая плашка как в Fox Whiskers
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
                    text = "Корзина",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isEmpty) {
                    // Количество товаров как на скриншоте Fox Whiskers
                    Text(
                        text = "$itemCount товаров",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Очистить корзину",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    }
    
    // Clear Cart Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text("Очистить корзину?")
            },
            text = {
                Text("Все товары будут удалены из корзины. Это действие нельзя отменить.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearCart()
                        showClearDialog = false
                    }
                ) {
                    Text("Очистить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Отмена")
                }
            }
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Загрузка корзины...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onDismissError: () -> Unit,
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
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                    text = "Ошибка загрузки корзины",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Повторить")
                    }
                    OutlinedButton(onClick = onDismissError) {
                        Text("Закрыть")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCartContent() {
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
                    text = "🛒",
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    text = "Корзина пуста",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Добавьте товары в корзину для оформления заказа",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun CartContent(
    uiState: CartUiState,
    onItemClick: (CartItem) -> Unit,
    onUpdateQuantity: (Long, Int) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Список товаров
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.items) { cartItem ->
                FoxCartItemCard(
                    cartItem = cartItem,
                    onItemClick = onItemClick,
                    onUpdateQuantity = { newQuantity ->
                        onUpdateQuantity(cartItem.productId, newQuantity)
                    },
                    onRemove = { onRemoveItem(cartItem.productId) }
                )
            }
        }
        
        // Итого и кнопка оформления как в Fox Whiskers
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Общая сумма
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Итого:",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${NumberFormat.getNumberInstance(Locale("ru", "RU")).format(uiState.totalPrice)} ₽",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Большая желтая кнопка как в Fox Whiskers
                Button(
                    onClick = onNavigateToCheckout,
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
                    Text(
                        text = "Оформить заказ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun FoxCartItemCard(
    cartItem: CartItem,
    onItemClick: (CartItem) -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Круглое изображение товара
            FoxCircularProductImageMedium(
                imageUrl = cartItem.productImageUrl,
                contentDescription = cartItem.productName,
                size = 60.dp
            )
            
            // Информация о товаре
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = cartItem.productName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale("ru", "RU")).format(cartItem.productPrice)} ₽",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Желтые круглые кнопки управления количеством
            FoxQuantitySelector(
                quantity = cartItem.quantity,
                onIncrement = { onUpdateQuantity(cartItem.quantity + 1) },
                onDecrement = { 
                    if (cartItem.quantity > 1) {
                        onUpdateQuantity(cartItem.quantity - 1)
                    } else {
                        onRemove()
                    }
                },
                buttonSize = 32.dp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CartScreenPreview() {
    PizzaNatTheme {
        CartScreen()
    }
} 