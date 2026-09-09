# SkladID

Android-приложение для складского учёта. Фото товара → ML Kit + OpenRouter AI → локальная БД → этикетка TSC.

## Установка Gradle Wrapper

Если `gradle-wrapper.jar` отсутствует, загрузите его:
```
curl -L -o gradle/wrapper/gradle-wrapper.jar https://services.gradle.org/distributions/gradle-8.9-bin.zip
```
Или скопируйте из установленного Gradle.

## Сборка

```bash
chmod +x gradlew
bash ./gradlew assembleDebug
```
