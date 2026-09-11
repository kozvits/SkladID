# SkladID

Android-приложение для оператора склада: сфотографировать товар или бирку, распознать его (штрих-код / текст / фото через ИИ), определить адрес хранения по локальной базе (Склад → Стеллаж → Полка → Ячейка), сформировать этикетку и напечатать её на принтере TSC.

## Технологический стек

- **Kotlin**, **Jetpack Compose** (Material3)
- **Clean Architecture**: `data` / `domain` / `presentation`, паттерн MVVM
- **Hilt** — dependency injection
- **Room** — локальная БД товаров
- **OkHttp** (без Retrofit) — запросы к OpenRouter API
- **ML Kit** — Barcode Scanning + Text Recognition (on-device)
- **CameraX** — съёмка
- **ZXing** — генерация штрих-кода Code128
- **kotlinx.serialization** — JSON
- **GitHub Actions** — сборка `assembleDebug` на каждый push
- `minSdk 26`, `targetSdk 35`

## Архитектура

```
app/src/main/java/com/kozvits/skladid/
├── data/                  # Room, сеть (OpenRouter), парсинг JSON базы, репозитории
│   ├── local/             # Room DB, ML Kit-реализация, файловые утилиты
│   ├── remote/             # OkHttp-клиент OpenRouter + DTO
│   ├── repository/        # Реализации доменных репозиториев
│   └── warehouse/         # Парсер JSON-базы складов
├── domain/                # Модели, интерфейсы репозиториев, use-case'ы
│   ├── model/
│   ├── repository/
│   └── usecase/
├── presentation/          # Экраны Compose, ViewModel'и, навигация
│   ├── home/ capture/ recognition/ storage/ labelpreview/ settings/
│   ├── navigation/        # NavHost + общий ProductDraftViewModel
│   └── theme/
├── label/                 # Canvas-генерация этикетки + ZXing Code128
├── printer/                # TSPL-команды, Bluetooth SPP / TCP-соединение с принтером
└── di/                    # Hilt-модули
```

### Поток экрана «Добавить товар»

`Home → Capture (фото товара, фото бирки) → Recognition (ML Kit + ИИ) → Storage (адрес хранения) → LabelPreview (печать)`

Промежуточные данные о товаре живут в `ProductDraftViewModel`, привязанной к `NavHost` — экраны не передают их через аргументы маршрута.

### Распознавание

1. ML Kit (Barcode + Text Recognition) выполняется **on-device** на фото бирки — даёт штрих-код и/или сырой текст (используются только как *подсказки*).
2. Фото товара всегда отправляется в OpenRouter (`POST /api/v1/chat/completions`, vision-модель) вместе с этими подсказками — модель обязана вернуть строгий JSON `{name, manufacturer, category, specs}`, из которого и заполняются поля товара.
3. Штрих-код можно поправить вручную на экране распознавания.

## Сборка

```bash
git clone <repo-url>
cd SkladID
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

CI (`.github/workflows/Build.yml`) на каждый push гоняет юнит-тесты и собирает debug-APK, публикуя его как артефакт сборки.

## Настройка

Всё — в приложении, на экране **Настройки**:

- **API-ключ OpenRouter** (`sk-or-...`) — хранится в `EncryptedSharedPreferences` (AES-256-GCM), не логируется (заголовок `Authorization` явно редактируется в HTTP-логах).
- **Модель распознавания** — список подтягивается кнопкой «Обновить модели» (`GET /api/v1/models`, только vision-модели).
- **База размещения** — импортируется как `.json`-файл через системный проводник. Формат:

```json
{
  "warehouses": [
    {
      "name": "Склад 1",
      "racks": [
        {
          "name": "Стеллаж A",
          "shelves": [
            {
              "name": "Полка 1",
              "cells": ["Ячейка 1", "Ячейка 2", "Ячейка 3"]
            }
          ]
        }
      ]
    }
  ]
}
```

- **Принтер** — Bluetooth (Classic SPP, из списка сопряжённых устройств) или сеть (`IP:порт`, по умолчанию `9100`).
- **Размер этикетки** — ширина/высота в мм + DPI принтера (по умолчанию 40×30 мм, 203 DPI).

## Тесты

```bash
./gradlew testDebugUnitTest
```

Покрыты юнит-тестами:
- `WarehouseJsonParserTest` — валидный/невалидный JSON, проверка обязательных полей, round-trip сериализации.
- `TsplCommandBuilderTest` — корректность команд `SIZE`/`GAP`/`CLS`/`BITMAP`/`PRINT`, упаковка изображения в 1bpp построчно с выравниванием по байту.

## Известные ограничения

- Правило автоподбора ячейки (`SuggestStorageCellUseCase`) — «первая свободная», без учёта категории товара (задел под расширение оставлен в сигнатуре).
- Bluetooth-печать не проверяет, что выбранное сопряжённое устройство — именно TSC-принтер (протокол SPP одинаков для многих устройств).
