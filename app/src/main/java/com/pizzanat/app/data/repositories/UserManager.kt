/**
 * @file: UserManager.kt
 * @description: Менеджер для управления данными текущего пользователя
 * @dependencies: DataStore, Gson, Domain entities
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.pizzanat.app.domain.entities.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) {
    
    companion object {
        private val USER_KEY = stringPreferencesKey("current_user")
    }
    
    /**
     * Сохранение данных пользователя
     */
    suspend fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        dataStore.edit { preferences ->
            preferences[USER_KEY] = userJson
        }
    }
    
    /**
     * Получение данных пользователя
     */
    suspend fun getUser(): User? {
        val userJson = dataStore.data.first()[USER_KEY]
        return if (!userJson.isNullOrBlank()) {
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Flow для отслеживания данных пользователя
     */
    fun getUserFlow(): Flow<User?> {
        return dataStore.data.map { preferences ->
            val userJson = preferences[USER_KEY]
            if (!userJson.isNullOrBlank()) {
                try {
                    gson.fromJson(userJson, User::class.java)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }
    
    /**
     * Проверка наличия данных пользователя
     */
    suspend fun hasUser(): Boolean {
        return getUser() != null
    }
    
    /**
     * Очистка данных пользователя
     */
    suspend fun clearUser() {
        dataStore.edit { preferences ->
            preferences.remove(USER_KEY)
        }
    }
} 