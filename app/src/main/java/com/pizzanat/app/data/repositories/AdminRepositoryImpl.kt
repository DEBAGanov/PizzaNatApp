/**
 * @file: AdminRepositoryImpl.kt
 * @description: Реализация AdminRepository с полной интеграцией API (без mock fallback)
 * @dependencies: AdminRepository, AdminApiService, OrderApiService, ProductApiService
 * @created: 2024-12-19
 * @updated: 2024-12-20 - Убраны все mock данные, только реальный API
 */
package com.pizzanat.app.data.repositories

import android.util.Log
import com.pizzanat.app.data.mappers.toDomain as adminStatsMapperToDomain
import com.pizzanat.app.data.mappers.toDomain as productMapperToDomain
import com.pizzanat.app.data.mappers.toDomain as categoryMapperToDomain
import com.pizzanat.app.data.mappers.toDomain as orderMapperToDomain
import com.pizzanat.app.data.mappers.toDomain
import com.pizzanat.app.data.mappers.toCreateRequest
import com.pizzanat.app.data.mappers.toUpdateRequest
import com.pizzanat.app.data.mappers.toProductsDomain
import com.pizzanat.app.data.remote.api.AdminApiService
import com.pizzanat.app.data.remote.api.OrderApiService
import com.pizzanat.app.data.remote.api.ProductApiService
import com.pizzanat.app.data.network.api.AuthApiService
import com.pizzanat.app.data.network.dto.LoginRequestDto
import com.pizzanat.app.data.repositories.TokenManager
import com.pizzanat.app.data.remote.util.safeApiCall
import com.pizzanat.app.domain.entities.*
import com.pizzanat.app.domain.repositories.AdminRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val adminApiService: AdminApiService,
    private val orderApiService: OrderApiService,
    private val productApiService: ProductApiService,
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : AdminRepository {

    private var currentAdmin: AdminUser? = null

    override suspend fun loginAdmin(username: String, password: String): Result<AdminUser> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("AdminRepository", "🔐 Авторизация админа через API: $username")
            
            val loginRequest = LoginRequestDto(username, password)
            val apiResult = safeApiCall { authApiService.login(loginRequest) }
            
            if (apiResult.isSuccess) {
                val authResponse = apiResult.getOrNull()
                if (authResponse != null) {
                    // Сохраняем админский токен
                    tokenManager.saveToken(authResponse.token)
                    
                    // Создаем AdminUser из ответа API
                    val adminUser = AdminUser(
                        id = authResponse.userId,
                        username = authResponse.username,
                        email = authResponse.email,
                        firstName = authResponse.firstName,
                        lastName = authResponse.lastName,
                        role = AdminRole.SUPER_ADMIN, // Предполагаем что admin - супер админ
                        permissions = AdminPermission.values().toSet(), // Полные права
                        isActive = true,
                        lastLoginAt = LocalDateTime.now(),
                        createdAt = LocalDateTime.now()
                    )
                    
                    currentAdmin = adminUser
                    Log.d("AdminRepository", "✅ Админ авторизован: ${adminUser.username}")
                    Result.success(adminUser)
                } else {
                    Log.e("AdminRepository", "❌ Пустой ответ API авторизации")
                    Result.failure(Exception("Ошибка авторизации: пустой ответ сервера"))
                }
            } else {
                Log.e("AdminRepository", "❌ Ошибка API авторизации: ${apiResult.getErrorMessage()}")
                Result.failure(Exception("Ошибка авторизации: ${apiResult.getErrorMessage()}"))
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "❌ Исключение при авторизации: ${e.message}")
            Result.failure(Exception("Ошибка авторизации: ${e.message}"))
        }
    }

    override suspend fun getCurrentAdmin(): Result<AdminUser?> {
        return Result.success(currentAdmin)
    }

    override suspend fun logoutAdmin(): Result<Unit> {
        Log.d("AdminRepository", "🚪 Выход админа: ${currentAdmin?.username}")
        currentAdmin = null
        tokenManager.clearToken()
        return Result.success(Unit)
    }

    override suspend fun getAdminStats(): Result<AdminStats> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("AdminRepository", "📊 Запрос статистики админа")
            val apiResult = safeApiCall { adminApiService.getAdminStats() }
            
            if (apiResult.isSuccess) {
                val statsDto = apiResult.getOrNull()
                if (statsDto != null) {
                    val stats = statsDto.adminStatsMapperToDomain()
                    Log.d("AdminRepository", "✅ Статистика загружена с API")
                    Result.success(stats)
                } else {
                    Log.e("AdminRepository", "❌ Пустой ответ API статистики")
                    Result.failure(Exception("Статистика недоступна"))
                }
            } else {
                Log.e("AdminRepository", "❌ Ошибка API статистики: ${apiResult.getErrorMessage()}")
                Result.failure(Exception("Ошибка загрузки статистики: ${apiResult.getErrorMessage()}"))
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "❌ Исключение при получении статистики: ${e.message}")
            Result.failure(Exception("Ошибка получения статистики: ${e.message}"))
        }
    }

    override fun getAdminStatsFlow(): Flow<AdminStats> = flow {
        while (true) {
            val result = getAdminStats()
            if (result.isSuccess) {
                result.getOrNull()?.let { emit(it) }
            }
            kotlinx.coroutines.delay(30000) // Обновляем каждые 30 секунд
        }
    }

    override suspend fun getAllOrders(page: Int, size: Int): Result<List<Order>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("AdminRepository", "📦 Запрос всех заказов через API")
            
            val allOrders = mutableListOf<Order>()
            var currentPage = 0
            var hasMorePages = true
            
            // Загружаем все страницы
            while (hasMorePages) {
                Log.d("AdminRepository", "📄 Загружаем страницу $currentPage")
                val apiResult = safeApiCall { 
                    orderApiService.getAllOrders(page = currentPage, size = 100) 
                }
                
                if (apiResult.isSuccess) {
                    val adminOrdersPageResponse = apiResult.getOrNull()
                    if (adminOrdersPageResponse != null) {
                        val orders = adminOrdersPageResponse.toDomain()
                        allOrders.addAll(orders)
                        
                        Log.d("AdminRepository", "✅ Страница $currentPage: ${orders.size} заказов")
                        
                        hasMorePages = !adminOrdersPageResponse.last && currentPage < (adminOrdersPageResponse.totalPages - 1)
                        currentPage++
                        
                        if (currentPage > 10) {
                            Log.w("AdminRepository", "⚠️ Остановка: максимум страниц достигнут")
                            break
                        }
                    } else {
                        hasMorePages = false
                    }
                } else {
                    Log.e("AdminRepository", "❌ Ошибка загрузки страницы $currentPage: ${apiResult.getErrorMessage()}")
                    if (currentPage == 0) {
                        return@withContext Result.failure(Exception("Не удалось загрузить заказы: ${apiResult.getErrorMessage()}"))
                    }
                    hasMorePages = false
                }
            }
            
            // Сортируем по дате создания
            val sortedOrders = allOrders.sortedByDescending { it.createdAt }
            Log.d("AdminRepository", "✅ Загружено всего заказов: ${sortedOrders.size}")
            Result.success(sortedOrders)
            
        } catch (e: Exception) {
            Log.e("AdminRepository", "❌ Исключение при загрузке заказов: ${e.message}")
            Result.failure(Exception("Ошибка загрузки заказов: ${e.message}"))
        }
    }

    override suspend fun updateOrderStatus(orderId: Long, status: OrderStatus): Result<Order> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("AdminRepository", "🔄 Обновление статуса заказа $orderId на $status")
            // Пока Admin API не готов, возвращаем ошибку
            Result.failure(Exception("Обновление статуса заказов пока недоступно - Admin API не готов"))
        } catch (e: Exception) {
            Log.e("AdminRepository", "❌ Исключение при обновлении статуса: ${e.message}")
            Result.failure(Exception("Ошибка обновления статуса: ${e.message}"))
        }
    }

    override fun getAllOrdersFlow(): Flow<List<Order>> = flow {
        while (true) {
            val result = getAllOrders()
            if (result.isSuccess) {
                result.getOrNull()?.let { emit(it) }
            }
            kotlinx.coroutines.delay(10000) // Обновляем каждые 10 секунд
        }
    }

    override suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("AdminRepository", "📋 Запрос заказов по статусу: $status")
            // Получаем все заказы и фильтруем по статусу
            val allOrdersResult = getAllOrders()
            if (allOrdersResult.isSuccess) {
                val filteredOrders = allOrdersResult.getOrNull()?.filter { it.status == status } ?: emptyList()
                Log.d("AdminRepository", "✅ Найдено заказов со статусом $status: ${filteredOrders.size}")
                Result.success(filteredOrders)
            } else {
                Result.failure(Exception("Не удалось загрузить заказы: ${allOrdersResult.exceptionOrNull()?.message}"))
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "❌ Исключение при получении заказов по статусу: ${e.message}")
            Result.failure(Exception("Ошибка получения заказов по статусу: ${e.message}"))
        }
    }

    override suspend fun getAllProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("AdminRepository", "🛍️ Запрос всех продуктов")
            val apiResult = safeApiCall { productApiService.getAllProducts() }
            
            if (apiResult.isSuccess) {
                val productsDto = apiResult.getOrNull()
                if (productsDto != null) {
                    val products = productsDto.toProductsDomain()
                    Log.d("AdminRepository", "✅ Загружено продуктов: ${products.size}")
                    Result.success(products)
                } else {
                    Result.failure(Exception("Продукты недоступны"))
                }
            } else {
                Result.failure(Exception("Ошибка загрузки продуктов: ${apiResult.getErrorMessage()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка загрузки продуктов: ${e.message}"))
        }
    }

    override suspend fun getAllCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("AdminRepository", "📂 Запрос всех категорий")
            val apiResult = safeApiCall { productApiService.getCategories() }
            
            if (apiResult.isSuccess) {
                val categoriesDto = apiResult.getOrNull()
                if (categoriesDto != null) {
                    val categories = categoriesDto.map { it.categoryMapperToDomain() }
                    Log.d("AdminRepository", "✅ Загружено категорий: ${categories.size}")
                    Result.success(categories)
                } else {
                    Result.failure(Exception("Категории недоступны"))
                }
            } else {
                Result.failure(Exception("Ошибка загрузки категорий: ${apiResult.getErrorMessage()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка загрузки категорий: ${e.message}"))
        }
    }

    override suspend fun createProduct(product: Product): Result<Product> {
        return Result.failure(Exception("Создание продуктов пока недоступно - Admin API не готов"))
    }

    override suspend fun updateProduct(product: Product): Result<Product> {
        return Result.failure(Exception("Обновление продуктов пока недоступно - Admin API не готов"))
    }

    override suspend fun deleteProduct(productId: Long): Result<Unit> {
        return Result.failure(Exception("Удаление продуктов пока недоступно - Admin API не готов"))
    }

    override suspend fun createCategory(category: Category): Result<Category> {
        return Result.failure(Exception("Создание категорий пока недоступно - Admin API не готов"))
    }

    override suspend fun updateCategory(category: Category): Result<Category> {
        return Result.failure(Exception("Обновление категорий пока недоступно - Admin API не готов"))
    }

    override suspend fun deleteCategory(categoryId: Long): Result<Unit> {
        return Result.failure(Exception("Удаление категорий пока недоступно - Admin API не готов"))
    }

    override suspend fun getPopularProducts(limit: Int): Result<List<PopularProduct>> {
        return Result.failure(Exception("Популярные продукты пока недоступны - Admin API не готов"))
    }

    override suspend fun getRecentOrders(limit: Int): Result<List<Order>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("AdminRepository", "📅 Запрос последних заказов (limit: $limit)")
            val result = getAllOrders(page = 0, size = limit)
            if (result.isSuccess) {
                val orders = result.getOrNull()?.sortedByDescending { it.createdAt }?.take(limit) ?: emptyList()
                Log.d("AdminRepository", "✅ Загружено последних заказов: ${orders.size}")
                Result.success(orders)
            } else {
                Result.failure(Exception("Не удалось загрузить последние заказы: ${result.exceptionOrNull()?.message}"))
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "❌ Исключение при получении последних заказов: ${e.message}")
            Result.failure(Exception("Ошибка получения последних заказов: ${e.message}"))
        }
    }

    override suspend fun getRevenueStats(days: Int): Result<Double> {
        return Result.failure(Exception("Статистика доходов пока недоступна - Admin API не готов"))
    }
} 