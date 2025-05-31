/**
 * @file: MockAdminRepositoryImpl.kt
 * @description: Мок-реализация AdminRepository с тестовыми данными
 * @dependencies: AdminRepository, AdminUser, AdminStats
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.repositories

import com.pizzanat.app.domain.entities.*
import com.pizzanat.app.domain.repositories.AdminRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAdminRepositoryImpl @Inject constructor() : AdminRepository {
    
    private var currentAdmin: AdminUser? = null
    
    // Тестовые админы
    private val testAdmins = listOf(
        AdminUser(
            id = 1L,
            username = "admin",
            email = "admin@pizzanat.com",
            firstName = "Главный",
            lastName = "Администратор",
            role = AdminRole.SUPER_ADMIN,
            permissions = AdminUser.getPermissionsForRole(AdminRole.SUPER_ADMIN),
            isActive = true,
            lastLoginAt = LocalDateTime.now().minusHours(2),
            createdAt = LocalDateTime.now().minusDays(30)
        ),
        AdminUser(
            id = 2L,
            username = "manager",
            email = "manager@pizzanat.com",
            firstName = "Анна",
            lastName = "Менеджер",
            role = AdminRole.MANAGER,
            permissions = AdminUser.getPermissionsForRole(AdminRole.MANAGER),
            isActive = true,
            lastLoginAt = LocalDateTime.now().minusMinutes(30),
            createdAt = LocalDateTime.now().minusDays(15)
        )
    )
    
    override suspend fun loginAdmin(username: String, password: String): Result<AdminUser> {
        delay(1000) // Симуляция сетевого запроса
        
        // Тестовые логины: admin/admin123, manager/manager123
        val admin = when {
            username == "admin" && password == "admin123" -> testAdmins[0]
            username == "manager" && password == "manager123" -> testAdmins[1]
            else -> null
        }
        
        return if (admin != null) {
            currentAdmin = admin.copy(lastLoginAt = LocalDateTime.now())
            Result.success(currentAdmin!!)
        } else {
            Result.failure(Exception("Неверное имя пользователя или пароль"))
        }
    }
    
    override suspend fun getCurrentAdmin(): Result<AdminUser?> {
        return Result.success(currentAdmin)
    }
    
    override suspend fun logoutAdmin(): Result<Unit> {
        currentAdmin = null
        return Result.success(Unit)
    }
    
    override suspend fun getAdminStats(): Result<AdminStats> {
        delay(500)
        
        val stats = AdminStats(
            totalOrders = 347,
            todayOrders = 28,
            pendingOrders = 12,
            completedOrders = 320,
            totalRevenue = 156800.50,
            todayRevenue = 8950.00,
            totalProducts = 25,
            totalCategories = 5,
            totalUsers = 89,
            popularProducts = listOf(
                PopularProduct(1L, "Пицца Маргарита", 45, 22500.0),
                PopularProduct(2L, "Пицца Пепперони", 38, 19000.0),
                PopularProduct(3L, "Пицца 4 Сыра", 32, 16800.0)
            ),
            recentOrders = emptyList(), // Заполним из OrderRepository
            generatedAt = LocalDateTime.now()
        )
        
        return Result.success(stats)
    }
    
    override fun getAdminStatsFlow(): Flow<AdminStats> = flow {
        while (true) {
            val result = getAdminStats()
            if (result.isSuccess) {
                emit(result.getOrThrow())
            }
            delay(30000) // Обновляем каждые 30 секунд
        }
    }
    
    override suspend fun getAllOrders(page: Int, size: Int): Result<List<Order>> {
        delay(500)
        
        // Генерируем тестовые заказы
        val testOrders = listOf(
            Order(
                id = 1L,
                userId = 1L,
                status = OrderStatus.PENDING,
                items = emptyList(),
                totalAmount = 1250.0,
                deliveryAddress = "ул. Пушкина, дом Колотушкина, кв. 15",
                customerPhone = "+7 (999) 123-45-67",
                customerName = "Иван Петров",
                notes = "Домофон не работает, звонить по телефону",
                paymentMethod = PaymentMethod.CASH,
                deliveryMethod = DeliveryMethod.DELIVERY,
                deliveryCost = 200.0,
                estimatedDeliveryTime = LocalDateTime.now().plusMinutes(45),
                createdAt = LocalDateTime.now().minusMinutes(15),
                updatedAt = LocalDateTime.now().minusMinutes(10)
            ),
            Order(
                id = 2L,
                userId = 2L,
                status = OrderStatus.PREPARING,
                items = emptyList(),
                totalAmount = 890.0,
                deliveryAddress = "Самовывоз",
                customerPhone = "+7 (985) 678-90-12",
                customerName = "Мария Сидорова",
                notes = "Заказ на самовывоз",
                paymentMethod = PaymentMethod.CARD_ON_DELIVERY,
                deliveryMethod = DeliveryMethod.PICKUP,
                deliveryCost = 0.0,
                estimatedDeliveryTime = LocalDateTime.now().plusMinutes(20),
                createdAt = LocalDateTime.now().minusMinutes(25),
                updatedAt = LocalDateTime.now().minusMinutes(5)
            ),
            Order(
                id = 3L,
                userId = 3L,
                status = OrderStatus.DELIVERING,
                items = emptyList(),
                totalAmount = 1680.0,
                deliveryAddress = "пр. Ленина, д. 42, оф. 315",
                customerPhone = "+7 (912) 345-67-89",
                customerName = "Алексей Козлов",
                notes = "Офисное здание, вход с торца",
                paymentMethod = PaymentMethod.ONLINE_CARD,
                deliveryMethod = DeliveryMethod.DELIVERY,
                deliveryCost = 200.0,
                estimatedDeliveryTime = LocalDateTime.now().plusMinutes(10),
                createdAt = LocalDateTime.now().minusHours(1),
                updatedAt = LocalDateTime.now().minusMinutes(15)
            ),
            Order(
                id = 4L,
                userId = 4L,
                status = OrderStatus.DELIVERED,
                items = emptyList(),
                totalAmount = 750.0,
                deliveryAddress = "ул. Гагарина, д. 8, кв. 22",
                customerPhone = "+7 (903) 456-78-90",
                customerName = "Елена Николаева",
                notes = "",
                paymentMethod = PaymentMethod.CASH,
                deliveryMethod = DeliveryMethod.DELIVERY,
                deliveryCost = 200.0,
                estimatedDeliveryTime = LocalDateTime.now().minusMinutes(10),
                createdAt = LocalDateTime.now().minusHours(2),
                updatedAt = LocalDateTime.now().minusMinutes(30)
            ),
            Order(
                id = 5L,
                userId = 5L,
                status = OrderStatus.CANCELLED,
                items = emptyList(),
                totalAmount = 1420.0,
                deliveryAddress = "ул. Советская, д. 15",
                customerPhone = "+7 (921) 567-89-01",
                customerName = "Дмитрий Волков",
                notes = "Отменен клиентом",
                paymentMethod = PaymentMethod.CARD_ON_DELIVERY,
                deliveryMethod = DeliveryMethod.DELIVERY,
                deliveryCost = 200.0,
                estimatedDeliveryTime = LocalDateTime.now().plusHours(1),
                createdAt = LocalDateTime.now().minusHours(3),
                updatedAt = LocalDateTime.now().minusHours(2)
            )
        )
        
        // Применяем пагинацию
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, testOrders.size)
        
        val paginatedOrders = if (startIndex < testOrders.size) {
            testOrders.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        return Result.success(paginatedOrders)
    }
    
    override fun getAllOrdersFlow(): Flow<List<Order>> = flow {
        while (true) {
            val result = getAllOrders()
            if (result.isSuccess) {
                emit(result.getOrThrow())
            }
            delay(10000) // Обновляем каждые 10 секунд
        }
    }
    
    override suspend fun updateOrderStatus(orderId: Long, status: OrderStatus): Result<Order> {
        delay(800)
        
        // Мок обновления статуса - находим заказ и меняем статус
        val result = getAllOrders()
        if (result.isSuccess) {
            val orders = result.getOrThrow()
            val order = orders.find { it.id == orderId }
            
            if (order != null) {
                val updatedOrder = order.copy(
                    status = status,
                    updatedAt = LocalDateTime.now()
                )
                return Result.success(updatedOrder)
            }
        }
        
        return Result.failure(Exception("Заказ с ID $orderId не найден"))
    }
    
    override suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>> {
        delay(300)
        
        val allOrdersResult = getAllOrders()
        if (allOrdersResult.isSuccess) {
            val filteredOrders = allOrdersResult.getOrThrow().filter { it.status == status }
            return Result.success(filteredOrders)
        }
        
        return allOrdersResult
    }
    
    override suspend fun getAllProducts(): Result<List<Product>> {
        delay(300)
        
        val mockProducts = listOf(
            Product(1L, "Пицца Маргарита", "Классическая пицца с томатами, моцареллой и базиликом", 650.0, "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400", 1L, true),
            Product(2L, "Пицца Пепперони", "Острая пицца с пепперони и сыром моцарелла", 750.0, "https://images.unsplash.com/photo-1628840042765-356cda07504e?w=400", 1L, true),
            Product(3L, "Пицца 4 Сыра", "Пицца с четырьмя видами сыра", 890.0, "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400", 1L, true),
            Product(4L, "Пицца Мясная", "Сытная пицца с курицей, беконом и колбасой", 950.0, "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=400", 1L, true),
            Product(5L, "Пицца Гавайская", "Пицца с ананасами и ветчиной", 820.0, "https://images.unsplash.com/photo-1576458088443-04a19d8a06b0?w=400", 1L, false),
            Product(6L, "Бургер Классический", "Говяжья котлета с овощами и соусом", 450.0, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400", 2L, true),
            Product(7L, "Чизбургер", "Бургер с двойным сыром", 520.0, "https://images.unsplash.com/photo-1553979459-d2229ba7433a?w=400", 2L, true),
            Product(8L, "Картофель фри", "Хрустящий картофель фри", 280.0, "https://images.unsplash.com/photo-1541592106381-b31e9677c0e5?w=400", 3L, true),
            Product(9L, "Крылышки BBQ", "Острые куриные крылышки в соусе BBQ", 620.0, "https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=400", 3L, true),
            Product(10L, "Кока-кола", "Классическая кола 0.5л", 120.0, "https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=400", 4L, true),
            Product(11L, "Апельсиновый сок", "Свежевыжатый сок 0.3л", 180.0, "https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?w=400", 4L, true),
            Product(12L, "Тирамису", "Классический итальянский десерт", 290.0, "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=400", 5L, true),
            Product(13L, "Чизкейк", "Нежный чизкейк с ягодами", 350.0, "https://images.unsplash.com/photo-1567958499588-feb1aaed8042?w=400", 5L, true)
        )
        
        return Result.success(mockProducts)
    }
    
    override suspend fun createProduct(product: Product): Result<Product> {
        delay(1000)
        return Result.success(product.copy(id = System.currentTimeMillis()))
    }
    
    override suspend fun updateProduct(product: Product): Result<Product> {
        delay(800)
        return Result.success(product)
    }
    
    override suspend fun deleteProduct(productId: Long): Result<Unit> {
        delay(500)
        return Result.success(Unit)
    }
    
    override suspend fun getAllCategories(): Result<List<Category>> {
        delay(300)
        
        val mockCategories = listOf(
            Category(1L, "Пиццы", "Вкусные пиццы на любой вкус", "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400"),
            Category(2L, "Бургеры", "Сочные бургеры и сэндвичи", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400"),
            Category(3L, "Закуски", "Разнообразные закуски и снеки", "https://images.unsplash.com/photo-1541592106381-b31e9677c0e5?w=400"),
            Category(4L, "Напитки", "Освежающие напитки", "https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=400"),
            Category(5L, "Десерты", "Сладкие десерты", "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=400")
        )
        
        return Result.success(mockCategories)
    }
    
    override suspend fun createCategory(category: Category): Result<Category> {
        delay(800)
        return Result.success(category.copy(id = System.currentTimeMillis()))
    }
    
    override suspend fun updateCategory(category: Category): Result<Category> {
        delay(600)
        return Result.success(category)
    }
    
    override suspend fun deleteCategory(categoryId: Long): Result<Unit> {
        delay(500)
        return Result.success(Unit)
    }
    
    override suspend fun getPopularProducts(limit: Int): Result<List<PopularProduct>> {
        delay(400)
        
        val popularProducts = listOf(
            PopularProduct(1L, "Пицца Маргарита", 45, 22500.0),
            PopularProduct(2L, "Пицца Пепперони", 38, 19000.0),
            PopularProduct(3L, "Пицца 4 Сыра", 32, 16800.0),
            PopularProduct(4L, "Пицца Гавайская", 28, 14000.0),
            PopularProduct(5L, "Пицца Мясная", 25, 15000.0)
        ).take(limit)
        
        return Result.success(popularProducts)
    }
    
    override suspend fun getRecentOrders(limit: Int): Result<List<Order>> {
        delay(300)
        return Result.success(emptyList())
    }
    
    override suspend fun getRevenueStats(days: Int): Result<Double> {
        delay(300)
        return Result.success(156800.50)
    }
} 