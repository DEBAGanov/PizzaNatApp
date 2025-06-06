# ТЗ для Backend команды: Интеграция SMS и Telegram аутентификации

## Обзор

Требуется добавить два новых метода аутентификации в существующий backend PizzaNat:
1. **Аутентификация через SMS код** (используя Exolve SMS API)
2. **Аутентификация через Telegram** (используя Telegram Bot API)

## 1. SMS Аутентификация

### 1.1 Общая схема

```
Пользователь → Ввод телефона → Отправка SMS → Ввод кода → Авторизация
```

### 1.2 Требуемые API эндпоинты

#### POST `/api/auth/phone/send-code`

**Описание**: Отправка SMS кода на указанный номер телефона

**Request Body:**
```json
{
  "phoneNumber": "+79991234567"
}
```

**Response 200:**
```json
{
  "success": true,
  "message": "SMS код отправлен",
  "expiresAt": "2024-12-20T15:30:00Z",
  "codeLength": 4
}
```

**Response 400:**
```json
{
  "success": false,
  "error": "INVALID_PHONE_NUMBER",
  "message": "Неверный формат номера телефона"
}
```

**Response 429:**
```json
{
  "success": false,
  "error": "RATE_LIMIT_EXCEEDED",
  "message": "Слишком много запросов. Повторите через 60 секунд",
  "retryAfter": 60
}
```

#### POST `/api/auth/phone/verify-code`

**Описание**: Проверка SMS кода и авторизация

**Request Body:**
```json
{
  "phoneNumber": "+79991234567",
  "code": "1234"
}
```

**Response 200:**
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 123,
    "phoneNumber": "+79991234567",
    "firstName": "Иван",
    "lastName": "Иванов",
    "email": null,
    "isPhoneVerified": true,
    "createdAt": "2024-12-20T12:00:00Z"
  }
}
```

**Response 400:**
```json
{
  "success": false,
  "error": "INVALID_CODE",
  "message": "Неверный код или код истек"
}
```

### 1.3 Интеграция с Exolve SMS API

**Документация**: https://docs.exolve.ru/docs/ru/api-reference/sms-api/

**Параметры подключения:**
- **API URL**: `https://api.exolve.ru/messaging/v1/SendSMS`
- **Authorization**: `Bearer YOUR_API_KEY`
- **Метод**: POST

**Пример запроса к Exolve:**
```json
{
  "number": "PizzaNat",
  "destination": "+79991234567",
  "text": "Ваш код для входа в PizzaNat: 1234. Не сообщайте его никому!"
}
```

### 1.4 Логика работы SMS аутентификации

1. **Получение запроса** на отправку SMS кода
2. **Валидация номера** телефона (формат +7XXXXXXXXXX)
3. **Rate limiting**: максимум 3 SMS в час на номер
4. **Генерация 4-значного кода** (цифры 0-9)
5. **Сохранение кода** в базе данных с TTL 10 минут
6. **Отправка SMS** через Exolve API
7. **Логирование** результата отправки

#### Проверка кода:
1. **Валидация** номера и кода
2. **Проверка** существования и актуальности кода
3. **Поиск пользователя** по номеру телефона
4. **Создание нового пользователя** если не существует
5. **Генерация JWT токена**
6. **Удаление использованного кода**

### 1.5 База данных

#### Таблица `sms_codes`

```sql
CREATE TABLE sms_codes (
    id BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL,
    code VARCHAR(4) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    attempts INTEGER DEFAULT 0,

    INDEX idx_phone_number (phone_number),
    INDEX idx_expires_at (expires_at)
);
```

#### Обновление таблицы `users`

```sql
ALTER TABLE users ADD COLUMN phone_number VARCHAR(20) UNIQUE;
ALTER TABLE users ADD COLUMN is_phone_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE users ALTER COLUMN email DROP NOT NULL; -- телефон как альтернатива email
```

## 2. Telegram Аутентификация

### 2.1 Общая схема

```
Пользователь → Генерация auth_token → Открытие Telegram → Подтверждение → Авторизация
```

### 2.2 Требуемые API эндпоинты

#### POST `/api/auth/telegram/init`

**Описание**: Инициализация Telegram аутентификации

**Request Body:**
```json
{
  "deviceId": "android_device_123" // опционально для отслеживания
}
```

**Response 200:**
```json
{
  "success": true,
  "authToken": "tg_auth_abc123def456",
  "telegramBotUrl": "https://t.me/pizzanat_bot?start=tg_auth_abc123def456",
  "expiresAt": "2024-12-20T15:30:00Z"
}
```

#### GET `/api/auth/telegram/status/{authToken}`

**Описание**: Проверка статуса Telegram аутентификации

**Response 200 (не подтверждено):**
```json
{
  "success": true,
  "status": "PENDING",
  "message": "Ожидание подтверждения в Telegram"
}
```

**Response 200 (подтверждено):**
```json
{
  "success": true,
  "status": "CONFIRMED",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 123,
    "telegramId": 987654321,
    "username": "@john_doe",
    "firstName": "Иван",
    "lastName": "Иванов",
    "email": null,
    "isTelegramVerified": true,
    "createdAt": "2024-12-20T12:00:00Z"
  }
}
```

**Response 400:**
```json
{
  "success": false,
  "error": "INVALID_TOKEN",
  "message": "Неверный или истекший токен"
}
```

### 2.3 Telegram Bot интеграция

**Документация**: https://core.telegram.org/api#tdlib-build-your-own-telegram

