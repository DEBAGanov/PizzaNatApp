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
        Product(1L, "Маргарита", "Классическая пицца с томатами и моцареллой", 890.0, "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400", 1L, true),
        Product(2L, "Пепперони", "Пицца с острой колбасой пепперони", 1200.0, "https://images.unsplash.com/photo-1628840042765-356cda07504e?w=400", 1L, true),
        Product(3L, "Четыре сыра", "Пицца с четырьмя видами сыра", 1350.0, "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400", 1L, true),
        Product(4L, "Гавайская", "Пицца с ананасами и ветчиной", 1100.0, "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400", 1L, true),
        
        // Бургеры  
        Product(5L, "Чизбургер", "Классический бургер с сыром", 450.0, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400", 2L, true),
        Product(6L, "Биг Мак", "Большой бургер с двумя котлетами", 650.0, "https://images.unsplash.com/photo-1550547660-d9450f859349?w=400", 2L, true),
        Product(7L, "Веган бургер", "Бургер с растительной котлетой", 550.0, "https://images.unsplash.com/photo-1525059696034-4967a729002e?w=400", 2L, true),
        
        // Салаты
        Product(8L, "Цезарь", "Салат с курицей и пармезаном", 380.0, "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400", 3L, true),
        Product(9L, "Греческий", "Традиционный греческий салат", 320.0, "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=400", 3L, true),
        
        // Напитки
        Product(10L, "Кока-кола", "Классическая кола 0.5л", 120.0, "https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=400", 4L, true),
        Product(11L, "Апельсиновый сок", "Свежевыжатый сок 0.3л", 180.0, "https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?w=400", 4L, true),
        
        // Десерты
        Product(12L, "Тирамису", "Классический итальянский десерт", 290.0, "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=400", 5L, true),
        Product(13L, "Чизкейк", "Нежный чизкейк с ягодами", 350.0, "https://images.unsplash.com/photo-1567958499588-feb1aaed8042?w=400", 5L, true)
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