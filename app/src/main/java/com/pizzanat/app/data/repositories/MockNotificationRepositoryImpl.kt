/**
 * @file: MockNotificationRepositoryImpl.kt
 * @description: Мок-реализация NotificationRepository для тестирования без Firebase
 * @dependencies: NotificationRepository, DataStore
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.repositories

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pizzanat.app.data.local.dao.NotificationDao
import com.pizzanat.app.data.mappers.toDomain
import com.pizzanat.app.data.mappers.toEntity
import com.pizzanat.app.domain.entities.Notification
import com.pizzanat.app.domain.entities.NotificationSettings
import com.pizzanat.app.domain.entities.NotificationType
import com.pizzanat.app.domain.repositories.NotificationRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockNotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    private val dataStore: DataStore<Preferences>
) : NotificationRepository {
    
    companion object {
        private const val TAG = "MockNotificationRepo"
        
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
        private val SAMPLE_NOTIFICATIONS_CREATED = booleanPreferencesKey("sample_notifications_created")
    }
    
    init {
        // Создаём тестовые уведомления при первом запуске
        GlobalScope.launch {
            val preferences = dataStore.data.first()
            if (preferences[SAMPLE_NOTIFICATIONS_CREATED] != true) {
                createSampleNotifications()
                dataStore.edit { prefs ->
                    prefs[SAMPLE_NOTIFICATIONS_CREATED] = true
                }
                Log.d(TAG, "Тестовые уведомления созданы")
            }
        }
    }
    
    override fun getAllNotifications(): Flow<List<Notification>> {
        return notificationDao.getAllNotifications().map { entities ->
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
            notificationDao.markAsRead(notificationId)
            Log.d(TAG, "Уведомление отмечено как прочитанное: $notificationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка отметки уведомления как прочитанное: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun markAllAsRead(): Result<Unit> {
        return try {
            notificationDao.markAllAsRead()
            Log.d(TAG, "Все уведомления отмечены как прочитанные")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка отметки всех уведомлений как прочитанные: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            notificationDao.deleteNotification(notificationId)
            Log.d(TAG, "Уведомление удалено: $notificationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка удаления уведомления: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun clearAllNotifications(): Result<Unit> {
        return try {
            notificationDao.deleteAllNotifications()
            Log.d(TAG, "Все уведомления очищены")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка очистки уведомлений: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun saveNotification(notification: Notification): Result<Unit> {
        return try {
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
            Log.e(TAG, "Ошибка получения настроек уведомлений: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun saveNotificationSettings(settings: NotificationSettings): Result<Unit> {
        return try {
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
            Log.d(TAG, "Настройки уведомлений сохранены")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка сохранения настроек уведомлений: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun registerFcmToken(token: String): Result<Unit> {
        return try {
            // Симуляция задержки сети
            delay(500)
            
            // Сохраняем токен локально
            dataStore.edit { preferences ->
                preferences[FCM_TOKEN] = token
            }
            
            Log.d(TAG, "FCM токен зарегистрирован (мок): ${token.take(20)}...")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка регистрации FCM токена: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun subscribeToOrderUpdates(orderId: Long): Result<Unit> {
        return try {
            // Симуляция подписки на уведомления о заказе
            delay(300)
            
            // Создаем тестовое уведомление о подписке
            val notification = Notification(
                id = "sub_${orderId}_${System.currentTimeMillis()}",
                title = "Отслеживание заказа",
                message = "Вы подписались на уведомления о заказе #$orderId",
                type = NotificationType.SYSTEM,
                orderId = orderId,
                createdAt = LocalDateTime.now()
            )
            saveNotification(notification)
            
            Log.d(TAG, "Подписка на уведомления заказа #$orderId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка подписки на уведомления заказа: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun unsubscribeFromOrderUpdates(orderId: Long): Result<Unit> {
        return try {
            // Симуляция отписки от уведомлений о заказе
            delay(300)
            Log.d(TAG, "Отписка от уведомлений заказа #$orderId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка отписки от уведомлений заказа: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Создание тестовых уведомлений для демонстрации
     */
    suspend fun createSampleNotifications() {
        val now = LocalDateTime.now()
        val sampleNotifications = listOf(
            // Уведомления о заказах
            Notification(
                id = "sample_1",
                title = "Заказ подтвержден",
                message = "Ваш заказ #123 принят в обработку. Ожидаемое время приготовления: 30 минут",
                type = NotificationType.ORDER_STATUS_CHANGED,
                orderId = 123,
                createdAt = now.minusMinutes(45),
                isRead = false
            ),
            Notification(
                id = "sample_2", 
                title = "Готовится",
                message = "Ваш заказ #123 готовится. Осталось примерно 15 минут",
                type = NotificationType.ORDER_STATUS_CHANGED,
                orderId = 123,
                createdAt = now.minusMinutes(20),
                isRead = false
            ),
            Notification(
                id = "sample_3",
                title = "Заказ готов",
                message = "Ваш заказ #122 готов к выдаче. Курьер уже в пути!",
                type = NotificationType.ORDER_STATUS_CHANGED,
                orderId = 122,
                createdAt = now.minusMinutes(10),
                isRead = true
            ),
            
            // Уведомления о доставке
            Notification(
                id = "sample_4",
                title = "Курьер в пути",
                message = "Курьер Александр направляется к вам. Примерное время прибытия: 15 минут",
                type = NotificationType.DELIVERY_UPDATE,
                orderId = 123,
                createdAt = now.minusMinutes(8),
                isRead = false
            ),
            Notification(
                id = "sample_5",
                title = "Курьер прибыл",
                message = "Курьер прибыл по адресу доставки. Пожалуйста, встретьте его",
                type = NotificationType.DELIVERY_UPDATE,
                orderId = 122,
                createdAt = now.minusHours(2),
                isRead = true
            ),
            
            // Промо уведомления
            Notification(
                id = "sample_6",
                title = "🍕 Специальное предложение!",
                message = "Скидка 25% на все пиццы до конца недели! Используйте промокод PIZZA25",
                type = NotificationType.PROMOTION,
                createdAt = now.minusHours(6),
                isRead = false,
                actionUrl = "promotion/pizza25"
            ),
            Notification(
                id = "sample_7",
                title = "🎉 Новая пицца в меню!",
                message = "Попробуйте новую пиццу 'Тропическая' с ананасами и креветками",
                type = NotificationType.PROMOTION,
                createdAt = now.minusDays(1),
                isRead = true
            ),
            
            // Системные уведомления
            Notification(
                id = "sample_8",
                title = "Обновление приложения",
                message = "Доступна новая версия приложения с улучшениями и исправлениями",
                type = NotificationType.SYSTEM,
                createdAt = now.minusDays(2),
                isRead = false
            ),
            Notification(
                id = "sample_9",
                title = "Техническое обслуживание",
                message = "Плановое техническое обслуживание завершено. Спасибо за ваше терпение",
                type = NotificationType.SYSTEM,
                createdAt = now.minusDays(3),
                isRead = true
            ),
            
            // Напоминания
            Notification(
                id = "sample_10",
                title = "Не забудьте оценить заказ",
                message = "Как вам понравился заказ #121? Ваше мнение важно для нас",
                type = NotificationType.REMINDER,
                orderId = 121,
                createdAt = now.minusHours(12),
                isRead = false
            ),
            Notification(
                id = "sample_11",
                title = "Время для новой пиццы!",
                message = "Давно не заказывали у нас. Возможно, пора попробовать что-то новенькое?",
                type = NotificationType.REMINDER,
                createdAt = now.minusDays(5),
                isRead = true
            )
        )
        
        sampleNotifications.forEach { notification ->
            saveNotification(notification)
        }
        
        Log.d(TAG, "Создано ${sampleNotifications.size} тестовых уведомлений")
    }
} 