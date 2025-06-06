/**
 * @file: FloatingCartButton.kt
 * @description: Floating кнопка корзины для отображения на экранах товаров
 * @dependencies: Compose Material3, Hilt
 * @created: 2024-12-20
 */
package com.pizzanat.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pizzanat.app.domain.usecases.cart.GetCartItemsUseCase
import com.pizzanat.app.presentation.theme.CategoryPlateYellow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.NumberFormat
import java.util.Locale

data class CartSummary(
    val itemCount: Int = 0,
    val totalPrice: Double = 0.0,
    val isVisible: Boolean = false
)

@HiltViewModel
class FloatingCartViewModel @Inject constructor(
    private val getCartItemsUseCase: GetCartItemsUseCase
) : ViewModel() {

    private val _cartSummary = MutableStateFlow(CartSummary())
    val cartSummary: StateFlow<CartSummary> = _cartSummary.asStateFlow()

    init {
        loadCartSummary()
    }

    private fun loadCartSummary() {
        viewModelScope.launch {
            getCartItemsUseCase().collect { cartItems ->
                val itemCount = cartItems.sumOf { it.quantity }
                val totalPrice = cartItems.sumOf { it.totalPrice }

                _cartSummary.value = CartSummary(
                    itemCount = itemCount,
                    totalPrice = totalPrice,
                    isVisible = itemCount > 0
                )
            }
        }
    }
}

/**
 * Floating кнопка корзины
 */
@Composable
fun FloatingCartButton(
    onNavigateToCart: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FloatingCartViewModel = hiltViewModel()
) {
    val cartSummary by viewModel.cartSummary.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = cartSummary.isVisible,
        enter = slideInVertically(
            initialOffsetY = { it }
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it }
        ) + fadeOut(),
        modifier = modifier
    ) {
        Button(
            onClick = onNavigateToCart,
            colors = ButtonDefaults.buttonColors(
                containerColor = CategoryPlateYellow,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(28.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Корзина",
                    tint = Color.Black
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = " ${cartSummary.itemCount} ${getItemsWord(cartSummary.itemCount)} НА ${formatPrice(cartSummary.totalPrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

/**
 * Возвращает правильную форму слова "товар/товара/товаров"
 */
private fun getItemsWord(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "ТОВАР"
        count % 10 in 2..4 && (count % 100 < 10 || count % 100 >= 20) -> "ТОВАРА"
        else -> "ТОВАРОВ"
    }
}

/**
 * Форматирует цену
 */
private fun formatPrice(price: Double): String {
    return "${NumberFormat.getNumberInstance(Locale("ru", "RU")).format(price)} ₽"
}