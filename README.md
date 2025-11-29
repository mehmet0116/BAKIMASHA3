# BAKIMASHA3

Android Telefon Uygulaması

## Proje Yapısı

```
BAKIMASHA3/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/bakimasha3/app/
│   │       │   └── MainActivity.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml
│   │       │   ├── values/
│   │       │   │   ├── colors.xml
│   │       │   │   ├── strings.xml
│   │       │   │   └── themes.xml
│   │       │   └── xml/
│   │       │       ├── backup_rules.xml
│   │       │       └── data_extraction_rules.xml
│   │       └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
└── gradlew.bat
```

## Gereksinimler

- Android Studio Arctic Fox veya daha yeni
- JDK 8 veya üzeri
- Android SDK 34 (API Level 34)
- Minimum SDK: 24 (Android 7.0)

## Kurulum

1. Bu projeyi Android Studio ile açın
2. Gradle senkronizasyonunun tamamlanmasını bekleyin
3. Emülatör veya fiziksel cihazda çalıştırın

## Derleme

Komut satırından derlemek için:

```bash
./gradlew assembleDebug
```

Release APK oluşturmak için:

```bash
./gradlew assembleRelease
```

## Lisans

Bu proje MIT lisansı altında lisanslanmıştır.
