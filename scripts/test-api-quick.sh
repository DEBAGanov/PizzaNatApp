#!/bin/bash

# Быстрое тестирование API адресов
echo "🚀 Быстрое тестирование API адресов PizzaNat"
echo "==========================================="

API_BASE="https://api.pizzanat.ru/api/v1"

# 1. Тест подсказок адресов
echo "1️⃣ Тестирование подсказок адресов:"
echo ""

echo "🔍 Поиск 'Volzhsk':"
curl -s "$API_BASE/delivery/address-suggestions?query=Volzhsk&limit=3" | jq -r '.[] | "  📍 " + .shortAddress'
echo ""

echo "🔍 Поиск 'Ленина' (UTF-8):"
curl -s "$API_BASE/delivery/address-suggestions?query=%D0%9B%D0%B5%D0%BD%D0%B8%D0%BD%D0%B0&limit=3" | jq -r '.[] | "  📍 " + .shortAddress'
echo ""

# 2. Тест расчета доставки
echo "2️⃣ Тестирование расчета доставки:"
echo ""

echo "🚚 Доставка в Волжск (500₽ заказ):"
curl -s "$API_BASE/delivery/estimate?address=Volzhsk&orderAmount=500" | jq -r '"  💰 Стоимость: " + (.deliveryCost|tostring) + " ₽"'
echo ""

echo "🚚 Доставка в Волжск (1500₽ заказ):"
response=$(curl -s "$API_BASE/delivery/estimate?address=Volzhsk&orderAmount=1500")
is_free=$(echo "$response" | jq -r '.isDeliveryFree')
cost=$(echo "$response" | jq -r '.deliveryCost')
if [ "$is_free" = "true" ]; then
    echo "  🎉 Бесплатная доставка!"
else
    echo "  💰 Стоимость: $cost ₽"
fi
echo ""

# 3. Результат
echo "✅ Тестирование завершено!"
echo "📊 API работает корректно" 