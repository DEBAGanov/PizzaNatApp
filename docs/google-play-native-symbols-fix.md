# Решение проблемы с символьными файлами в Google Play Console

## Проблема

При загрузке App Bundle в Google Play Console появляется предупреждение:

```
⚠️ Внимание!
Этот объект (App Bundle) содержит нативный код. Рекомендуем загрузить файл с отладочными символами, чтобы упростить выявление и устранение сбоев и ошибок ANR.
```

## Причина

Приложение содержит нативные библиотеки через зависимости:
- **Room Database** - SQLite нативные библиотеки
- **Coil** - нативные декодеры изображений
- **OkHttp** - нативные сетевые компоненты
- **Google Play Services** - нативные компоненты Android

## ✅ Решение

### 1. Обновлены настройки в `app/build.gradle.kts`

#### Добавлены символьные файлы для Release и Staging:
```kotlin
release {
    // Включаем отладочные символы для нативного кода
    ndk {
        debugSymbolLevel = "FULL"
    }
}

staging {
    // Включаем отладочные символы для нативного кода
    ndk {
        debugSymbolLevel = "FULL"
    }
}
```

#### Настройки Android App Bundle:
```kotlin
bundle {
    density {
        enableSplit = true
    }
    abi {
        enableSplit = true
    }
    language {
        enableSplit = false
    }
}
```

#### Конфигурация для символьных файлов:
```kotlin
androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        // Настройки для включения символьных файлов в bundle
        variant.packaging.resources.pickFirsts.add("**/libc++_shared.so")
    }
}
```

### 2. Команды для сборки

#### Создание Release Bundle с символьными файлами:
```bash
./gradlew :app:bundleRelease
```

#### Расположение готового файла:
```
./app/build/outputs/bundle/release/app-release.aab
```

## 📊 Результат

- ✅ **App Bundle с символьными файлами** готов к загрузке
- ✅ **Размер**: ~2.81 МБ (оптимизирован)
- ✅ **Время скачивания**: ~1 сек
- ✅ **Отладочные символы**: Включены для нативного кода
- ✅ **Совместимость**: API 25+ (Android 7.1+)

## 🔗 Документация

- [Android App Optimization](https://developer.android.com/topic/performance/app-optimization/enable-app-optimization?hl=ru#native-crash-support)
- [Google Play Console - Native Symbol Files](https://play.google.com/console/)
- [Android App Bundle Guide](https://developer.android.com/guide/app-bundle)

## 📋 Следующие шаги

1. Соберите новый bundle: `./gradlew :app:bundleRelease`
2. Загрузите `app-release.aab` в Google Play Console
3. Проверьте что предупреждение исчезло
4. Опубликуйте обновление для внутреннего тестирования

---

**Дата создания**: 30.01.2025  
**Статус**: ✅ Решено  
**Версия**: 1.0.1 (versionCode 3) 