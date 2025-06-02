/**
 * @file: MockProductRepositoryImpl.kt
 * @description: Мок-реализация репозитория продуктов с тестовыми данными
 * @dependencies: Product, Category entities
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.repositories

import com.pizzanat.app.domain.entities.Category
import com.pizzanat.app.domain.entities.Product
import com.pizzanat.app.domain.repositories.ProductRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockProductRepositoryImpl @Inject constructor() : ProductRepository {
    
    private val mockCategories = listOf(
        Category(1L, "Пицца", "Вкусная пицца на любой вкус", "https://example.com/pizza.jpg"),
        Category(2L, "Бургеры", "Сочные бургеры с разными начинками", "https://example.com/burger.jpg"),
        Category(3L, "Салаты", "Свежие и полезные салаты", "https://example.com/salad.jpg"),
        Category(4L, "Напитки", "Прохладные напитки", "https://example.com/drinks.jpg"),
        Category(5L, "Десерты", "Сладкие десерты", "https://example.com/dessert.jpg")
    )
    
    private val mockProducts = listOf(
        // Пиццы
        Product(
            id = 1L, 
            name = "Маргарита", 
            description = "Классическая пицца с томатами и моцареллой", 
            price = 890.0, 
            categoryId = 1L, 
            imageUrl = "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400"
        ),
        Product(
            id = 2L, 
            name = "Пепперони", 
            description = "Пицца с острой колбасой пепперони", 
            price = 1200.0, 
            categoryId = 1L, 
            imageUrl = "https://images.unsplash.com/photo-1628840042765-356cda07504e?w=400"
        ),
        Product(
            id = 3L, 
            name = "Четыре сыра", 
            description = "Пицца с четырьмя видами сыра", 
            price = 1350.0, 
            categoryId = 1L, 
            imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400"
        ),
        Product(
            id = 4L, 
            name = "Гавайская", 
            description = "Пицца с ананасами и ветчиной", 
            price = 1100.0, 
            categoryId = 1L, 
            imageUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400"
        ),
        
        // Бургеры  
        Product(
            id = 5L, 
            name = "Чизбургер", 
            description = "Классический бургер с сыром", 
            price = 450.0, 
            categoryId = 2L, 
            imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400"
        ),
        Product(
            id = 6L, 
            name = "Биг Мак", 
            description = "Большой бургер с двумя котлетами", 
            price = 650.0, 
            categoryId = 2L, 
            imageUrl = "https://images.unsplash.com/photo-1550547660-d9450f859349?w=400"
        ),
        Product(
            id = 7L, 
            name = "Веган бургер", 
            description = "Бургер с растительной котлетой", 
            price = 550.0, 
            categoryId = 2L, 
            imageUrl = "https://images.unsplash.com/photo-1525059696034-4967a729002e?w=400"
        ),
        
        // Салаты
        Product(
            id = 8L, 
            name = "Цезарь", 
            description = "Салат с курицей и пармезаном", 
            price = 380.0, 
            categoryId = 3L, 
            imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400"
        ),
        Product(
            id = 9L, 
            name = "Греческий", 
            description = "Традиционный греческий салат", 
            price = 320.0, 
            categoryId = 3L, 
            imageUrl = "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=400"
        ),
        
        // Напитки
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
        
        // Десерты
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
    
    override suspend fun getCategories(): Result<List<Category>> {
        delay(500) // Симуляция сетевой задержки
        return Result.success(mockCategories)
    }
    
    override suspend fun getProductsByCategory(
        categoryId: Long,
        page: Int,
        size: Int
    ): Result<List<Product>> {
        delay(300)
        val products = mockProducts.filter { it.categoryId == categoryId }
        return Result.success(products)
    }
    
    override suspend fun getProductById(productId: Long): Result<Product> {
        delay(200)
        val product = mockProducts.find { it.id == productId }
        return if (product != null) {
            Result.success(product)
        } else {
            Result.failure(Exception("Продукт не найден"))
        }
    }
    
    override suspend fun searchProducts(
        query: String,
        page: Int,
        size: Int
    ): Result<List<Product>> {
        delay(400)
        val filteredProducts = mockProducts.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.description.contains(query, ignoreCase = true) 
        }
        return Result.success(filteredProducts)
    }
    
    override suspend fun getSpecialOffers(): Result<List<Product>> {
        delay(300)
        // Возвращаем случайные 5 продуктов как спецпредложения
        val offers = mockProducts.shuffled().take(5)
        return Result.success(offers)
    }
    
    override fun getFavoriteProducts(): Flow<List<Product>> {
        // Возвращаем пустой список, так как избранное пока не реализовано
        return flowOf(emptyList())
    }
    
    override suspend fun addToFavorites(productId: Long) {
        // Заглушка для добавления в избранное
    }
    
    override suspend fun removeFromFavorites(productId: Long) {
        // Заглушка для удаления из избранного
    }
} 