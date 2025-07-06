#!/bin/bash

# 🧪 Скрипт для тестирования Address API
# Автор: PizzaNat Team
# Дата: 2025-01-23

echo "🚀 Тестирование Address API интеграции PizzaNat"
echo "=============================================="

# Настройки
BASE_URL="https://api.pizzanat.ru"
API_URL="$BASE_URL/api/v1"

# Цвета для вывода
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Функция для тестирования эндпоинта
test_endpoint() {
    local method=$1
    local endpoint=$2
    local description=$3
    local expected_status=$4

    echo -e "${BLUE}🔍 Тестирование: $description${NC}"
    echo "   Метод: $method"
    echo "   URL: $API_URL$endpoint"

    response=$(curl -s -w "%{http_code}" -X $method "$API_URL$endpoint" \
        -H "Accept: application/json" \
        -H "Content-Type: application/json")

    http_code="${response: -3}"
    body="${response%???}"

    if [[ $http_code -eq $expected_status ]]; then
        echo -e "${GREEN}✅ УСПЕХ ($http_code)${NC}"
        if [[ ! -z "$body" && "$body" != "null" ]]; then
            echo "   Ответ: ${body:0:100}..."
        fi
    else
        echo -e "${RED}❌ ОШИБКА (получен: $http_code, ожидался: $expected_status)${NC}"
        if [[ ! -z "$body" ]]; then
            echo "   Ответ: $body"
        fi
    fi
    echo "---"
}

# Функция для тестирования с параметрами
test_with_params() {
    local method=$1
    local endpoint=$2
    local params=$3
    local description=$4
    local expected_status=$5

    echo -e "${BLUE}🔍 Тестирование: $description${NC}"
    echo "   Метод: $method"
    echo "   URL: $API_URL$endpoint?$params"

    response=$(curl -s -w "%{http_code}" -X $method "$API_URL$endpoint?$params" \
        -H "Accept: application/json" \
        -H "Content-Type: application/json")

    http_code="${response: -3}"
    body="${response%???}"

    if [[ $http_code -eq $expected_status ]]; then
        echo -e "${GREEN}✅ УСПЕХ ($http_code)${NC}"
        if [[ ! -z "$body" && "$body" != "null" ]]; then
            # Пытаемся распарсить JSON ответ
            if command -v jq >/dev/null 2>&1; then
                echo "   Ответ (JSON):"
                echo "$body" | jq '.' 2>/dev/null || echo "   $body"
            else
                echo "   Ответ: ${body:0:200}..."
            fi
        fi
    else
        echo -e "${RED}❌ ОШИБКА (получен: $http_code, ожидался: $expected_status)${NC}"
        if [[ ! -z "$body" ]]; then
            echo "   Ответ: $body"
        fi
    fi
    echo "---"
}

echo "📡 Проверка подключения к серверу..."
if ! curl -s --connect-timeout 5 "$BASE_URL" >/dev/null; then
    echo -e "${RED}❌ Сервер недоступен: $BASE_URL${NC}"
    exit 1
else
    echo -e "${GREEN}✅ Сервер доступен${NC}"
fi

echo ""
echo "🔍 Тестирование Address API эндпоинтов..."
echo ""

# 1. Health Check (если есть)
test_endpoint "GET" "/health" "Health Check" 200

# 2. Тестирование подсказок адресов
echo -e "${YELLOW}📍 ТЕСТИРОВАНИЕ ПОДСКАЗОК АДРЕСОВ${NC}"

# Короткий запрос (должен возвращать ошибку или пустой результат)
test_with_params "GET" "/delivery/address-suggestions" "query=Мо&limit=10" \
    "Короткий запрос (2 символа)" 400

# Нормальный запрос - Москва
test_with_params "GET" "/delivery/address-suggestions" "query=Москва&limit=10" \
    "Подсказки для 'Москва'" 200

# Конкретная улица
test_with_params "GET" "/delivery/address-suggestions" "query=Москва%20Тверская&limit=5" \
    "Подсказки для 'Москва Тверская'" 200

# Запрос с русскими символами (URL encoded)
test_with_params "GET" "/delivery/address-suggestions" "query=%D0%A1%D0%9F%D0%B1&limit=10" \
    "Подсказки для 'СПб' (URL encoded)" 200

echo ""
echo -e "${YELLOW}🎯 ТЕСТИРОВАНИЕ ВАЛИДАЦИИ АДРЕСОВ${NC}"

# 3. Валидация адреса
test_with_params "GET" "/delivery/validate-address" \
    "address=г%20Москва,%20ул%20Тверская,%20д%201" \
    "Валидация адреса Москвы" 200

# Невалидный адрес
test_with_params "GET" "/delivery/validate-address" \
    "address=Несуществующий%20адрес%20123" \
    "Валидация некорректного адреса" 400

echo ""
echo -e "${YELLOW}🚚 ТЕСТИРОВАНИЕ РАСЧЕТА ДОСТАВКИ${NC}"

# 4. Расчет доставки
test_with_params "GET" "/delivery/estimate" \
    "address=г%20Москва,%20ул%20Тверская,%20д%201" \
    "Расчет доставки для Москвы" 200

# Адрес за пределами доставки
test_with_params "GET" "/delivery/estimate" \
    "address=г%20Владивосток,%20ул%20Светланская,%20д%201" \
    "Расчет доставки за пределами зоны" 200

echo ""
echo -e "${YELLOW}📱 ТЕСТИРОВАНИЕ ANDROID СПЕЦИФИЧНЫХ СЛУЧАЕВ${NC}"

# Тестирование с User-Agent Android приложения
echo -e "${BLUE}🔍 Тестирование с Android User-Agent${NC}"
response=$(curl -s -w "%{http_code}" \
    "$API_URL/delivery/address-suggestions?query=Москва&limit=5" \
    -H "Accept: application/json" \
    -H "Content-Type: application/json" \
    -H "User-Agent: PizzaNat-Android/1.0.0")

http_code="${response: -3}"
if [[ $http_code -eq 200 ]]; then
    echo -e "${GREEN}✅ Android User-Agent поддерживается${NC}"
else
    echo -e "${RED}❌ Проблемы с Android User-Agent ($http_code)${NC}"
fi

echo ""
echo "🎉 Тестирование завершено!"
echo ""

# Итоговый отчет
echo -e "${YELLOW}📊 ИТОГОВЫЙ ОТЧЕТ${NC}"
echo "================================"
echo "🔗 Базовый URL: $BASE_URL"
echo "📅 Дата тестирования: $(date)"
echo ""
echo "📝 РЕКОМЕНДАЦИИ:"
echo "1. Проверьте логи Backend сервера для диагностики ошибок"
echo "2. Убедитесь что Yandex Maps API ключ настроен"
echo "3. Проверьте CORS настройки для Android приложений"
echo "4. Мониторьте производительность при высокой нагрузке"
echo ""
echo "🔧 СЛЕДУЮЩИЕ ШАГИ:"
echo "1. Исправить найденные ошибки"
echo "2. Провести нагрузочное тестирование"
echo "3. Протестировать на Android устройстве"
echo "4. Настроить мониторинг в production"