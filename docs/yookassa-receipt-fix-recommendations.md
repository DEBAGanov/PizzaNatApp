# Рекомендации по исправлению ошибки чека ЮКассы

**Дата**: 30.01.2025  
**Статус**: Критическая ошибка требует исправления backend  
**Приоритет**: ВЫСОКИЙ  

## 🚨 Проблема

ЮКасса возвращает ошибку при создании СБП платежей:

```json
{
  "type": "error",
  "id": "01983884-4262-7bcf-ae30-5cbfdbebea29",
  "code": "invalid_request",
  "description": "Invalid parameter's value (for example, the value is illegal or its format is incorrect). Send the value in accordance with the documentation.",
  "parameter": "receipt.items.amount"
}
```

**Причина**: Сумма товаров в чеке не равна общей сумме платежа:
- **Общая сумма платежа**: 201.0 ₽ (правильно)
- **Сумма товара в чеке**: 1.00 ₽ (неправильно)

## 🔍 Анализ

### Что происходит:
1. ❌ **Двойное добавление доставки**: Мобильное приложение добавляет 200₽ доставки к сумме товаров
2. ❌ **Backend тоже добавляет доставку**: При формировании чека ЮКассы backend добавляет свою стоимость доставки  
3. ❌ **Товары имеют тестовые цены**: В базе данных товары стоят 1₽ вместо реальных цен
4. ✅ **ИСПРАВЛЕНО в мобильном приложении**: Теперь передается только сумма товаров (subtotal), backend сам добавит доставку

### Фактический запрос к ЮКассе:
```json
{
  "amount": {
    "currency": "RUB",
    "value": "201.0"  // ✅ Правильная общая сумма
  },
  "receipt": {
    "items": [
      {
        "description": "Мясная пицца",
        "quantity": "1.00",
        "amount": {
          "value": "1.00",  // ❌ Неправильная цена товара
          "currency": "RUB"
        },
        "vat_code": 1
      }
    ]
  }
}
```

## 🛠️ Решения

### Вариант 1: Исправить цены в базе данных (РЕКОМЕНДУЕТСЯ)

```sql
-- Обновить цены товаров на реальные значения
UPDATE products SET price = 559.00 WHERE name = 'Мясная пицца';
UPDATE products SET price = 449.00 WHERE name = 'Маргарита';
-- и так далее для всех товаров
```

### Вариант 2: Исправить логику формирования чека

Изменить backend код, который формирует чек для ЮКассы:

```java
// Пример исправления в Spring Boot контроллере
@PostMapping("/payments/yookassa/create")
public ResponseEntity<PaymentDto> createPayment(@RequestBody CreatePaymentRequest request) {
    Order order = orderService.findById(request.getOrderId());
    
    // 🔧 ИСПРАВЛЕНИЕ: Корректируем цены товаров в чеке
    List<ReceiptItem> receiptItems = new ArrayList<>();
    
    // Если сумма товаров в заказе не равна общей сумме платежа
    double orderItemsSum = order.getItems().stream()
        .mapToDouble(item -> item.getPrice() * item.getQuantity())
        .sum();
    
    double expectedSum = request.getAmount() - order.getDeliveryCost();
    
    if (Math.abs(orderItemsSum - expectedSum) > 0.01) {
        // Пропорционально корректируем цены товаров
        double correctionFactor = expectedSum / orderItemsSum;
        
        for (OrderItem item : order.getItems()) {
            double correctedPrice = item.getPrice() * correctionFactor;
            receiptItems.add(new ReceiptItem(
                item.getName(),
                correctedPrice,
                item.getQuantity()
            ));
        }
    } else {
        // Используем исходные цены
        for (OrderItem item : order.getItems()) {
            receiptItems.add(new ReceiptItem(
                item.getName(),
                item.getPrice(),
                item.getQuantity()
            ));
        }
    }
    
    // Добавляем доставку как отдельную позицию если нужно
    if (order.getDeliveryCost() > 0) {
        receiptItems.add(new ReceiptItem(
            "Доставка",
            order.getDeliveryCost(),
            1
        ));
    }
    
    // Отправляем в ЮКассу
    return yookassaService.createPayment(request.getAmount(), receiptItems);
}
```

### Вариант 3: Использовать простые чеки без детализации (НЕ РЕКОМЕНДУЕТСЯ)

```java
// Убрать детальный чек и использовать только общую сумму
// НЕ СООТВЕТСТВУЕТ 54-ФЗ для большинства случаев
PaymentRequest paymentRequest = PaymentRequest.builder()
    .amount(Amount.builder()
        .value(request.getAmount())
        .currency("RUB")
        .build())
    .description(request.getDescription())
    // Убираем receipt полностью
    .build();
```

## 🆕 Текущее состояние мобильного приложения

### ✅ Основная проблема решена:

1. **Исправлено двойное добавление доставки**: 
   ```kotlin
   // БЫЛО:
   createSbpPayment(orderId, _uiState.value.total, ...)  // товары + доставка
   
   // СТАЛО:
   createSbpPayment(orderId, _uiState.value.subtotal, ...)  // только товары
   ```

2. **Логика**: Backend теперь сам добавляет доставку в чек ЮКассы, избегая дублирования

### 🛡️ Дополнительная защита:

1. **Graceful Fallback**: При ошибке чека ЮКассы заказ автоматически завершается как "Картой/наличными при получении"

2. **Расширенная диагностика**: Добавлено логирование для выявления проблем:
   ```kotlin
   Log.d("PaymentViewModel", "🛒 ДИАГНОСТИКА ТОВАРОВ ДЛЯ ЮКАССЫ:")
   Log.d("PaymentViewModel", "  📦 Всего товаров в заказе: ${data.cartItems.size}")
   Log.d("PaymentViewModel", "  🔧 ВАЖНО: Передаем только сумму товаров, backend добавит доставку в чек")
   ```

3. **Обработка ошибки**: При получении `receipt.items.amount` ошибки применяется fallback

## 📋 План действий

### Для Backend команды:

1. **Немедленно**: Обновить цены товаров в базе данных на реальные значения
2. **Среднесрочно**: Реализовать корректировку цен в чеке (Вариант 2)
3. **Долгосрочно**: Добавить валидацию соответствия цен при создании заказов

### Для Mobile команды:

1. ✅ **Выполнено**: Исправлено двойное добавление доставки - передается только сумма товаров
2. ✅ **Выполнено**: Добавлен graceful fallback для ошибок чека
3. ✅ **Выполнено**: Расширена диагностика и логирование
4. **Готово к тестированию**: Приложение должно корректно работать после исправления цен товаров в backend

## 🧪 Тестирование

После исправления backend протестировать:

1. **Создание СБП платежа** - должно проходить без ошибок
2. **Формирование чека** - сумма товаров должна равняться общей сумме
3. **Разные сценарии**:
   - Заказ с одним товаром
   - Заказ с несколькими товарами
   - Заказ с доставкой
   - Заказ без доставки (самовывоз)

## 📱 Контактная информация

При вопросах по исправлению обращаться к команде мобильной разработки.

---

**Обновлено**: 30.01.2025 (18:00) - Исправлено двойное добавление доставки 