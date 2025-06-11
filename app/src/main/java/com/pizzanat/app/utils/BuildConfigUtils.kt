/**
 * @file: BuildConfigUtils.kt
 * @description: Утилиты для работы с конфигурацией сборки и средами
 * @dependencies: BuildConfig, Android Log
 * @created: 2024-12-20
 */
package com.pizzanat.app.utils

import android.util.Log
import com.pizzanat.app.BuildConfig

object BuildConfigUtils {

    private const val TAG = "BuildConfig"

    /**
     * Логирование текущей конфигурации приложения
     */
    fun logCurrentConfiguration() {
        Log.i(TAG, "================== КОНФИГУРАЦИЯ ПРИЛОЖЕНИЯ ==================")
        Log.i(TAG, "Версия приложения: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        Log.i(TAG, "Среда: ${BuildConfig.ENVIRONMENT}")
        Log.i(TAG, "Debug режим: ${BuildConfig.DEBUG}")
        Log.i(TAG, "API URL: ${BuildConfig.BASE_API_URL}")
        Log.i(TAG, "Mock данные: ${BuildConfig.USE_MOCK_DATA}")
        Log.i(TAG, "Application ID: ${BuildConfig.APPLICATION_ID}")
        Log.i(TAG, "Build Type: ${BuildConfig.BUILD_TYPE}")
        Log.i(TAG, "===============================================================")
    }

    /**
     * Проверка, является ли текущая сборка production
     */
    fun isProduction(): Boolean {
        return BuildConfig.ENVIRONMENT == "PRODUCTION"
    }

    /**
     * Проверка, является ли текущая сборка staging
     */
    fun isStaging(): Boolean {
        return BuildConfig.ENVIRONMENT == "STAGING"
    }

    /**
     * Проверка, является ли текущая сборка debug
     */
    fun isDebug(): Boolean {
        return BuildConfig.ENVIRONMENT == "DEBUG"
    }

    /**
     * Получение окружения в читаемом формате
     */
    fun getEnvironmentDisplayName(): String {
        return when (BuildConfig.ENVIRONMENT) {
            "PRODUCTION" -> "Продакшн"
            "STAGING" -> "Тестовый"
            "DEBUG" -> "Разработка"
            else -> "Неизвестно"
        }
    }

    /**
     * Получение полного API URL
     */
    fun getBaseUrl(): String {
        return BuildConfig.BASE_API_URL
    }

    /**
     * Проверка доступности Analytics
     */
    fun isAnalyticsEnabled(): Boolean {
        return isProduction() || isStaging()
    }

    /**
     * Проверка возможности crash reporting
     */
    fun isCrashReportingEnabled(): Boolean {
        return isProduction() || isStaging()
    }

    /**
     * Должны ли логи быть подробными
     */
    fun isVerboseLoggingEnabled(): Boolean {
        return BuildConfig.DEBUG
    }

    /**
     * Получение пользовательского агента для API запросов
     */
    fun getUserAgent(): String {
        return "PizzaNat/${BuildConfig.VERSION_NAME} (Android ${android.os.Build.VERSION.RELEASE}; ${BuildConfig.ENVIRONMENT})"
    }
}