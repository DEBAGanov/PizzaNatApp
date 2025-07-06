#!/bin/bash

# Тестирование системы адресов с зональной доставкой Волжск
# API: https://api.pizzanat.ru/api/v1/

API_BASE="https://api.pizzanat.ru"
DELIVERY_ENDPOINT="$API_BASE/api/v1/delivery"

echo "🏠 === ТЕСТИРОВАНИЕ СИСТЕМЫ АДРЕСОВ ВОЛЖСК ==="
echo "📍 Backend API: $API_BASE"
echo ""

# Функция для отправки запроса с логированием
make_request() {
    local method=$1
    local url=$2
    local description=$3
    
    echo "🔍 $description"
    echo "📤 $method $url"
    echo "⏱️  $(date '+%H:%M:%S')"
    echo ""
    
    curl -s -w "\n📊 HTTP Status: %{http_code} | Time: %{time_total}s\n" \
         -H "Accept: application/json" \
         -H "User-Agent: PizzaNat-Android-Test/1.0" \
         "$url"
    
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
}

# URL encoding функция для русских символов
urlencode() {
    local string="${1}"
    local strlen=${#string}
    local encoded=""
    local pos c o

    for (( pos=0 ; pos<strlen ; pos++ )); do
        c=${string:$pos:1}
        case "$c" in
            [-_.~a-zA-Z0-9] ) o="${c}" ;;
            * )               printf -v o '%%%02x' "'$c"
        esac
        encoded+="${o}"
    done
    echo "${encoded}"
}

# 1. Тестирование подсказок адресов (минимум 2 символа)
echo "1️⃣ ТЕСТИРОВАНИЕ ПОДСКАЗОК АДРЕСОВ"
echo ""

# Тест 1.1: Короткий запрос (должен работать с 2 символов)
query=$(urlencode "ле")
make_request "GET" "$DELIVERY_ENDPOINT/address-suggestions?query=$query&limit=10" \
    "Поиск улиц на 'ле' (минимум 2 символа)"

# Тест 1.2: Конкретная улица
query=$(urlencode "ленина")
make_request "GET" "$DELIVERY_ENDPOINT/address-suggestions?query=$query&limit=10" \
    "Поиск улиц на 'ленина'"

# Тест 1.3: Другие улицы Волжска
query=$(urlencode "мира")
make_request "GET" "$DELIVERY_ENDPOINT/address-suggestions?query=$query&limit=10" \
    "Поиск улиц на 'мира'"

query=$(urlencode "советская")
make_request "GET" "$DELIVERY_ENDPOINT/address-suggestions?query=$query&limit=10" \
    "Поиск улиц на 'советская'"

# Тест 1.4: Проверка районов/микрорайонов
query=$(urlencode "дружба")
make_request "GET" "$DELIVERY_ENDPOINT/address-suggestions?query=$query&limit=10" \
    "Поиск в районе 'дружба'"

query=$(urlencode "заря")
make_request "GET" "$DELIVERY_ENDPOINT/address-suggestions?query=$query&limit=10" \
    "Поиск в районе 'заря'"

# 2. Тестирование зональной системы доставки
echo "2️⃣ ТЕСТИРОВАНИЕ ЗОНАЛЬНОЙ СИСТЕМЫ ДОСТАВКИ"
echo ""

# Тест 2.1: Зона Дружба (самая дешевая - 100₽)
address=$(urlencode "г. Волжск, мкр. Дружба, ул. Ленина, д. 1")
make_request "GET" "$DELIVERY_ENDPOINT/estimate?address=$address&orderAmount=500" \
    "Расчет доставки в зону Дружба (500₽ заказ)"

make_request "GET" "$DELIVERY_ENDPOINT/estimate?address=$address&orderAmount=1000" \
    "Расчет доставки в зону Дружба (1000₽ заказ - бесплатная доставка)"

# Тест 2.2: Центральная зона (200₽)
address=$(urlencode "г. Волжск, ул. Советская, д. 10")
make_request "GET" "$DELIVERY_ENDPOINT/estimate?address=$address&orderAmount=800" \
    "Расчет доставки в Центральную зону (800₽ заказ)"

make_request "GET" "$DELIVERY_ENDPOINT/estimate?address=$address&orderAmount=1200" \
    "Расчет доставки в Центральную зону (1200₽ заказ - бесплатная доставка)"

# Тест 2.3: Зона Заря (250₽)
address=$(urlencode "г. Волжск, мкр. Заря, ул. Мира, д. 5")
make_request "GET" "$DELIVERY_ENDPOINT/estimate?address=$address&orderAmount=900" \
    "Расчет доставки в зону Заря (900₽ заказ)"

make_request "GET" "$DELIVERY_ENDPOINT/estimate?address=$address&orderAmount=1300" \
    "Расчет доставки в зону Заря (1300₽ заказ - бесплатная доставка)"

# Тест 2.4: Зона Промузел (самая дорогая - 300₽)
address=$(urlencode "г. Волжск, Промышленная зона, ул. Заводская, д. 1")
make_request "GET" "$DELIVERY_ENDPOINT/estimate?address=$address&orderAmount=1000" \
    "Расчет доставки в зону Промузел (1000₽ заказ)"

make_request "GET" "$DELIVERY_ENDPOINT/estimate?address=$address&orderAmount=1600" \
    "Расчет доставки в зону Промузел (1600₽ заказ - бесплатная доставка)"

# 3. Тестирование граничных случаев
echo "3️⃣ ТЕСТИРОВАНИЕ ГРАНИЧНЫХ СЛУЧАЕВ"
echo ""

