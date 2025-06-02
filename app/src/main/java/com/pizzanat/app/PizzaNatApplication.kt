/**
 * @file: PizzaNatApplication.kt
 * @description: Основной класс приложения с инициализацией Hilt DI и оптимизированным ImageLoader
 * @dependencies: Hilt Android, Coil ImageLoader
 * @created: 2024-12-19
 * @updated: 2024-12-19 - Добавлена конфигурация ImageLoader для максимальной производительности
 */
package com.pizzanat.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class PizzaNatApplication : Application(), ImageLoaderFactory {
    
    /**
     * Создание оптимизированного ImageLoader для максимальной производительности
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            // Memory Cache - 25% ОЗУ для быстрого доступа к недавно загруженным изображениям
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // 25% доступной памяти
                    .build()
            }
            // Disk Cache - 150MB для долговременного хранения
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(150 * 1024 * 1024) // 150MB
                    .build()
            }
            // Оптимизированный OkHttpClient для загрузки изображений
            .okHttpClient {
                OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    // Максимум 4 одновременных соединения для изображений
                    .dispatcher(okhttp3.Dispatcher().apply {
                        maxRequests = 20
                        maxRequestsPerHost = 4
                    })
                    .build()
            }
            // Глобальные настройки для всех изображений
            .crossfade(300) // Плавная анимация появления
            .respectCacheHeaders(false) // Игнорируем проблемные S3 заголовки
            // Debug логирование в debug режиме
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }
} 