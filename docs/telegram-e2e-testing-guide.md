# 🧪 Инструкция E2E тестирования Telegram авторизации

## Обзор полной цепочки тестирования

**Цель:** Проверить полную интеграцию Android → Backend API → Telegram Bot → Webhook → Backend → Android

**Архитектура тестирования:**
```
[Android App] → [Backend API] → [Telegram Bot] → [User confirms] → [Webhook] → [Backend API] → [Android App]
```

## 📋 Предварительные требования

### 1. Backend API готовность
- ✅ **URL**: `https://debaganov-pizzanat-0177.twc1.net/api/v1/`
- ✅ **Эндпоинты**:
  - `POST /auth/telegram/init` - инициализация авторизации
  - `GET /auth/telegram/status/{authToken}` - проверка статуса
- ✅ **Telegram Bot**: должен быть настроен и активен
- ✅ **Webhook**: должен быть настроен для получения подтверждений от Telegram

### 2. Android приложение готовность
- ✅ **Сборка**: Debug APK собран успешно
- ✅ **E2E тестер**: TelegramE2ETester доступен в debug панели
- ✅ **Auto тесты**: AutoE2ETestPanel готов к использованию
- ✅ **API логирование**: ApiLogger интегрирован

### 3. Устройство/эмулятор требования
- ✅ **Telegram app**: установлен на устройстве тестирования
- ✅ **Интернет**: стабильное подключение
- ✅ **Логи**: доступ к Android Logcat для отладки

---

## 🚀 Пошаговое тестирование

### Этап 1: Проверка Backend доступности

**1.1 Автоматическая проверка конфигурации:**
```
1. Запустите Android приложение
2. Откройте Debug панель
3. Перейдите на вкладку "Auto Test"
4. Нажмите "Запустить E2E тесты"
5. Проверьте результаты первых двух тестов:
   - Configuration Check ✅
   - API Connectivity ✅
```

**1.2 Ожидаемые результаты:**
- Environment: DEBUG
- Base URL: https://debaganov-pizzanat-0177.twc1.net/api/v1/
- API вызовы выполняются без ошибок сети

### Этап 2: Интерактивное тестирование инициализации

**2.1 Открытие интерактивного тестера:**
```
1. Перейдите на вкладку "Telegram E2E"
2. Нажмите "Запустить Telegram авторизацию"
3. Наблюдайте за статусом загрузки
```

**2.2 Ожидаемые результаты:**
```
✅ Статус: "📱 Готов к авторизации в Telegram"
✅ Auth Token: получен (20+ символов)
✅ Telegram URL: получен (содержит t.me/yourbot?start=...)
✅ API Логи: показывают POST /auth/telegram/init со статусом 200
```

**2.3 Если получена ошибка:**
```
❌ Проверьте API логи на наличие:
   - Network errors (нет интернета)
   - HTTP 4xx/5xx (проблемы с backend)
   - Timeout errors (медленная сеть)

❌ Возможные проблемы:
   - Backend API недоступен
   - Telegram Bot не настроен
   - Неправильная конфигурация эндпоинтов
```

### Этап 3: Тестирование Telegram Bot интеграции

**3.1 Открытие Telegram:**
```
1. В разделе "Информация об авторизации" нажмите стрелку рядом с Telegram URL
2. Выберите открыть в Telegram app (или браузере если app не установлен)
3. В Telegram должен открыться чат с ботом
```

**3.2 Ожидаемое поведение в Telegram:**
```
✅ Telegram Bot отвечает на команду /start
✅ Бот показывает сообщение о подтверждении авторизации
✅ Есть кнопка/команда для подтверждения авторизации
```

**3.3 Подтверждение авторизации:**
```
1. Следуйте инструкциям Telegram Bot
2. Нажмите кнопку подтверждения авторизации
3. Бот должен ответить что авторизация подтверждена
```

### Этап 4: Проверка polling механизма

**4.1 Автоматический polling (уже запущен):**
```
✅ Android app автоматически проверяет статус каждые 5 секунд
✅ Максимум 12 попыток (60 секунд)
✅ Статус должен измениться: "🔄 Ожидание подтверждения..." → "✅ Авторизация успешно завершена!"
```

**4.2 Ручная проверка статуса:**
```
1. Нажмите "Проверить статус авторизации"
2. Наблюдайте API логи: GET /auth/telegram/status/{token}
3. Статус должен стать CONFIRMED после подтверждения в Telegram
```

### Этап 5: Проверка сохранения данных

**5.1 После успешной авторизации:**
```
✅ Auth token должен быть сохранен в DataStore
✅ User данные должны быть сохранены
✅ Приложение должно перейти в авторизованное состояние
```