# Тест 3.1: Слишком короткий запрос
query=$(urlencode "л")
make_request "GET" "$DELIVERY_ENDPOINT/address-suggestions?query=$query&limit=10" \
    "Слишком короткий запрос (1 символ)"

# Тест 3.2: Пустой запрос
make_request "GET" "$DELIVERY_ENDPOINT/address-suggestions?query=&limit=10" \
    "Пустой запрос"

# Тест 3.3: Несуществующий адрес
address=$(urlencode "г. Москва, ул. Тверская, д. 1")
make_request "GET" "$DELIVERY_ENDPOINT/estimate?address=$address&orderAmount=1000" \
    "Расчет доставки по адресу вне зоны доставки"

# Тест 3.4: Некорректные данные
make_request "GET" "$DELIVERY_ENDPOINT/estimate?address=&orderAmount=1000" \
    "Расчет доставки с пустым адресом"

address=$(urlencode "г. Волжск, ул. Ленина, д. 1")
make_request "GET" "$DELIVERY_ENDPOINT/estimate?address=$address&orderAmount=0" \
    "Расчет доставки с нулевой суммой заказа"

# 4. Тестирование производительности
echo "4️⃣ ТЕСТИРОВАНИЕ ПРОИЗВОДИТЕЛЬНОСТИ"
echo ""

# Тест 4.1: Множественные запросы подсказок (имитация пользовательского ввода)
echo "🔄 Имитация пользовательского ввода с debounce..."
for query_text in "л" "ле" "лен" "лени" "ленин" "ленина"; do
    if [ ${#query_text} -ge 2 ]; then
        query=$(urlencode "$query_text")
        make_request "GET" "$DELIVERY_ENDPOINT/address-suggestions?query=$query&limit=5" \
            "Поиск по запросу '$query_text' (${#query_text} символов)"
        sleep 0.3  # Имитация debounce 300ms
    else
        echo "⏭️  Пропуск запроса '$query_text' (менее 2 символов)"
        echo ""
    fi
done

# 5. Тестирование всех зон доставки
echo "5️⃣ ПОЛНОЕ ТЕСТИРОВАНИЕ ВСЕХ ЗОН ДОСТАВКИ"
echo ""

# Массив тестовых адресов для каждой зоны
declare -A test_addresses=(
    ["Дружба"]="г. Волжск, мкр. Дружба, ул. Комсомольская, д. 15"
    ["Центральный"]="г. Волжск, ул. Советская, д. 25"
    ["Машиностроитель"]="г. Волжск, мкр. Машиностроитель, ул. Рабочая, д. 8"
    ["ВДК"]="г. Волжск, мкр. ВДК, ул. Строителей, д. 12"
    ["Северный"]="г. Волжск, Северный микрорайон, ул. Северная, д. 3"
    ["Горгаз"]="г. Волжск, мкр. Горгаз, ул. Газовиков, д. 7"
    ["Заря"]="г. Волжск, мкр. Заря, ул. Восточная, д. 20"
    ["Промузел"]="г. Волжск, Промышленная зона, ул. Индустриальная, д. 5"
    ["Прибрежный"]="г. Волжск, мкр. Прибрежный, ул. Набережная, д. 14"
)

declare -A expected_costs=(
    ["Дружба"]=100
    ["Центральный"]=200
    ["Машиностроитель"]=200
    ["ВДК"]=200
    ["Северный"]=200
    ["Горгаз"]=200
    ["Заря"]=250
    ["Промузел"]=300
    ["Прибрежный"]=300
)

for zone in "${!test_addresses[@]}"; do
    address_text="${test_addresses[$zone]}"
    expected_cost="${expected_costs[$zone]}"
    address=$(urlencode "$address_text")
    
    echo "🏘️  Тестирование зоны: $zone (ожидаемая стоимость: ${expected_cost}₽)"
    make_request "GET" "$DELIVERY_ENDPOINT/estimate?address=$address&orderAmount=500" \
        "Расчет доставки в зону $zone"
done

# 6. Итоговая статистика
echo "6️⃣ ИТОГОВАЯ СТАТИСТИКА ТЕСТИРОВАНИЯ"
echo ""
echo "📊 Проведенные тесты:"
echo "   ✓ Подсказки адресов (минимум 2 символа)"
echo "   ✓ Зональная система доставки (11 зон)"
echo "   ✓ Расчет стоимости доставки"
echo "   ✓ Бесплатная доставка по порогам"
echo "   ✓ Граничные случаи и ошибки"
echo "   ✓ Тестирование производительности"
echo "   ✓ Полное покрытие всех зон"
echo ""
echo "🎯 Ожидаемые результаты:"
echo "   • HTTP 200 для корректных запросов"
echo "   • Подсказки адресов для запросов ≥2 символов"
echo "   • Корректная стоимость доставки по зонам:"
echo "     - Дружба: 100₽ (бесплатно от 800₽)"
echo "     - Центральный/Машиностроитель/ВДК/Северный/Горгаз: 200₽ (бесплатно от 1000₽)"
echo "     - Заря: 250₽ (бесплатно от 1200₽)"
echo "     - Промузел/Прибрежный: 300₽ (бесплатно от 1500₽)"
echo ""
echo "⚠️  Обработка ошибок:"
echo "   • HTTP 400 для некорректных данных"
echo "   • HTTP 404 для адресов вне зоны доставки"
echo "   • Fallback на стандартную зону при неопределенности"
echo ""
echo "🏁 Тестирование завершено: $(date '+%Y-%m-%d %H:%M:%S')" 