**Требования к боту:**
- **Имя бота**: `@pizzanat_bot` (или аналогичное)
- **Команды**: `/start`, `/auth`, `/help`
- **Webhook URL**: `https://your-backend.com/api/telegram/webhook`

**Обработка команд:**

```
/start tg_auth_abc123def456
```

Ответ бота:
```
🍕 Добро пожаловать в PizzaNat!

Вы хотите авторизоваться в мобильном приложении?

[✅ Подтвердить] [❌ Отмена]
```

### 2.4 Логика работы Telegram аутентификации

1. **Генерация уникального auth_token** (UUID + префикс)
2. **Сохранение токена** в базе данных с TTL 10 минут
3. **Создание ссылки** на Telegram бота с параметром
4. **Ожидание** подтверждения от пользователя в Telegram
5. **Получение данных** пользователя из Telegram API
6. **Поиск/создание** пользователя в базе данных
7. **Генерация JWT токена**

### 2.5 База данных

#### Таблица `telegram_auth_tokens`

```sql
CREATE TABLE telegram_auth_tokens (
    id BIGSERIAL PRIMARY KEY,
    auth_token VARCHAR(50) UNIQUE NOT NULL,
    telegram_id BIGINT,
    telegram_username VARCHAR(100),
    telegram_first_name VARCHAR(100),
    telegram_last_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, CONFIRMED, EXPIRED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP,

    INDEX idx_auth_token (auth_token),
    INDEX idx_expires_at (expires_at)
);
```

#### Обновление таблицы `users`

```sql
ALTER TABLE users ADD COLUMN telegram_id BIGINT UNIQUE;
ALTER TABLE users ADD COLUMN telegram_username VARCHAR(100);
ALTER TABLE users ADD COLUMN is_telegram_verified BOOLEAN DEFAULT FALSE;
```

## 3. Общие требования

### 3.1 Безопасность

1. **Rate Limiting**:
   - SMS: максимум 3 сообщения в час на номер
   - Telegram: максимум 5 токенов в час на IP

2. **Валидация данных**:
   - Проверка формата номера телефона
   - Санитизация входных данных
   - CSRF защита для Telegram webhook

3. **Логирование**:
   - Все попытки аутентификации
   - Ошибки интеграции с внешними API
   - Подозрительная активность

### 3.2 Конфигурация

**Environment переменные:**

```bash
# Exolve SMS
EXOLVE_API_URL=https://api.exolve.ru/messaging/v1
EXOLVE_API_KEY=your_api_key_here
EXOLVE_SENDER_NAME=PizzaNat

# Telegram Bot
TELEGRAM_BOT_TOKEN=your_bot_token_here
TELEGRAM_BOT_USERNAME=pizzanat_bot
TELEGRAM_WEBHOOK_URL=https://your-backend.com/api/telegram/webhook

# SMS Settings
SMS_CODE_LENGTH=4
SMS_CODE_TTL_MINUTES=10
SMS_RATE_LIMIT_PER_HOUR=3

# Telegram Settings
TELEGRAM_AUTH_TOKEN_TTL_MINUTES=10
TELEGRAM_RATE_LIMIT_PER_HOUR=5
```

### 3.3 Обработка ошибок

**Возможные ошибки и их обработка:**

1. **Exolve API недоступен**: Логирование, возврат ошибки пользователю
2. **Telegram API недоступен**: Fallback на polling вместо webhook
3. **Дублирование SMS**: Проверка последнего отправленного кода
4. **Истечение токенов**: Автоматическая очистка expired записей

### 3.4 Мониторинг

**Метрики для отслеживания:**
- Количество отправленных SMS в день
- Процент успешных аутентификаций
- Время ответа Exolve и Telegram API
- Количество ошибок по типам

## 4. Тестирование

### 4.1 Unit тесты

- Валидация номеров телефона
- Генерация и проверка SMS кодов
- Логика создания пользователей
- JWT токены

### 4.2 Integration тесты

- Полный цикл SMS аутентификации
- Полный цикл Telegram аутентификации
- Обработка ошибок внешних API

### 4.3 Тестовые данные

**Для разработки:**
- Mock Exolve API для локальной разработки
- Тестовый Telegram бот для staging
- Тестовые номера телефонов

## 5. Развертывание

### 5.1 Staging

1. Создание тестового Telegram бота
2. Настройка тестового аккаунта Exolve
3. Настройка environment переменных
4. Проверка webhook endpoints

### 5.2 Production

1. Регистрация production Telegram бота
2. Получение production API ключей Exolve
3. Настройка SSL сертификатов для webhook
4. Мониторинг и логирование

## 6. Временные рамки

**Оценка времени разработки:**

- **SMS аутентификация**: 3-4 дня
- **Telegram аутентификация**: 4-5 дней
- **Тестирование и отладка**: 2-3 дня
- **Документация и деплой**: 1 день

**Общее время**: 10-13 дней

## 7. Критерии приемки

1. ✅ Пользователь может авторизоваться через SMS код
2. ✅ Пользователь может авторизоваться через Telegram
3. ✅ Корректная обработка ошибок и edge cases
4. ✅ Соблюдение rate limiting
5. ✅ Безопасное хранение данных пользователей
6. ✅ Полное логирование для отладки
7. ✅ Unit и integration тесты покрывают основную логику
8. ✅ API документация актуализирована

---

**Примечания:**
- Все эндпоинты должны возвращать консистентный JSON формат
- Требуется backward compatibility с существующей аутентификацией
- Mobile приложение уже готово к интеграции с этими API