**5.2 Проверка через автоматические тесты:**
```
1. После успешной авторизации запустите Auto тесты снова
2. Все 5 тестов должны пройти успешно:
   - Configuration Check ✅
   - API Connectivity ✅
   - Telegram Init ✅
   - Status Check ✅
   - Timeout Simulation ✅
```

---

## 🔍 Детальная диагностика

### API логирование в реальном времени

**Интерактивный мониторинг:**
```
1. Откройте вкладку "Telegram E2E"
2. Раздел "API Логи" показывает все HTTP запросы
3. Для каждого запроса видно:
   - Метод и URL
   - HTTP статус код
   - Время выполнения (мс)
   - Тело ошибки (если есть)
```

**Типичные успешные логи:**
```
✅ POST /auth/telegram/init - Status: 200 - 245ms
✅ GET /auth/telegram/status/{token} - Status: 200 - 123ms
```

**Типичные ошибки:**
```
❌ POST /auth/telegram/init - Status: 500 - 2341ms
   Error: Telegram Bot configuration error

❌ GET /auth/telegram/status/{token} - Network Error
   Error: Network Error: timeout
```

### Android Logcat фильтры

**Основные теги для мониторинга:**
```bash
# API логирование
adb logcat -s "ApiLogger"

# E2E тестирование
adb logcat -s "E2ETestScenario"

# Telegram авторизация
adb logcat -s "TelegramAuthViewModel"

# Конфигурация сборки
adb logcat -s "BuildConfig"

# Все PizzaNat логи
adb logcat -s "PizzaNat*"
```

### Типичные проблемы и решения

**❌ Проблема: "Network Error" при инициализации**
```
Причина: Backend API недоступен
Решение:
1. Проверьте интернет подключение
2. Проверьте доступность https://debaganov-pizzanat-0177.twc1.net/
3. Проверьте конфигурацию BASE_API_URL в build.gradle.kts
```

**❌ Проблема: "HTTP 404" на /auth/telegram/init**
```
Причина: Эндпоинт не реализован на backend
Решение: Убедитесь что backend имеет Telegram авторизацию эндпоинты
```

**❌ Проблема: Telegram Bot не отвечает**
```
Причина: Бот не настроен или неактивен
Решение:
1. Проверьте что бот создан через @BotFather
2. Проверьте что webhook настроен на backend
3. Проверьте логи backend для Telegram webhook events
```

**❌ Проблема: "Auth token expired"**
```
Причина: Превышен timeout авторизации
Решение:
1. Нажмите "Сбросить тест"
2. Запустите авторизацию заново
3. Подтвердите в Telegram быстрее (в течение 60 секунд)
```

**❌ Проблема: Polling не получает CONFIRMED статус**
```
Причина: Webhook не работает или не настроен
Решение:
1. Проверьте логи backend webhook endpoint
2. Убедитесь что Telegram webhook URL настроен правильно
3. Проверьте что backend обновляет статус авторизации при получении webhook
```

---

## 📊 Ожидаемые результаты успешного тестирования

### Полная успешная цепочка:

**1. Инициализация (Android → Backend):**
```
POST /auth/telegram/init
Response: 200 OK
{
  "authToken": "auth_12345...",
  "telegramBotUrl": "https://t.me/yourbot?start=auth_12345...",
  "expiresAt": "2024-12-20T15:30:00Z"
}
```

**2. Telegram авторизация (User → Telegram Bot):**
```
User клик по ссылке → Telegram открывается → User подтверждает
Bot отправляет webhook: POST {backend_url}/telegram/webhook
```

**3. Статус проверка (Android → Backend):**
```
GET /auth/telegram/status/auth_12345...
Response: 200 OK
{
  "status": "CONFIRMED",
  "token": "jwt_token...",
  "user": {
    "id": 123,
    "telegramId": 456789,
    "username": "testuser"
  }
}
```

**4. Данные сохранены (Android local):**
```
✅ JWT token в DataStore
✅ User данные в DataStore
✅ Приложение в авторизованном состоянии
```

### Все автоматические тесты пройдены:
```
✅ Configuration Check - 15ms
✅ API Connectivity - 234ms (1 API call)
✅ Telegram Init - 456ms (1 API call)
✅ Status Check - 189ms (1 API call)
✅ Timeout Simulation - 50ms
```

---

## 🔧 Дополнительные инструменты отладки

При необходимости можно добавить:
- Детальное логирование WebHook событий
- Mock Telegram Bot для автономного тестирования
- Настройка разных timeout интервалов
- Интеграция с Telegram Bot API для проверки состояния бота

**Готовность к тестированию: 100% ✅**

Все компоненты реализованы и готовы к полному E2E тестированию с реальным Telegram Bot.