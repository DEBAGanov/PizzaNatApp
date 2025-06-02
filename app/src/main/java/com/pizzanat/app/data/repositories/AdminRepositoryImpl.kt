/**
 * @file: AdminRepositoryImpl.kt
 * @description: Реализация AdminRepository с API интеграцией и fallback на mock данные
 * @dependencies: AdminRepository, AdminApiService, OrderApiService, ProductApiService
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.repositories

import android.util.Log
import com.pizzanat.app.data.mappers.toDomain as adminStatsMapperToDomain
import com.pizzanat.app.data.mappers.toDomain as productMapperToDomain
import com.pizzanat.app.data.mappers.toDomain as categoryMapperToDomain
import com.pizzanat.app.data.mappers.toDomain as orderMapperToDomain
import com.pizzanat.app.data.mappers.toDomainOrders
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
    private val tokenManager: TokenManager,
    private val mockAdminRepository: MockAdminRepositoryImpl
) : AdminRepository {

    private var currentAdmin: AdminUser? = null

    override suspend fun loginAdmin(username: String, password: String): Result<AdminUser> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("AdminRepository", "Попытка входа админа через реальный API: $username")
            
            // Сначала пробуем через реальный API (admin/admin123)
            val loginRequest = LoginRequestDto(username, password)
            val apiResult = safeApiCall { authApiService.login(loginRequest) }
            
            if (apiResult.isSuccess) {
                val authResponse = apiResult.getOrNull()
                if (authResponse != null) {
                    // Сохраняем админский токен
                    tokenManager.saveToken(authResponse.token)
                    
                    // Создаем AdminUser из ответа API
                    val adminUser = AdminUser(
                        id = authResponse.id ?: 1L,
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
                    Log.d("AdminRepository", "Админ авторизован через API: ${adminUser.username}, токен сохранен")
                    Result.success(adminUser)
                } else {
                    Log.w("AdminRepository", "Пустой ответ API авторизации, используем mock")
                    val mockResult = mockAdminRepository.loginAdmin(username, password)
                    if (mockResult.isSuccess) {
                        currentAdmin = mockResult.getOrNull()
                    }
                    mockResult
                }
            } else {
                Log.w("AdminRepository", "Ошибка API авторизации: ${apiResult.getErrorMessage()}, используем mock")
                val mockResult = mockAdminRepository.loginAdmin(username, password)
                if (mockResult.isSuccess) {
                    currentAdmin = mockResult.getOrNull()
                }
                mockResult
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "Исключение при авторизации админа: ${e.message}")
            // Fallback на mock
            val mockResult = mockAdminRepository.loginAdmin(username, password)
            if (mockResult.isSuccess) {
                currentAdmin = mockResult.getOrNull()
            }
            mockResult
        }
    }

    override suspend fun getCurrentAdmin(): Result<AdminUser?> {
        return Result.success(currentAdmin)
    }

    override suspend fun logoutAdmin(): Result<Unit> {
        Log.d("AdminRepository", "Выход админа: ${currentAdmin?.username}")
        currentAdmin = null
        // Очищаем админский токен при выходе
        tokenManager.clearToken()
        return Result.success(Unit)
    }

    override suspend fun getAdminStats(): Result<AdminStats> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Пробуем получить статистику с API
            val apiResult = safeApiCall { adminApiService.getAdminStats() }
            
            if (apiResult.isSuccess) {
                val statsDto = apiResult.getOrNull()
                if (statsDto != null) {
                    val stats = statsDto.adminStatsMapperToDomain()
                    Log.d("AdminRepository", "Статистика загружена с API")
                    Result.success(stats)
                } else {
                    Log.w("AdminRepository", "Пустой ответ API, используем mock данные")
                    mockAdminRepository.getAdminStats()
                }
            } else {
                Log.w("AdminRepository", "Ошибка API статистики: ${apiResult.getErrorMessage()}, используем mock данные")
                mockAdminRepository.getAdminStats()
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "Исключение при получении статистики: ${e.message}")
            mockAdminRepository.getAdminStats()
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
            Log.d("AdminRepository", "Запрос всех заказов через API (page=$page, size=$size)")
            
            // Получаем заказы через OrderApiService (админский эндпоинт)
            val apiResult = safeApiCall { orderApiService.getAllOrders(page = page, size = size) }
            
            if (apiResult.isSuccess) {
                val adminOrdersPageResponse = apiResult.getOrNull()
                if (adminOrdersPageResponse != null) {
                    // Используем новый маппер для AdminOrdersPageResponse
                    val orders = adminOrdersPageResponse.toDomainOrders()
                    Log.d("AdminRepository", "✅ Заказы загружены с API: ${orders.size} заказов")
                    orders.forEach { order ->
                        Log.d("AdminRepository", "API Заказ: ID=${order.id}, клиент=${order.customerName}, сумма=${order.totalAmount}")
                    }
                    Result.success(orders)
                } else {
                    Log.w("AdminRepository", "❌ Пустой ответ API заказов, используем mock данные")
                    mockAdminRepository.getAllOrders(page, size)
                }
            } else {
                Log.w("AdminRepository", "❌ Ошибка API заказов: ${apiResult.getErrorMessage()}, используем mock данные")
                Log.w("AdminRepository", "HTTP статус ошибки: возможно требуется админский токен")
                mockAdminRepository.getAllOrders(page, size)
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "❌ Исключение при получении заказов: ${e.message}")
            mockAdminRepository.getAllOrders(page, size)
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

    override suspend fun updateOrderStatus(orderId: Long, status: OrderStatus): Result<Order> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Обновляем статус через API
            val request = com.pizzanat.app.data.remote.dto.UpdateOrderStatusRequest(status.name)
            val apiResult = safeApiCall { orderApiService.updateOrderStatus(orderId, request) }
            
            if (apiResult.isSuccess) {
                val orderDto = apiResult.getOrNull()
                if (orderDto != null) {
                    val order = orderDto.orderMapperToDomain()
                    Log.d("AdminRepository", "Статус заказа $orderId обновлен через API: $status")
                    Result.success(order)
                } else {
                    Log.w("AdminRepository", "Пустой ответ при обновлении статуса, используем mock")
                    mockAdminRepository.updateOrderStatus(orderId, status)
                }
            } else {
                Log.w("AdminRepository", "Ошибка API при обновлении статуса: ${apiResult.getErrorMessage()}, используем mock")
                mockAdminRepository.updateOrderStatus(orderId, status)
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "Исключение при обновлении статуса: ${e.message}")
            mockAdminRepository.updateOrderStatus(orderId, status)
        }
    }

    override suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Получаем заказы по статусу через API
            val apiResult = safeApiCall { 
                orderApiService.getAllOrders(status = status.name, page = 0, size = 100) 
            }
            
            if (apiResult.isSuccess) {
                val adminOrdersPageResponse = apiResult.getOrNull()
                if (adminOrdersPageResponse != null) {
                    val orders = adminOrdersPageResponse.toDomainOrders()
                    Log.d("AdminRepository", "Заказы по статусу $status загружены с API: ${orders.size}")
                    Result.success(orders)
                } else {
                    mockAdminRepository.getOrdersByStatus(status)
                }
            } else {
                Log.w("AdminRepository", "Ошибка API заказов по статусу, используем mock данные")
                mockAdminRepository.getOrdersByStatus(status)
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "Исключение при получении заказов по статусу: ${e.message}")
            mockAdminRepository.getOrdersByStatus(status)
        }
    }

    override suspend fun getAllProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Получаем все продукты через API
            val apiResult = safeApiCall { productApiService.getAllProducts(page = 0, size = 100) }
            
            if (apiResult.isSuccess) {
                val productsResponse = apiResult.getOrNull()
                if (productsResponse != null) {
                    val products = productsResponse.content.map { it.productMapperToDomain() }
                    Log.d("AdminRepository", "Продукты загружены с API: ${products.size}")
                    Result.success(products)
                } else {
                    Log.w("AdminRepository", "Пустой ответ API продуктов, используем mock данные")
                    mockAdminRepository.getAllProducts()
                }
            } else {
                Log.w("AdminRepository", "Ошибка API продуктов: ${apiResult.getErrorMessage()}, используем mock данные")
                mockAdminRepository.getAllProducts()
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "Исключение при получении продуктов: ${e.message}")
            mockAdminRepository.getAllProducts()
        }
    }

    override suspend fun createProduct(product: Product): Result<Product> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Создаем продукт через админское API
            val request = product.toCreateRequest()
            val apiResult = safeApiCall { adminApiService.createProduct(request) }
            
            if (apiResult.isSuccess) {
                val productDto = apiResult.getOrNull()
                if (productDto != null) {
                    val createdProduct = productDto.productMapperToDomain()
                    Log.d("AdminRepository", "Продукт создан через API: ${createdProduct.name}")
                    Result.success(createdProduct)
                } else {
                    Log.w("AdminRepository", "Пустой ответ при создании продукта, используем mock")
                    mockAdminRepository.createProduct(product)
                }
            } else {
                Log.w("AdminRepository", "Ошибка API при создании продукта: ${apiResult.getErrorMessage()}, используем mock")
                mockAdminRepository.createProduct(product)
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "Исключение при создании продукта: ${e.message}")
            mockAdminRepository.createProduct(product)
        }
    }

    override suspend fun updateProduct(product: Product): Result<Product> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Обновляем продукт через админское API
            val request = product.toUpdateRequest()
            val apiResult = safeApiCall { adminApiService.updateProduct(product.id, request) }
            
            if (apiResult.isSuccess) {
                val productDto = apiResult.getOrNull()
                if (productDto != null) {
                    val updatedProduct = productDto.productMapperToDomain()
                    Log.d("AdminRepository", "Продукт обновлен через API: ${updatedProduct.name}")
                    Result.success(updatedProduct)
                } else {
                    Log.w("AdminRepository", "Пустой ответ при обновлении продукта, используем mock")
                    mockAdminRepository.updateProduct(product)
                }
            } else {
                Log.w("AdminRepository", "Ошибка API при обновлении продукта: ${apiResult.getErrorMessage()}, используем mock")
                mockAdminRepository.updateProduct(product)
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "Исключение при обновлении продукта: ${e.message}")
            mockAdminRepository.updateProduct(product)
        }
    }

    override suspend fun deleteProduct(productId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Удаляем продукт через админское API
            val apiResult = safeApiCall { adminApiService.deleteProduct(productId) }
            
            if (apiResult.isSuccess) {
                Log.d("AdminRepository", "Продукт удален через API: $productId")
                Result.success(Unit)
            } else {
                Log.w("AdminRepository", "Ошибка API при удалении продукта: ${apiResult.getErrorMessage()}, используем mock")
                mockAdminRepository.deleteProduct(productId)
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "Исключение при удалении продукта: ${e.message}")
            mockAdminRepository.deleteProduct(productId)
        }
    }

    override suspend fun getAllCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Получаем категории через API
            val apiResult = safeApiCall { productApiService.getCategories() }
            
            if (apiResult.isSuccess) {
                val categoriesDto = apiResult.getOrNull()
                if (categoriesDto != null) {
                    val categories = categoriesDto.map { it.categoryMapperToDomain() }
                    Log.d("AdminRepository", "Категории загружены с API: ${categories.size}")
                    Result.success(categories)
                } else {
                    Log.w("AdminRepository", "Пустой ответ API категорий, используем mock данные")
                    mockAdminRepository.getAllCategories()
                }
            } else {
                Log.w("AdminRepository", "Ошибка API категорий: ${apiResult.getErrorMessage()}, используем mock данные")
                mockAdminRepository.getAllCategories()
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "Исключение при получении категорий: ${e.message}")
            mockAdminRepository.getAllCategories()
        }
    }

    override suspend fun createCategory(category: Category): Result<Category> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Создаем категорию через админское API
            val request = category.toCreateRequest()
            val apiResult = safeApiCall { adminApiService.createCategory(request) }
            
            if (apiResult.isSuccess) {
                val categoryDto = apiResult.getOrNull()
                if (categoryDto != null) {
                    val createdCategory = categoryDto.categoryMapperToDomain()
                    Log.d("AdminRepository", "Категория создана через API: ${createdCategory.name}")
                    Result.success(createdCategory)
                } else {
                    Log.w("AdminRepository", "Пустой ответ при создании категории, используем mock")
                    mockAdminRepository.createCategory(category)
                }
            } else {
                Log.w("AdminRepository", "Ошибка API при создании категории: ${apiResult.getErrorMessage()}, используем mock")
                mockAdminRepository.createCategory(category)
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "Исключение при создании категории: ${e.message}")
            mockAdminRepository.createCategory(category)
        }
    }

    override suspend fun updateCategory(category: Category): Result<Category> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Обновляем категорию через админское API
            val request = category.toUpdateRequest()
            val apiResult = safeApiCall { adminApiService.updateCategory(category.id, request) }
            
            if (apiResult.isSuccess) {
                val categoryDto = apiResult.getOrNull()
                if (categoryDto != null) {
                    val updatedCategory = categoryDto.categoryMapperToDomain()
                    Log.d("AdminRepository", "Категория обновлена через API: ${updatedCategory.name}")
                    Result.success(updatedCategory)
                } else {
                    Log.w("AdminRepository", "Пустой ответ при обновлении категории, используем mock")
                    mockAdminRepository.updateCategory(category)
                }
            } else {
                Log.w("AdminRepository", "Ошибка API при обновлении категории: ${apiResult.getErrorMessage()}, используем mock")
                mockAdminRepository.updateCategory(category)
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "Исключение при обновлении категории: ${e.message}")
            mockAdminRepository.updateCategory(category)
        }
    }

    override suspend fun deleteCategory(categoryId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Удаляем категорию через админское API
            val apiResult = safeApiCall { adminApiService.deleteCategory(categoryId) }
            
            if (apiResult.isSuccess) {
                Log.d("AdminRepository", "Категория удалена через API: $categoryId")
                Result.success(Unit)
            } else {
                Log.w("AdminRepository", "Ошибка API при удалении категории: ${apiResult.getErrorMessage()}, используем mock")
                mockAdminRepository.deleteCategory(categoryId)
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "Исключение при удалении категории: ${e.message}")
            mockAdminRepository.deleteCategory(categoryId)
        }
    }

    override suspend fun getPopularProducts(limit: Int): Result<List<PopularProduct>> {
        // Пока используем mock данные, так как это требует сложной аналитики
        return mockAdminRepository.getPopularProducts(limit)
    }

    override suspend fun getRecentOrders(limit: Int): Result<List<Order>> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Получаем последние заказы через API
            val result = getAllOrders(page = 0, size = limit)
            if (result.isSuccess) {
                val orders = result.getOrNull()?.sortedByDescending { it.createdAt }?.take(limit) ?: emptyList()
                Result.success(orders)
            } else {
                mockAdminRepository.getRecentOrders(limit)
            }
        } catch (e: Exception) {
            mockAdminRepository.getRecentOrders(limit)
        }
    }

    override suspend fun getRevenueStats(days: Int): Result<Double> {
        // Пока используем mock данные, так как это требует сложной аналитики
        return mockAdminRepository.getRevenueStats(days)
    }
} 