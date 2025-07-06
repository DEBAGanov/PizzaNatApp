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

        // Обновленная статистика, соответствующая реальным данным из микросервиса
        val stats = AdminStats(
            totalOrders = 6, // Реальное количество заказов в БД
            todayOrders = 6, // Все заказы сегодняшние (за последние дни)
            pendingOrders = 6, // Все заказы в статусе CREATED (ожидают обработки)
            completedOrders = 0, // Пока нет завершенных заказов
            totalRevenue = 3135.15, // 499+499+499+629+509.15 = реальная сумма из БД
            todayRevenue = 3135.15, // Вся выручка сегодняшняя
            totalProducts = 25, // Количество продуктов (может оставить как есть)
            totalCategories = 5, // Количество категорий
            totalUsers = 6, // Примерно равно количеству заказов
            popularProducts = listOf(
                PopularProduct(1L, "Пицца Маргарита", 3, 1497.0), // 3 заказа по 499
                PopularProduct(8L, "Пицца Карбонара", 1, 629.0), // 1 заказ на 629
                PopularProduct(12L, "Бургер \"Чизбургер\"", 1, 509.15), // 1 заказ на 509.15
                PopularProduct(11L, "Бургер \"Классический\"", 1, 499.0) // 1 заказ на 499
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
                paymentMethod = PaymentMethod.CARD_ON_DELIVERY,
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
                paymentMethod = PaymentMethod.SBP,
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
                paymentMethod = PaymentMethod.CARD_ON_DELIVERY,
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
            Product(
                id = 1L,
                name = "Пицца Маргарита",
                description = "Классическая пицца с томатами, моцареллой и базиликом",
                price = 650.0,
                categoryId = 1L,
                imageUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400"
            ),
            Product(
                id = 2L,
                name = "Пицца Пепперони",
                description = "Острая пицца с пепперони и сыром моцарелла",
                price = 750.0,
                categoryId = 1L,
                imageUrl = "https://images.unsplash.com/photo-1628840042765-356cda07504e?w=400"
            ),
            Product(
                id = 3L,
                name = "Пицца 4 Сыра",
                description = "Пицца с четырьмя видами сыра",
                price = 890.0,
                categoryId = 1L,
                imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400"
            ),
            Product(
                id = 4L,
                name = "Пицца Мясная",
                description = "Сытная пицца с курицей, беконом и колбасой",
                price = 950.0,
                categoryId = 1L,
                imageUrl = "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=400"
            ),
            Product(
                id = 5L,
                name = "Пицца Гавайская",
                description = "Пицца с ананасами и ветчиной",
                price = 820.0,
                categoryId = 1L,
                imageUrl = "https://images.unsplash.com/photo-1576458088443-04a19d8a06b0?w=400",
                available = false
            ),
            Product(
                id = 6L,
                name = "Бургер Классический",
                description = "Говяжья котлета с овощами и соусом",
                price = 450.0,
                categoryId = 2L,
                imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400"
            ),
            Product(
                id = 7L,
                name = "Чизбургер",
                description = "Бургер с двойным сыром",
                price = 520.0,
                categoryId = 2L,
                imageUrl = "https://images.unsplash.com/photo-1553979459-d2229ba7433a?w=400"
            ),
            Product(
                id = 8L,
                name = "Картофель фри",
                description = "Хрустящий картофель фри",
                price = 280.0,
                categoryId = 3L,
                imageUrl = "https://images.unsplash.com/photo-1541592106381-b31e9677c0e5?w=400"
            ),
            Product(
                id = 9L,
                name = "Крылышки BBQ",
                description = "Острые куриные крылышки в соусе BBQ",
                price = 620.0,
                categoryId = 3L,
                imageUrl = "https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=400"
            ),
            Product(
                id = 10L,
                name = "Кока-кола",
                description = "Классическая кола 0.5л",
                price = 120.0,
                categoryId = 4L,
                imageUrl = "https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=400"
            ),
            Product(
                id = 11L,
                name = "Апельсиновый сок",
                description = "Свежевыжатый сок 0.3л",
                price = 180.0,
                categoryId = 4L,
                imageUrl = "https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?w=400"
            ),
            Product(
                id = 12L,
                name = "Тирамису",
                description = "Классический итальянский десерт",
                price = 290.0,
                categoryId = 5L,
                imageUrl = "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=400"
            ),
            Product(
                id = 13L,
                name = "Чизкейк",
                description = "Нежный чизкейк с ягодами",
                price = 350.0,
                categoryId = 5L,
                imageUrl = "https://images.unsplash.com/photo-1567958499588-feb1aaed8042?w=400"
            )
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