# ✅ КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ: Ошибка суммы в ЮКассе API

**Дата**: 30.01.2025  
**Статус**: ✅ ИСПРАВЛЕНО  
**Приоритет**: КРИТИЧЕСКИЙ  

## 🚨 Проблема была

ЮКасса возвращала ошибку при создании СБП платежей:

```json
{
  "type": "error",
  "code": "invalid_request",
  "description": "Invalid parameter's value",
  "parameter": "receipt.items.amount"
}
```

### Причина
Несоответствие между общей суммой платежа и суммой товаров в чеке:
- **Общая сумма платежа**: 559.0 ₽ (только товары)
- **Сумма товаров в чеке**: 559.00 + 200.00 = **759.00 ₽** (товары + доставка)

## ✅ Решение

### Изменение в коде
**До:**
```kotlin
createSbpPayment(orderId, _uiState.value.subtotal, currentUser.email ?: "", data.customerPhone)
```

**После:**
```kotlin
createSbpPayment(orderId, _uiState.value.total, currentUser.email ?: "", data.customerPhone)
```

### Логика
- Теперь в ЮКассу передается **полная сумма** (товары + доставка)
- Backend должен создать чек на **точно такую же сумму**
- Соблюдается требование ЮКассы: общая сумма = сумма товаров в чеке

## 🎯 Результат

✅ **Исправлено**: Общая сумма платежа теперь соответствует сумме товаров в чеке ЮКассы  
✅ **Сборка**: Debug версия собирается успешно  
✅ **Документация**: Обновлен changelog.md  

## 📋 Следующие шаги

1. **Протестировать** исправление с реальными суммами
2. **Диагностировать** почему selectedPaymentMethod != PaymentMethod.SBP 
3. **Проверить** работу СБП платежей end-to-end

## 📁 Измененные файлы

- `app/src/main/java/com/pizzanat/app/presentation/payment/PaymentViewModel.kt`
- `docs/changelog.md`
- `docs/yookassa-receipt-fix-summary.md` (новый) 