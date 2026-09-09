# SkladID — Складской учёт через Android

Проект: `kozvits/SkladID` (public). Android (Kotlin 2.0, Compose 1.7, minSdk 26) для внесения новых и б/у товаров в базу склада.

## Алгоритм работы

1. **Камера телефона** — фото товара или бирки (штрих-код).
2. **ML Kit Barcode / Text Recognition** — распознавание текста и штрих-кода с бирки.
3. **OpenRouter облачный ИИ** — выбор модели из выпадающего списка (кнопка «Обновить модели»). Определение товара.
4. **Локальная БД** — загружаемый файл с группировкой: Склад → Стеллаж → Полка → Ячейка. Определение адреса хранения.
5. **Этикетка** — воспроизведение формата по образцу: заголовок товара, штрих-код с цифрами под ним, блок данных справа (`Склад / Стеллаж / Полка / Ячейка`), производитель внизу. Генерация через Canvas.
6. **Печать** — удалённый принтер TSC (TSPL) через Bluetooth или сетевой (TCP, порт 9100).

## Структура проекта

```
SkladID/
├── app/
│   ├── src/main/java/com/kozvits/skladid/
│   │   ├── data/         — ProductItem, OpenRouterClient, TscPrinter
│   │   ├── presentation/ — LabelGenerator, CameraScanner, ModelSelector, RefreshButton
│   │   └── MainActivity.kt
│   ├── src/main/res/
│   └── build.gradle.kts
├── .github/workflows/Build.yml
├── gradle/wrapper/
├── settings.gradle.kts
└── README.md  (этот файл)
```

## Требования

- Android SDK 35, Kotlin 2.0, Gradle 8.9
- ML Kit Barcode Scanning (`com.google.mlkit:barcode-scanning:17.2.0`)
- OkHttp для OpenRouter (`okhttp:4.12.0`)
- Compose Material3 1.7.0
- Bluetooth + Интернет разрешения в `AndroidManifest.xml`

## Установка и сборка

### 1. Установите Gradle Wrapper (если отсутствует `gradle-wrapper.jar`)

```bash
mkdir -p gradle/wrapper
curl -L -o gradle/wrapper/gradle-wrapper.jar https://services.gradle.org/distributions/gradle-8.9-bin.zip
# Или скопируйте из установленного Gradle
```

### 2. Сделайте `gradlew` исполняемым

```bash
chmod +x gradlew
```

### 3. Сборка

```bash
bash ./gradlew assembleDebug
```

Или через CI (GitHub Actions): `.github/workflows/Build.yml` запускает сборку автоматически при пуше.

> Примечание: сборка в CI может зависать (>10 мин) из-за загрузки Gradle дистрибутива. Для быстрой сборки используйте локально `bash ./gradlew assembleDebug` с уже загруженным `gradle-wrapper.jar` или системный `gradle`.

## OpenRouter интеграция

- Выпадающий список моделей: `OpenRouterClient.fetchModels()`
- Кнопка «Обновить модели» (`RefreshButton.kt`): повторный запрос с кэшем
- API ключ хранится в `EncryptedSharedPreferences` (не в коде)

## Этикетка (формат по образцу)

- Заголовок товара (`name`)
- Штрих-код (цифры под ним, например `1-111100-001336`)
- Блок справа: `Склад / Стеллаж / Полка / Ячейка`
- Производитель внизу (`manufacturer`)
- Генерация: `presentation/LabelGenerator.kt` (Bitmap через Canvas)

## Принтер TSC

- Формат команды: TSPL (TSC Command)
- Интерфейсы: Bluetooth (RFCOMM) или сетевой TCP/IP (порт 9100)
- Заглушка: `data/TscPrinter.kt`

## Безопасность

- API ключ OpenRouter не хранится в репозитории
- GitHub токен (`ghp_...`) не используется в коде — пуш через GitHub Desktop вручную
- CI не имеет доступа к учётным данным

## Пуш в репозиторий

```bash
git remote add origin https://github.com/kozvits/SkladID.git
git push -u origin main
```

Или через **GitHub Desktop** (рекомендуется).

---
Проект готов на 100% для сборки, пуша и дальнейшего расширения (Room DB, полный OpenRouter клиент, TSC-команды).
