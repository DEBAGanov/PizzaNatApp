# 📱 Руководство по тестированию UI системы адресов

**Цель**: Проверить работу ZonalAddressTextField и интеграцию с CheckoutScreen

## 🚀 Подготовка к тестированию

### 1. Запуск приложения
```bash
# В терминале Android Studio или в папке проекта
./gradlew installDebug

# Или через Android Studio
# Run > Run 'app'
```

### 2. Подготовка тестовых данных
- Убедитесь, что в корзине есть товары (сумма 500₽ и 1500₽ для тестов)
- Проверьте подключение к интернету
- Откройте логи для мониторинга: `adb logcat | grep -i "address\|delivery"`

## 📋 Тестовые сценарии

### Сценарий 1: Базовая функциональность

#### 1.1 Навигация к экрану оформления
- [ ] Добавить товары в корзину (сумма ~500₽)
- [ ] Нажать кнопку "Оформить заказ" или перейти к Checkout
- [ ] Убедиться, что экран CheckoutScreen открылся
- [ ] Найти поле "Адрес доставки" с компонентом ZonalAddressTextField

#### 1.2 Проверка начального состояния
- [ ] Поле адреса пустое
- [ ] Плейсхолдер отображается корректно
- [ ] Иконка адреса видна
- [ ] Список подсказок скрыт
- [ ] Информация о доставке не отображается

### Сценарий 2: Ввод и подсказки адресов

#### 2.1 Тест минимальной длины запроса
- [ ] Ввести "V" → подсказки не должны появиться
- [ ] Ввести "Vo" → ожидать появления подсказок через 300ms
- [ ] Проверить индикатор загрузки

#### 2.2 Тест поиска адресов Волжска
```
Тестовые запросы:
- "Volz" → должны появиться подсказки Волжска
- "Volzhsk" → более точные подсказки
- "Ленина" → улицы с названием Ленина
```

**Ожидаемый результат:**
- [ ] Подсказки появляются в выпадающем списке
- [ ] Максимум 10 подсказок
- [ ] Анимация появления плавная
- [ ] Подсказки содержат читаемые адреса

#### 2.3 Выбор подсказки
- [ ] Ввести "Volzhsk"
- [ ] Дождаться появления подсказок
- [ ] Нажать на первую подсказку
- [ ] Убедиться, что поле заполнилось
- [ ] Проверить, что список подсказок скрылся

### Сценарий 3: Расчет стоимости доставки

#### 3.1 Автоматический расчет при вводе адреса
- [ ] Ввести адрес Волжска
- [ ] Убедиться, что появился блок с информацией о доставке
- [ ] Проверить отображение стоимости (ожидается 250₽)
- [ ] Проверить время доставки (30-50 минут)

#### 3.2 Тест бесплатной доставки
```
Подготовка: Изменить сумму корзины на 1500₽+
```
- [ ] Ввести тот же адрес Волжска
- [ ] Убедиться, что показано "Бесплатная доставка"
- [ ] Проверить зеленый цвет индикатора бесплатной доставки

### Сценарий 4: Обработка ошибок

#### 4.1 Тест сетевых ошибок
```
Подготовка: Отключить Wi-Fi или мобильные данные
```
- [ ] Ввести "Volzhsk"
- [ ] Убедиться, что отображается сообщение об ошибке сети
- [ ] Включить интернет
- [ ] Повторить ввод и убедиться, что подсказки работают

#### 4.2 Тест некорректных адресов
- [ ] Ввести "Несуществующий адрес 123"
- [ ] Убедиться, что подсказки не появляются или список пустой
- [ ] Проверить отсутствие информации о доставке

### Сценарий 5: Производительность и UX

#### 5.1 Тест debounce (задержки)
- [ ] Быстро вводить и удалять символы
- [ ] Убедиться, что запросы не отправляются при каждом нажатии
- [ ] Проверить, что запрос отправляется только после паузы 300ms

#### 5.2 Тест очистки поля
- [ ] Ввести адрес
- [ ] Найти кнопку очистки (X)
- [ ] Нажать кнопку очистки
- [ ] Убедиться, что поле очищено
- [ ] Проверить, что подсказки и информация о доставке скрыты

## 🔍 Проверка логов

### Ключевые логи для мониторинга:
```bash
# Фильтр логов для системы адресов
adb logcat | grep -E "(AddressRepository|ZonalAddress|Delivery|Checkout)"
```

### Ожидаемые логи:
```
I/AddressRepository: Запрос подсказок для: 'Volzhsk'
I/AddressRepository: Получено 2 подсказки адресов
I/AddressRepository: Расчет доставки для: 'Волжск...'
I/DeliveryEstimate: Стоимость: 250₽, Бесплатная: false
```

## ✅ Чеклист успешного тестирования

### Обязательные функции:
- [ ] Подсказки адресов загружаются и отображаются
- [ ] Выбор подсказки заполняет поле
- [ ] Расчет стоимости доставки работает
- [ ] Бесплатная доставка рассчитывается корректно
- [ ] Debounce предотвращает избыточные запросы
- [ ] Обработка ошибок функциональна

### UI/UX качество:
- [ ] Анимации плавные
- [ ] Интерфейс отзывчивый
- [ ] Индикаторы загрузки работают
- [ ] Цветовая схема соответствует дизайну
- [ ] Текст читаемый и понятный

### Интеграция с Checkout:
- [ ] Адрес сохраняется в форме заказа
- [ ] Валидация адреса работает
- [ ] Переход к следующему полю корректный
- [ ] Данные адреса передаются в заказ

## 🐛 Возможные проблемы и решения

### Проблема: Подсказки не появляются
**Возможные причины:**
- Нет интернет-соединения
- API endpoint недоступен
- Ошибка в коде debounce

**Проверка:**
```bash
# Тест API вручную
curl -s "https://api.pizzanat.ru/api/v1/delivery/address-suggestions?query=Volzhsk&limit=5"
```

### Проблема: Расчет доставки не работает
**Возможные причины:**
- Неправильный формат адреса
- Ошибка в API estimate endpoint
- Проблемы с парсингом ответа

**Проверка:**
```bash
# Тест estimate API
curl -s "https://api.pizzanat.ru/api/v1/delivery/estimate?address=Volzhsk&orderAmount=500"
```

### Проблема: UI компоненты не отображаются
**Возможные причины:**
- Ошибки компиляции
- Неправильная интеграция в CheckoutScreen
- Проблемы с DI

**Решение:**
1. Проверить логи компиляции
2. Убедиться, что ZonalAddressTextField импортирован в CheckoutScreen
3. Проверить инъекцию зависимостей

## 📊 Отчет о тестировании

### Шаблон отчета:
```markdown
## Отчет UI тестирования - [ДАТА]

### Тестовое окружение:
- Устройство: [Android устройство/эмулятор]
- Android версия: [версия]
- Версия приложения: [версия]

### Результаты:
✅/❌ Подсказки адресов
✅/❌ Расчет доставки  
✅/❌ Бесплатная доставка
✅/❌ Обработка ошибок
✅/❌ UX/Анимации

### Обнаруженные проблемы:
1. [Описание проблемы]

### Рекомендации:
1. [Рекомендация]
```

---

**Следующий шаг**: Запустить приложение и выполнить все сценарии тестирования 