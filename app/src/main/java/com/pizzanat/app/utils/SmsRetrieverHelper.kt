/**
 * @file: SmsRetrieverHelper.kt
 * @description: Helper для автоматического чтения SMS кодов через SMS Retriever API
 * @dependencies: Google Play Services Auth
 * @created: 2025-01-23
 */
package com.pizzanat.app.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Pattern
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest

class SmsRetrieverHelper(
    private val context: Context,
    private val onSmsCodeReceived: (String) -> Unit
) {
    
    companion object {
        private const val TAG = "SmsRetrieverHelper"
        private const val SMS_CONSENT_REQUEST = 2
    }
    
    private var smsReceiver: BroadcastReceiver? = null
    private var consentReceiver: BroadcastReceiver? = null
    
    /**
     * Запускает SMS Retriever для автоматического чтения SMS
     */
    fun startSmsRetriever() {
        Log.d(TAG, "📱 Запуск SMS Retriever API")
        
        // Проверяем доступность Google Play Services
        checkGooglePlayServices()
        
        // Запускаем оба метода для максимальной совместимости
        startSmsRetrieverClassic()
        startSmsUserConsent()
    }
    
    /**
     * Классический SMS Retriever API
     */
    private fun startSmsRetrieverClassic() {
        Log.d(TAG, "🔄 Запуск классического SMS Retriever...")
        
        val client = SmsRetriever.getClient(context)
        val task = client.startSmsRetriever()
        
        task.addOnSuccessListener {
            Log.d(TAG, "✅ SMS Retriever запущен успешно")
            Log.d(TAG, "📋 Ожидаем SMS с 4-значным кодом...")
            registerSmsReceiver()
        }
        
        task.addOnFailureListener { exception ->
            Log.e(TAG, "❌ Ошибка запуска SMS Retriever", exception)
            Log.e(TAG, "🔧 Возможные причины:")
            Log.e(TAG, "   - Google Play Services недоступны")
            Log.e(TAG, "   - Устройство не поддерживает SMS Retriever")
            Log.e(TAG, "   - Приложение не подписано правильным ключом")
        }
    }
    
    /**
     * SMS User Consent API (более надежный)
     */
    private fun startSmsUserConsent() {
        Log.d(TAG, "🔄 Запуск SMS User Consent API...")
        
        val client = SmsRetriever.getClient(context)
        val task = client.startSmsUserConsent(null) // null = любой номер отправителя
        
        task.addOnSuccessListener {
            Log.d(TAG, "✅ SMS User Consent запущен успешно")
            registerConsentReceiver()
        }
        
        task.addOnFailureListener { exception ->
            Log.e(TAG, "❌ Ошибка запуска SMS User Consent", exception)
        }
    }
    
    /**
     * Проверяет доступность Google Play Services
     */
    private fun checkGooglePlayServices() {
        try {
            val client = SmsRetriever.getClient(context)
            Log.d(TAG, "🔍 Google Play Services доступны: ${client != null}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Google Play Services недоступны", e)
        }
    }
    
    /**
     * Останавливает SMS Retriever
     */
    fun stopSmsRetriever() {
        Log.d(TAG, "🛑 Остановка SMS Retriever")
        unregisterSmsReceiver()
        unregisterConsentReceiver()
    }
    
    /**
     * Регистрирует BroadcastReceiver для получения SMS
     */
    private fun registerSmsReceiver() {
        if (smsReceiver != null) {
            Log.w(TAG, "⚠️ SMS Receiver уже зарегистрирован")
            return
        }
        
        Log.d(TAG, "📡 Регистрация SMS Receiver...")
        
        smsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "📨 Получен Intent: ${intent?.action}")
                
                if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
                    val extras = intent.extras
                    val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? Status
                    
                    Log.d(TAG, "📊 Статус SMS Retriever: ${status?.statusCode}")
                    
                    when (status?.statusCode) {
                        CommonStatusCodes.SUCCESS -> {
                            val message = extras[SmsRetriever.EXTRA_SMS_MESSAGE] as? String
                            Log.d(TAG, "📨 SMS получено: $message")
                            
                            message?.let { sms ->
                                val code = extractSmsCode(sms)
                                if (code != null) {
                                    Log.d(TAG, "🔢 Извлечен код: $code")
                                    onSmsCodeReceived(code)
                                } else {
                                    Log.w(TAG, "⚠️ Не удалось извлечь код из SMS: $sms")
                                    // Пробуем более агрессивный поиск
                                    val aggressiveCode = extractSmsCodeAggressive(sms)
                                    if (aggressiveCode != null) {
                                        Log.d(TAG, "🔍 Найден код агрессивным поиском: $aggressiveCode")
                                        onSmsCodeReceived(aggressiveCode)
                                    }
                                }
                            }
                        }
                        CommonStatusCodes.TIMEOUT -> {
                            Log.w(TAG, "⏰ SMS Retriever timeout (5 минут)")
                        }
                        else -> {
                            Log.w(TAG, "⚠️ SMS Retriever завершился с кодом: ${status?.statusCode}")
                        }
                    }
                } else {
                    Log.d(TAG, "📭 Получен другой Intent: ${intent?.action}")
                }
            }
        }
        
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(smsReceiver, intentFilter, Context.RECEIVER_EXPORTED)
            Log.d(TAG, "📱 SMS Receiver зарегистрирован (Android 13+)")
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(smsReceiver, intentFilter)
            Log.d(TAG, "📱 SMS Receiver зарегистрирован (Android <13)")
        }
    }
    
    /**
     * Отключает BroadcastReceiver
     */
    private fun unregisterSmsReceiver() {
        smsReceiver?.let { receiver ->
            try {
                context.unregisterReceiver(receiver)
                Log.d(TAG, "📴 SMS Receiver отключен")
            } catch (e: Exception) {
                Log.w(TAG, "Ошибка при отключении SMS Receiver", e)
            }
            smsReceiver = null
        }
    }
    
    /**
     * Извлекает код из SMS сообщения (стандартный поиск)
     */
    private fun extractSmsCode(smsMessage: String): String? {
        Log.d(TAG, "🔍 Поиск кода в сообщении: $smsMessage")
        
        // Ищем 4-значный код
        val patterns = listOf(
            "\\b\\d{4}\\b",           // 4 цифры подряд
            "код:?\\s*(\\d{4})",     // "код: 1234" или "код:1234"
            "code:?\\s*(\\d{4})",    // "code: 1234" или "code:1234"
            "(\\d{4})\\s*-\\s*код",  // "1234 - код"
            "\\D(\\d{4})\\D"         // 4 цифры между не-цифрами
        )
        
        for (patternStr in patterns) {
            val pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(smsMessage)
            
            if (matcher.find()) {
                val code = if (matcher.groupCount() > 0) {
                    matcher.group(1) // Группа из скобок
                } else {
                    matcher.group(0) // Вся найденная строка
                }
                
                // Проверяем, что найденный код - это 4 цифры
                if (code?.matches("\\d{4}".toRegex()) == true) {
                    Log.d(TAG, "✅ Найден код стандартным поиском: $code")
                    return code
                }
            }
        }
        
        return null
    }
    
    /**
     * Агрессивный поиск кода (все 4-значные числа)
     */
    private fun extractSmsCodeAggressive(smsMessage: String): String? {
        Log.d(TAG, "🔍 Агрессивный поиск всех 4-значных чисел...")
        
        val pattern = Pattern.compile("\\d{4}")
        val matcher = pattern.matcher(smsMessage)
        
        val foundCodes = mutableListOf<String>()
        while (matcher.find()) {
            foundCodes.add(matcher.group())
        }
        
        Log.d(TAG, "📋 Найдено 4-значных чисел: $foundCodes")
        
        // Возвращаем первое найденное 4-значное число
        return foundCodes.firstOrNull()?.also {
            Log.d(TAG, "✅ Выбран первый код: $it")
        }
    }
    
    /**
     * Регистрирует BroadcastReceiver для SMS User Consent
     */
    private fun registerConsentReceiver() {
        if (consentReceiver != null) {
            Log.w(TAG, "⚠️ Consent Receiver уже зарегистрирован")
            return
        }
        
        Log.d(TAG, "📡 Регистрация Consent Receiver...")
        
        consentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "📨 Получен Consent Intent: ${intent?.action}")
                
                if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
                    val extras = intent.extras
                    val smsMessage = extras?.getString(SmsRetriever.EXTRA_SMS_MESSAGE)
                    val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? Status
                    
                    Log.d(TAG, "📊 Consent статус: ${status?.statusCode}")
                    Log.d(TAG, "📨 Consent SMS: $smsMessage")
                    
                    when (status?.statusCode) {
                        CommonStatusCodes.SUCCESS -> {
                            smsMessage?.let { sms ->
                                val code = extractSmsCode(sms) ?: extractSmsCodeAggressive(sms)
                                code?.let {
                                    Log.d(TAG, "🎉 Код найден через User Consent: $it")
                                    onSmsCodeReceived(it)
                                }
                            }
                        }
                        CommonStatusCodes.TIMEOUT -> {
                            Log.w(TAG, "⏰ SMS User Consent timeout")
                        }
                        else -> {
                            Log.w(TAG, "⚠️ SMS User Consent завершился с кодом: ${status?.statusCode}")
                        }
                    }
                }
            }
        }
        
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(consentReceiver, intentFilter, Context.RECEIVER_EXPORTED)
            Log.d(TAG, "📱 Consent Receiver зарегистрирован (Android 13+)")
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(consentReceiver, intentFilter)
            Log.d(TAG, "📱 Consent Receiver зарегистрирован (Android <13)")
        }
    }
    
    /**
     * Отключает Consent BroadcastReceiver
     */
    private fun unregisterConsentReceiver() {
        consentReceiver?.let { receiver ->
            try {
                context.unregisterReceiver(receiver)
                Log.d(TAG, "📴 Consent Receiver отключен")
            } catch (e: Exception) {
                Log.w(TAG, "Ошибка при отключении Consent Receiver", e)
            }
            consentReceiver = null
        }
    }
} 