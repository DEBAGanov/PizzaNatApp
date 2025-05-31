/**
 * @file: TokenManager.kt
 * @description: Менеджер для безопасного хранения и управления JWT токенами
 * @dependencies: DataStore, Coroutines
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val TOKEN_TIMESTAMP_KEY = stringPreferencesKey("token_timestamp")
        private const val TOKEN_LIFETIME_HOURS = 24L
    }
    
    /**
     * Сохранение JWT токена с временной меткой
     */
    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[TOKEN_TIMESTAMP_KEY] = System.currentTimeMillis().toString()
        }
    }
    
    /**
     * Получение сохраненного токена
     */
    suspend fun getToken(): String? {
        return dataStore.data.first()[TOKEN_KEY]
    }
    
    /**
     * Flow для отслеживания токена
     */
    fun getTokenFlow(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }
    
    /**
     * Проверка валидности токена по времени
     */
    suspend fun isTokenValid(): Boolean {
        val token = getToken()
        if (token.isNullOrBlank()) return false
        
        val timestampString = dataStore.data.first()[TOKEN_TIMESTAMP_KEY] ?: return false
        val timestamp = timestampString.toLongOrNull() ?: return false
        
        val currentTime = System.currentTimeMillis()
        val tokenAge = currentTime - timestamp
        val tokenLifetime = TOKEN_LIFETIME_HOURS * 60 * 60 * 1000 // 24 часа в миллисекундах
        
        return tokenAge < tokenLifetime
    }
    
    /**
     * Проверка наличия токена
     */
    suspend fun hasToken(): Boolean {
        return !getToken().isNullOrBlank()
    }
    
    /**
     * Очистка токена (выход из системы)
     */
    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(TOKEN_TIMESTAMP_KEY)
        }
    }
    
    /**
     * Flow для отслеживания состояния аутентификации
     */
    fun isAuthenticatedFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            val token = preferences[TOKEN_KEY]
            val timestampString = preferences[TOKEN_TIMESTAMP_KEY]
            
            if (token.isNullOrBlank() || timestampString.isNullOrBlank()) return@map false
            
            val timestamp = timestampString.toLongOrNull() ?: return@map false
            val currentTime = System.currentTimeMillis()
            val tokenAge = currentTime - timestamp
            val tokenLifetime = TOKEN_LIFETIME_HOURS * 60 * 60 * 1000
            
            tokenAge < tokenLifetime
        }
    }
} 