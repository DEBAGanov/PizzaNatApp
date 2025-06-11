/**
 * @file: PizzaNatNavigation.kt
 * @description: Настройка навигации приложения с Jetpack Compose Navigation
 * @dependencies: Navigation Compose, Hilt Navigation Compose
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pizzanat.app.data.mappers.toProduct
import com.pizzanat.app.presentation.auth.login.LoginScreen
import com.pizzanat.app.presentation.auth.register.RegisterScreen
import com.pizzanat.app.presentation.cart.CartScreen
import com.pizzanat.app.presentation.category.CategoryProductsScreen
import com.pizzanat.app.presentation.home.HomeScreen
import com.pizzanat.app.presentation.product.ProductDetailScreen
import com.pizzanat.app.presentation.search.SearchScreen
import com.pizzanat.app.presentation.checkout.CheckoutScreen
import com.pizzanat.app.presentation.payment.PaymentScreen
import com.pizzanat.app.presentation.profile.ProfileScreen
import com.pizzanat.app.presentation.splash.SplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.remember
import com.pizzanat.app.presentation.notifications.NotificationsScreen
import com.pizzanat.app.presentation.admin.login.AdminLoginScreen
import com.pizzanat.app.presentation.admin.dashboard.AdminDashboardScreen
import com.pizzanat.app.presentation.admin.orders.AdminOrdersScreen
import com.pizzanat.app.presentation.admin.products.AdminProductsScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.pizzanat.app.presentation.auth.phone.PhoneAuthScreen
import com.pizzanat.app.presentation.auth.phone.SmsCodeScreen
import com.pizzanat.app.presentation.auth.telegram.TelegramAuthScreen
import com.pizzanat.app.presentation.debug.DebugNavigation

/**
 * Маршруты навигации приложения
 */
object PizzaNatRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PHONE_AUTH = "phone_auth"
    const val SMS_CODE = "sms_code/{phoneNumber}"
    const val TELEGRAM_AUTH = "telegram_auth"
    const val HOME = "home"
    const val SPLASH = "splash"
    const val CATEGORY_PRODUCTS = "category_products/{categoryId}/{categoryName}"
    const val PRODUCT_DETAIL = "product_detail/{productId}"
    const val SEARCH = "search"
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val PAYMENT = "payment/{orderTotal}"
    const val PROFILE = "profile"
    const val NOTIFICATIONS = "notifications"

    // Админ панель маршруты
    const val ADMIN_LOGIN = "admin_login"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_ORDERS = "admin_orders"
    const val ADMIN_PRODUCTS = "admin_products"

    // Debug маршрут
    const val DEBUG = "debug"

    fun categoryProducts(categoryId: Long, categoryName: String = "") =
        "category_products/$categoryId/$categoryName"
    fun productDetail(productId: Long) = "product_detail/$productId"
    fun smsCode(phoneNumber: String) = "sms_code/$phoneNumber"
    fun payment(orderTotal: Double) = "payment/$orderTotal"
}

/**
 * Основная навигация приложения
 */
