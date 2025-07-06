# Настройка production конфигурации для платежей ЮКасса

## Обзор

Android приложение PizzaNat настроено для работы с платежами ЮКасса через backend API. Все ключи и конфигурация хранятся на сервере в переменных окружения, что обеспечивает безопасность и гибкость.

## Архитектура платежей

### Backend (Сервер)
- **Создание платежей**: Сервер создает платежи в ЮКасса через API
- **Обработка webhook**: Сервер получает уведомления о статусе платежей
- **Хранение ключей**: Все ключи ЮКасса хранятся в переменных окружения
- **Безопасность**: Секретные ключи недоступны клиенту

### Android приложение
- **Создание заказа**: Отправляет данные заказа на сервер
- **Инициация платежа**: Запрашивает создание платежа через API
- **WebView**: Открывает страницу оплаты ЮКасса в WebView
- **Обработка результата**: Получает результат платежа по URL callback

## Настройка переменных окружения на сервере

### Основные переменные

```bash
# ЮКасса API ключи
YOOKASSA_SHOP_ID=your_shop_id_here
YOOKASSA_SECRET_KEY=your_secret_key_here

# URLs для callback
YOOKASSA_RETURN_URL=https://your-domain.com/payment/return
YOOKASSA_NOTIFICATION_URL=https://your-domain.com/api/v1/payments/webhook

# Настройки окружения
PAYMENT_ENVIRONMENT=production  # или test для тестирования
```

### Тестовые ключи (для разработки)

```bash
# Тестовые ключи ЮКасса
YOOKASSA_SHOP_ID=test_shop_id
YOOKASSA_SECRET_KEY=test_secret_key
PAYMENT_ENVIRONMENT=test
```

## Endpoints для платежей

### Создание платежа
```
POST /api/v1/payments/create
```

**Запрос:**
```json
{
  "amount": 1500.00,
  "currency": "RUB",
  "orderId": 123,
  "paymentMethod": "bank_card",
  "description": "Оплата заказа #123",
  "customerEmail": "customer@example.com",
  "customerPhone": "+79876543210",
  "returnUrl": "https://your-app.com/payment/return"
}
```

**Ответ:**
```json
{
  "id": "payment_id_from_yookassa",
  "orderId": 123,
  "amount": 1500.00,
  "currency": "RUB",
  "paymentMethod": "bank_card",
  "status": "pending",
  "confirmationUrl": "https://yoomoney.ru/checkout/payments/v2/contract?orderId=...",
  "createdAt": "2025-01-04T10:30:00Z",
  "description": "Оплата заказа #123"
}
```

### Проверка статуса платежа
```
GET /api/v1/payments/{paymentId}
```

### Отмена платежа
```
POST /api/v1/payments/{paymentId}/cancel
```

## Настройка webhook на сервере

### Endpoint для webhook
```
POST /api/v1/payments/webhook
```

### Обработка уведомлений
```java
@PostMapping("/webhook")
public ResponseEntity<String> handlePaymentWebhook(@RequestBody String payload, HttpServletRequest request) {
    // Проверка подписи
    String signature = request.getHeader("X-Yookassa-Signature");
    if (!validateSignature(payload, signature)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
    }
    
    // Обработка уведомления
    PaymentNotification notification = parseNotification(payload);
    updatePaymentStatus(notification);
    
    return ResponseEntity.ok("OK");
}
```

## Настройка Android приложения

### 1. Проверка базового URL
В `app/build.gradle.kts` убедитесь, что BASE_API_URL указывает на ваш production сервер:

```kotlin
buildTypes {
    release {
        buildConfigField("String", "BASE_API_URL", "\"https://api.pizzanat.ru/api/v1/\"")
    }
}
```

### 2. Использование PaymentWebViewScreen
```kotlin
// Создание платежа
paymentWebViewModel.createPayment(
    orderId = order.id,
    amount = order.grandTotal,
    paymentMethod = PaymentMethod.ONLINE_CARD,
    description = "Оплата заказа #${order.id}",
    customerEmail = user.email,
    customerPhone = user.phone,
    returnUrl = "https://your-app.com/payment/return"
)

// Открытие WebView с URL оплаты
if (paymentUrl != null) {
    PaymentWebViewScreen(
        navController = navController,
        paymentUrl = paymentUrl,
        onPaymentResult = { result ->
            when (result) {
                is PaymentResult.Success -> {
                    // Платеж успешен
                    navigateToOrderSuccess()
                }
                is PaymentResult.Failed -> {
                    // Ошибка платежа
                    showErrorDialog("Ошибка оплаты")
                }
                is PaymentResult.Cancelled -> {
                    // Платеж отменен
                    navigateBack()
                }
            }
        }
    )
}
```

## Схема URL для обработки результатов

### Success URLs
- `https://your-domain.com/payment/return?status=success`
- `https://your-domain.com/payment/return?payment_success=true`

### Error URLs
- `https://your-domain.com/payment/return?status=fail`
- `https://your-domain.com/payment/return?payment_fail=true`

### Cancel URLs
- `https://your-domain.com/payment/return?status=cancel`
- `https://your-domain.com/payment/return?payment_cancel=true`

## Безопасность

### 1. Валидация webhook
```java
private boolean validateSignature(String payload, String signature) {
    String secretKey = System.getenv("YOOKASSA_SECRET_KEY");
    String expectedSignature = calculateHmacSha256(payload, secretKey);
    return signature.equals(expectedSignature);
}
```

### 2. HTTPS обязательно
- Все API endpoints должны работать через HTTPS
- Webhook URL должен быть доступен только через HTTPS
- Return URL должен использовать HTTPS

### 3. Проверка источника
- Проверяйте IP адреса webhook запросов
- Используйте подписи для валидации

## Тестирование

### 1. Тестовые карты ЮКасса
```
Успешная оплата: 5555555555554444
Отклонение: 5555555555554477
3D-Secure: 5555555555554487
```

### 2. Тестирование webhook
```bash
curl -X POST https://your-domain.com/api/v1/payments/webhook \
  -H "Content-Type: application/json" \
  -H "X-Yookassa-Signature: your_signature" \
  -d '{"event": "payment.succeeded", "object": {...}}'
```

## Мониторинг

### 1. Логирование
- Логируйте все платежные операции
- Сохраняйте webhook уведомления
- Отслеживайте ошибки API

### 2. Алерты
- Настройте уведомления о неуспешных платежах
- Мониторьте доступность webhook endpoint
- Отслеживайте время отклика API

## Checklist для production

- [ ] Настроены переменные окружения с production ключами
- [ ] Webhook endpoint доступен и защищен
- [ ] HTTPS настроен для всех endpoints
- [ ] Валидация подписей webhook работает
- [ ] Тестирование с реальными картами проведено
- [ ] Логирование и мониторинг настроены
- [ ] Return URLs настроены правильно
- [ ] Android приложение использует production API URL

## Поддержка

При возникновении проблем проверьте:
1. Логи сервера на наличие ошибок API
2. Статус платежей в личном кабинете ЮКасса
3. Webhook уведомления и их обработку
4. Корректность переменных окружения 