/**
 * @file: NotificationRepositoryImpl.kt
 * @description: Реальная реализация NotificationRepository с Backend API интеграцией
 * @dependencies: NotificationApiService, NotificationDao, DataStore
 * @created: 2024-12-20
 */
package com.pizzanat.app.data.repositories

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pizzanat.app.data.local.dao.NotificationDao
import com.pizzanat.app.data.mappers.*
import com.pizzanat.app.data.remote.api.NotificationApiService
import com.pizzanat.app.data.remote.util.ApiResult
import com.pizzanat.app.domain.entities.Notification
import com.pizzanat.app.domain.entities.NotificationSettings
import com.pizzanat.app.domain.repositories.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationApiService: NotificationApiService,
    private val notificationDao: NotificationDao,
    private val dataStore: DataStore<Preferences>
) : NotificationRepository {
    
    companion object {
        private const val TAG = "NotificationRepository"
        
        // DataStore ключи для настроек уведомлений
        private val PUSH_NOTIFICATIONS_ENABLED = booleanPreferencesKey("push_notifications_enabled")
        private val ORDER_STATUS_ENABLED = booleanPreferencesKey("order_status_enabled")
        private val DELIVERY_UPDATES_ENABLED = booleanPreferencesKey("delivery_updates_enabled")
        private val PROMOTIONS_ENABLED = booleanPreferencesKey("promotions_enabled")
        private val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        private val QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        private val QUIET_HOURS_START = stringPreferencesKey("quiet_hours_start")
        private val QUIET_HOURS_END = stringPreferencesKey("quiet_hours_end")
        private val FCM_TOKEN = stringPreferencesKey("fcm_token")
    }
    
    override fun getAllNotifications(): Flow<List<Notification>> {
        // Сначала возвращаем локальные данные, затем синхронизируемся с сервером
        return notificationDao.getAllNotifications().map { entities ->
            // Асинхронно синхронизируем с сервером
            syncNotificationsWithServer()
            entities.toDomain()
        }
    }
    
    override fun getUnreadNotifications(): Flow<List<Notification>> {
        return notificationDao.getUnreadNotifications().map { entities ->
            entities.toDomain()
        }
    }
    
    override fun getUnreadCount(): Flow<Int> {
        return notificationDao.getUnreadCount()
    }
    
    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            // Сначала обновляем на сервере
            val response = notificationApiService.markAsRead(notificationId)
            
            if (response.isSuccessful) {
                // Затем обновляем локально
                notificationDao.markAsRead(notificationId)
                Log.d(TAG, "Уведомление отмечено как прочитанное: $notificationId")
                Result.success(Unit)
            } else {
                // Если сервер недоступен, обновляем только локально
                notificationDao.markAsRead(notificationId)
                Log.w(TAG, "Сервер недоступен, обновлено только локально: $notificationId")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            // В случае ошибки обновляем локально
            try {
                notificationDao.markAsRead(notificationId)
                Log.w(TAG, "Ошибка API, обновлено локально: ${e.message}")
                Result.success(Unit)
            } catch (localError: Exception) {
                Log.e(TAG, "Ошибка отметки уведомления как прочитанное: ${localError.message}")
                Result.failure(localError)
            }
        }
    }
    
    override suspend fun markAllAsRead(): Result<Unit> {
        return try {
            // Сначала обновляем на сервере
            val response = notificationApiService.markAllAsRead()
            
            if (response.isSuccessful) {
                // Затем обновляем локально
                notificationDao.markAllAsRead()
                Log.d(TAG, "Все уведомления отмечены как прочитанные")
                Result.success(Unit)
            } else {
                // Если сервер недоступен, обновляем только локально
                notificationDao.markAllAsRead()
                Log.w(TAG, "Сервер недоступен, обновлено только локально")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            // В случае ошибки обновляем локально
            try {
                notificationDao.markAllAsRead()
                Log.w(TAG, "Ошибка API, обновлено локально: ${e.message}")
                Result.success(Unit)
            } catch (localError: Exception) {
                Log.e(TAG, "Ошибка отметки всех уведомлений как прочитанные: ${localError.message}")
                Result.failure(localError)
            }
        }
    }
    
    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            // Сначала удаляем на сервере
            val response = notificationApiService.deleteNotification(notificationId)
            
            if (response.isSuccessful) {
                // Затем удаляем локально
                notificationDao.deleteNotification(notificationId)
                Log.d(TAG, "Уведомление удалено: $notificationId")
                Result.success(Unit)
            } else {
                // Если сервер недоступен, удаляем только локально
                notificationDao.deleteNotification(notificationId)
                Log.w(TAG, "Сервер недоступен, удалено только локально: $notificationId")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            // В случае ошибки удаляем локально
            try {
                notificationDao.deleteNotification(notificationId)
                Log.w(TAG, "Ошибка API, удалено локально: ${e.message}")
                Result.success(Unit)
            } catch (localError: Exception) {
                Log.e(TAG, "Ошибка удаления уведомления: ${localError.message}")
                Result.failure(localError)
            }
        }
    }
    
    override suspend fun clearAllNotifications(): Result<Unit> {
        return try {
            // Сначала очищаем на сервере
            val response = notificationApiService.clearAllNotifications()
            
            if (response.isSuccessful) {
                // Затем очищаем локально
                notificationDao.deleteAllNotifications()
                Log.d(TAG, "Все уведомления очищены")
                Result.success(Unit)
            } else {
                // Если сервер недоступен, очищаем только локально
                notificationDao.deleteAllNotifications()
                Log.w(TAG, "Сервер недоступен, очищено только локально")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            // В случае ошибки очищаем локально
            try {
                notificationDao.deleteAllNotifications()
                Log.w(TAG, "Ошибка API, очищено локально: ${e.message}")
                Result.success(Unit)
            } catch (localError: Exception) {
                Log.e(TAG, "Ошибка очистки уведомлений: ${localError.message}")
                Result.failure(localError)
            }
        }
    }
    
    override suspend fun saveNotification(notification: Notification): Result<Unit> {
        return try {
            // Сохраняем локально (уведомления обычно приходят через push)
            notificationDao.insertNotification(notification.toEntity())
            Log.d(TAG, "Уведомление сохранено: ${notification.title}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка сохранения уведомления: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun getNotificationSettings(): Result<NotificationSettings> {
        return try {
            // Сначала пытаемся получить с сервера
            val response = notificationApiService.getNotificationSettings()
            
            if (response.isSuccessful && response.body() != null) {
                val settings = response.body()!!.toDomain()
                // Сохраняем локально для кэширования
                saveSettingsLocally(settings)
                Result.success(settings)
            } else {
                // Если сервер недоступен, возвращаем локальные настройки
                getSettingsLocally()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Ошибка получения настроек с сервера, используем локальные: ${e.message}")
            getSettingsLocally()
        }
    }
    
    override suspend fun saveNotificationSettings(settings: NotificationSettings): Result<Unit> {
        return try {
            // Сначала сохраняем на сервере
            val response = notificationApiService.saveNotificationSettings(settings.toDto())
            
            if (response.isSuccessful) {
                // Затем сохраняем локально
                saveSettingsLocally(settings)
                Log.d(TAG, "Настройки уведомлений сохранены")
                Result.success(Unit)
            } else {
                // Если сервер недоступен, сохраняем только локально
                saveSettingsLocally(settings)
                Log.w(TAG, "Сервер недоступен, настройки сохранены только локально")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            // В случае ошибки сохраняем локально
            try {
                saveSettingsLocally(settings)
                Log.w(TAG, "Ошибка API, настройки сохранены локально: ${e.message}")
                Result.success(Unit)
            } catch (localError: Exception) {
                Log.e(TAG, "Ошибка сохранения настроек уведомлений: ${localError.message}")
                Result.failure(localError)
            }
        }
    }
    
    override suspend fun registerFcmToken(token: String): Result<Unit> {
        return try {
            // Регистрируем токен на сервере
            val tokenDto = createFcmTokenDto(token)
            val response = notificationApiService.registerFcmToken(tokenDto)
            
            if (response.isSuccessful) {
                // Сохраняем токен локально
                dataStore.edit { preferences ->
                    preferences[FCM_TOKEN] = token
                }
                Log.d(TAG, "FCM токен зарегистрирован: ${token.take(20)}...")
                Result.success(Unit)
            } else {
                Log.w(TAG, "Ошибка регистрации FCM токена на сервере: ${response.code()}")
                Result.failure(Exception("Ошибка регистрации токена: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка регистрации FCM токена: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun subscribeToOrderUpdates(orderId: Long): Result<Unit> {
        return try {
            val response = notificationApiService.subscribeToOrderUpdates(orderId)
            
            if (response.isSuccessful) {
                Log.d(TAG, "Подписка на уведомления заказа $orderId активирована")
                Result.success(Unit)
            } else {
                Log.w(TAG, "Ошибка подписки на уведомления заказа: ${response.code()}")
                Result.failure(Exception("Ошибка подписки: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка подписки на уведомления заказа: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun unsubscribeFromOrderUpdates(orderId: Long): Result<Unit> {
        return try {
            val response = notificationApiService.unsubscribeFromOrderUpdates(orderId)
            
            if (response.isSuccessful) {
                Log.d(TAG, "Отписка от уведомлений заказа $orderId выполнена")
                Result.success(Unit)
            } else {
                Log.w(TAG, "Ошибка отписки от уведомлений заказа: ${response.code()}")
                Result.failure(Exception("Ошибка отписки: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка отписки от уведомлений заказа: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Синхронизация уведомлений с сервером
     */
    private suspend fun syncNotificationsWithServer() {
        try {
            val response = notificationApiService.getNotifications(page = 0, size = 50)
            
            if (response.isSuccessful && response.body() != null) {
                val notifications = response.body()!!.toDomain()
                
                // Сохраняем новые уведомления локально
                notifications.forEach { notification ->
                    try {
                        notificationDao.insertNotification(notification.toEntity())
                    } catch (e: Exception) {
                        // Игнорируем дубликаты
                    }
                }
                
                Log.d(TAG, "Синхронизировано ${notifications.size} уведомлений")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Ошибка синхронизации уведомлений: ${e.message}")
        }
    }
    
    /**
     * Получение настроек из локального хранилища
     */
    private suspend fun getSettingsLocally(): Result<NotificationSettings> {
        return try {
            val preferences = dataStore.data.first()
            val settings = NotificationSettings(
                pushNotificationsEnabled = preferences[PUSH_NOTIFICATIONS_ENABLED] ?: true,
                orderStatusEnabled = preferences[ORDER_STATUS_ENABLED] ?: true,
                deliveryUpdatesEnabled = preferences[DELIVERY_UPDATES_ENABLED] ?: true,
                promotionsEnabled = preferences[PROMOTIONS_ENABLED] ?: true,
                soundEnabled = preferences[SOUND_ENABLED] ?: true,
                vibrationEnabled = preferences[VIBRATION_ENABLED] ?: true,
                quietHoursEnabled = preferences[QUIET_HOURS_ENABLED] ?: false,
                quietHoursStart = preferences[QUIET_HOURS_START] ?: "22:00",
                quietHoursEnd = preferences[QUIET_HOURS_END] ?: "08:00"
            )
            Result.success(settings)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка получения локальных настроек: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Сохранение настроек в локальное хранилище
     */
    private suspend fun saveSettingsLocally(settings: NotificationSettings) {
        dataStore.edit { preferences ->
            preferences[PUSH_NOTIFICATIONS_ENABLED] = settings.pushNotificationsEnabled
            preferences[ORDER_STATUS_ENABLED] = settings.orderStatusEnabled
            preferences[DELIVERY_UPDATES_ENABLED] = settings.deliveryUpdatesEnabled
            preferences[PROMOTIONS_ENABLED] = settings.promotionsEnabled
            preferences[SOUND_ENABLED] = settings.soundEnabled
            preferences[VIBRATION_ENABLED] = settings.vibrationEnabled
            preferences[QUIET_HOURS_ENABLED] = settings.quietHoursEnabled
            preferences[QUIET_HOURS_START] = settings.quietHoursStart
            preferences[QUIET_HOURS_END] = settings.quietHoursEnd
        }
    }
} 