# ASSANHANİL TECH-ASSIST

**Industrial Engineering & Reporting Platform (Assan Hanil Bursa)**

A high-end "Digital Field Engineer" platform for factory operations with offline engineering calculations, maintenance recipes, and professional Excel report generation.

## Features

### Core Functionality
- 🔧 **Visual Bearing Finder** - Find ISO codes by measuring dimensions (ID, OD, Width)
- ⚡ **Electrical Wizard** - Circuit calculations and cable sizing (Coming Soon)
- 📊 **Excel Report Generator** - Native .xlsx generation with Apache POI
- 📋 **Master Recipes** - Save and load maintenance templates
- 📷 **Smart Camera** - Photo capture with voice commands and annotation
- 🎤 **AI Voice Builder** - Create forms using voice commands

### Design Philosophy
- **Industrial Dark Mode** - AMOLED Black with Neon Blue accents
- **Glassmorphism** - Frosted glass UI effects
- **Split-View** - Adaptive layout for landscape orientation
- **Offline-First** - Full functionality without internet
- **Crash-Proof** - Autosave on every keystroke

## Technical Stack

| Component | Technology |
|-----------|------------|
| Platform | Native Android (Kotlin) |
| UI Framework | Jetpack Compose (Material 3) |
| Database | Room Database (Offline-first / Autosave) |
| Excel Engine | Apache POI (Native .xlsx generation) |
| Camera | CameraX |
| AI/Vision | ML Kit, Android Speech Recognizer |

## Project Structure

```
app/src/main/java/com/assanhanil/techassist/
├── data/
│   ├── local/
│   │   ├── dao/           # Room Data Access Objects
│   │   ├── entity/        # Room Entities
│   │   └── TechAssistDatabase.kt
│   └── repository/        # Repository Implementations
├── domain/
│   ├── model/            # Domain Models
│   ├── repository/       # Repository Interfaces
│   └── usecase/          # Use Cases
├── presentation/
│   ├── ui/
│   │   ├── components/   # Reusable UI Components
│   │   ├── screens/      # Screen Composables
│   │   └── theme/        # Theme (Colors, Typography)
│   └── viewmodel/        # ViewModels
├── service/
│   └── ExcelService.kt   # Excel Generation with Apache POI
├── util/                 # Utility Functions
├── MainActivity.kt
└── TechAssistApplication.kt
```

## Key Components

### ExcelService (Excel Generation)
- Native `.xlsx` generation using Apache POI
- Corporate header with company branding
- **Image Embedding**: Photos anchored INSIDE cells using `ClientAnchor`
- Auto-expanding rows to fit images
- Images resized to ~500KB to prevent OOM errors

### BearingFinder (Engineering Module)
- Technical bearing diagram visualization
- Input measured dimensions (ID, OD, Width)
- Query offline database with tolerance matching
- Returns ISO code (e.g., "6204-ZZ")

### Split-View Layout
- Adaptive layout for landscape orientation
- Left pane: Menu/List
- Right pane: Workspace
- Portrait mode: Full screen workspace

## Dependencies

```kotlin
// Jetpack Compose (Material 3)
implementation("androidx.compose:compose-bom:2023.10.01")
implementation("androidx.compose.material3:material3")

// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// Apache POI for Excel
implementation("org.apache.poi:poi:5.2.5")
implementation("org.apache.poi:poi-ooxml:5.2.5")

// CameraX
implementation("androidx.camera:camera-core:1.3.1")
implementation("androidx.camera:camera-camera2:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")
implementation("androidx.camera:camera-view:1.3.1")

// ML Kit
implementation("com.google.mlkit:text-recognition:16.0.0")
```

## Requirements

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or higher
- Android SDK 34 (API Level 34)
- Minimum SDK: 24 (Android 7.0)

## Building

```bash
./gradlew assembleDebug
```

## Testing

```bash
./gradlew test
```

## License

Proprietary - Assan Hanil Bursa