@Composable
fun PizzaNatNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = PizzaNatRoutes.SPLASH,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Экран загрузки
        composable(PizzaNatRoutes.SPLASH) {
            SplashScreen(
                onNavigateToAuth = {
                    navController.navigate(PizzaNatRoutes.LOGIN) {
                        popUpTo(PizzaNatRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // Экран входа
        composable(PizzaNatRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(PizzaNatRoutes.REGISTER) {
                        // Не сохраняем LoginScreen в back stack при переходе к регистрации
                        popUpTo(PizzaNatRoutes.LOGIN) { inclusive = false }
                    }
                },
                onNavigateToPhoneAuth = {
                    navController.navigate(PizzaNatRoutes.PHONE_AUTH)
                },
                onNavigateToTelegramAuth = {
                    navController.navigate(PizzaNatRoutes.TELEGRAM_AUTH)
                },
                onLoginSuccess = {
                    navController.navigate(PizzaNatRoutes.HOME) {
                        // Очищаем весь стек аутентификации при успешном входе
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Экран регистрации
        composable(PizzaNatRoutes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(PizzaNatRoutes.LOGIN) {
                        // Не сохраняем RegisterScreen в back stack при переходе к входу
                        popUpTo(PizzaNatRoutes.REGISTER) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(PizzaNatRoutes.HOME) {
                        // Очищаем весь стек аутентификации при успешной регистрации
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Экран аутентификации через номер телефона
        composable(PizzaNatRoutes.PHONE_AUTH) {
            PhoneAuthScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToSmsCode = { phoneNumber ->
                    navController.navigate(PizzaNatRoutes.smsCode(phoneNumber))
                },
                onAuthSuccess = {
                    navController.navigate(PizzaNatRoutes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Экран ввода SMS кода
        composable(
            route = "sms_code/{phoneNumber}",
            arguments = listOf(
                navArgument("phoneNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            SmsCodeScreen(
                phoneNumber = phoneNumber,
                onNavigateBack = { navController.navigateUp() },
                onAuthSuccess = {
                    navController.navigate(PizzaNatRoutes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Экран аутентификации через Telegram
        composable(PizzaNatRoutes.TELEGRAM_AUTH) {
            TelegramAuthScreen(
                onNavigateBack = { navController.navigateUp() },
                onAuthSuccess = {
                    navController.navigate(PizzaNatRoutes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Главный экран с категориями
        composable(PizzaNatRoutes.HOME) {
            HomeScreen(
                onNavigateToCategory = { category ->
                    navController.navigate(PizzaNatRoutes.categoryProducts(category.id, category.name))
                },
                onNavigateToSearch = {
                    navController.navigate(PizzaNatRoutes.SEARCH)
                },
                onNavigateToCart = {
                    navController.navigate(PizzaNatRoutes.CART)
                },
                onNavigateToProfile = {
                    navController.navigate(PizzaNatRoutes.PROFILE)
                },
                onNavigateToNotifications = {
                    navController.navigate(PizzaNatRoutes.NOTIFICATIONS)
                },
                onNavigateToAdmin = {
                    navController.navigate(PizzaNatRoutes.ADMIN_LOGIN)
                },
                onNavigateToDebug = {
                    navController.navigate(PizzaNatRoutes.DEBUG)
                }
            )
        }

        // Экран продуктов категории
        composable(
            route = "category_products/{categoryId}/{categoryName}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.LongType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            CategoryProductsScreen(
                categoryName = categoryName,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToProduct = { product ->
                    navController.navigate(PizzaNatRoutes.productDetail(product.id))
                },
                onAddToCart = { product ->
                    // Реализовано в CategoryProductsViewModel
                },
                onNavigateToCart = {
                    navController.navigate(PizzaNatRoutes.CART)
                }
            )
        }

        // Экран детальной информации о продукте
        composable(
            route = "product_detail/{productId}",
            arguments = listOf(
                navArgument("productId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
            ProductDetailScreen(
                productId = productId,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToCart = {
                    navController.navigate(PizzaNatRoutes.CART)
                }
            )
        }

        // Экран поиска
        composable(PizzaNatRoutes.SEARCH) {
            SearchScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToProduct = { product ->
                    navController.navigate(PizzaNatRoutes.productDetail(product.id))
                },
                onAddToCart = { product ->
                    // TODO: Implement add to cart functionality
                    // Будет реализовано в Этапе 4
                },
                onNavigateToCart = {
                    navController.navigate(PizzaNatRoutes.CART)
                }
            )
        }

        // Экран корзины
        composable(PizzaNatRoutes.CART) {
            CartScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToCheckout = {
                    navController.navigate(PizzaNatRoutes.CHECKOUT)
                },
                onNavigateToProduct = { cartItem ->
                    val product = cartItem.toProduct()
                    navController.navigate(PizzaNatRoutes.productDetail(product.id))
                }
            )
        }

        // Экран оформления заказа
        composable(PizzaNatRoutes.CHECKOUT) {
            CheckoutScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToPayment = { orderTotal ->
                    navController.navigate(PizzaNatRoutes.payment(orderTotal))
                }
            )
        }

        // Экран оплаты
        composable(PizzaNatRoutes.PAYMENT) { backStackEntry ->
            val orderTotal = backStackEntry.arguments?.getString("orderTotal")?.toDoubleOrNull() ?: 0.0

            // Получаем данные заказа из CheckoutViewModel
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(PizzaNatRoutes.CHECKOUT)
            }
            val checkoutViewModel: com.pizzanat.app.presentation.checkout.CheckoutViewModel = hiltViewModel(parentEntry)
            val orderData = checkoutViewModel.savedOrderData

            PaymentScreen(
                orderTotal = orderTotal,
                orderData = orderData,
                onNavigateBack = { navController.navigateUp() },
                onOrderCreated = { orderId ->
                    navController.navigate(PizzaNatRoutes.HOME) {
                        popUpTo(PizzaNatRoutes.HOME) { inclusive = false }
                    }
                }
            )
        }

        // Экран уведомлений
        composable(PizzaNatRoutes.NOTIFICATIONS) {
            NotificationsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(PizzaNatRoutes.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.navigateUp() },
                onLogout = {
                    navController.navigate(PizzaNatRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Админ панель - Экран входа
        composable(PizzaNatRoutes.ADMIN_LOGIN) {
            AdminLoginScreen(
                onLoginSuccess = {
                    navController.navigate(PizzaNatRoutes.ADMIN_DASHBOARD) {
                        popUpTo(PizzaNatRoutes.ADMIN_LOGIN) { inclusive = true }
                    }
                },
                onBackToApp = {
                    navController.navigate(PizzaNatRoutes.HOME) {
                        popUpTo(PizzaNatRoutes.ADMIN_LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Админ панель - Dashboard
        composable(PizzaNatRoutes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                onNavigateToOrders = {
                    navController.navigate(PizzaNatRoutes.ADMIN_ORDERS)
                },
                onNavigateToProducts = {
                    navController.navigate(PizzaNatRoutes.ADMIN_PRODUCTS)
                },
                onLogout = {
                    navController.navigate(PizzaNatRoutes.ADMIN_LOGIN) {
                        popUpTo(PizzaNatRoutes.ADMIN_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        // Админ панель - Заказы
        composable(PizzaNatRoutes.ADMIN_ORDERS) {
            AdminOrdersScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(PizzaNatRoutes.ADMIN_PRODUCTS) {
            AdminProductsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Debug панель - только в debug режиме
        composable(PizzaNatRoutes.DEBUG) {
            DebugNavigation(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Универсальная заглушка для экранов
 */
@Composable
private fun PlaceholderScreen(
    title: String,
    description: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🚧 $title",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Будет реализовано в следующих версиях",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("← Назад")
        }
    }
}

/**
 * Заглушка для профиля с кнопкой выхода
 */
@Composable
private fun ProfileScreenPlaceholder(
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "👤 Профиль пользователя",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Управление профилем и настройками",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("← Назад")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выйти из аккаунта")
        }
    }
}

/**
 * Временная заглушка для splash экрана
 */
@Composable
private fun SplashScreenPlaceholder(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🍕 PizzaNat",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        CircularProgressIndicator()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Загрузка...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}