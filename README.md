# 🕒 ClockGap

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-red.svg)](https://m3.material.io/)
[![Offline First](https://img.shields.io/badge/Privacy-100%25%20Offline-brightgreen.svg)](https://github.com/abdullah-ateeq/ClockGap)

**ClockGap** is a modern, intuitive Android utility built with Jetpack Compose that calculates the precise duration and difference between two clock times. Whether you're tracking work hours, shift schedules, flight durations, or overnight time spans, ClockGap provides instant results, clear visual representations, and flexible calculation modes.

---

## ✨ Features

- **3 Flexible Calculation Modes**:
  - **Direct Mode (Default)**: Measures time difference within the same 24-hour day.
  - **Across Midnight Mode**: Intelligently handles overnight intervals crossing past midnight into the next day.
  - **Shortest Gap Mode**: Treats the clock as a 24-hour circle to find the shortest angular distance between two times.
- **Rich Visualizations**:
  - **Circular 24-Hour Dial**: Graphical representation of start, end, and elapsed interval on a circular clock.
  - **Horizontal Timeline**: Linear visualization for quick duration assessment.
- **Detailed Time Breakdown**:
  - Displays difference in **Hours & Minutes**, **Total Minutes**, **Seconds**, and **Decimal Hours** (e.g., `8.5 hours`).
- **History & Recent Comparisons**:
  - Saves your recent time comparisons locally so you can review or reload past calculations instantly.
- **Quick Time Selectors & Presets**:
  - Handy presets for current time, top of the hour, or standard shift times.
  - Swap time buttons and single-tap clear options.
- **Customization & Modern UI**:
  - **Material 3 Design System** with Dynamic Color (Material You) support.
  - Full **Dark Mode**, **Light Mode**, and **System Theme** switching.
  - Haptic feedback and smooth animations.
  - Respects system 12-hour (AM/PM) and 24-hour time formatting settings.
- **Privacy First & 100% Offline**:
  - No internet permissions requested.
  - Zero analytics, tracking, or ads.

---

## 📐 Calculation Modes Explained

| Mode | Behavior | Example (`11:00 PM` ↔ `01:00 AM`) |
| :--- | :--- | :--- |
| **Direct Mode** | Compares times strictly within the same 24-hour calendar day. | `22 hours` |
| **Across Midnight** | Assumes Time B occurs after Time A, crossing into the next day if necessary. | `2 hours` (Next day arrival) |
| **Shortest Gap** | Calculates the minimal distance around a 24-hour clock face. | `2 hours` (Shortest distance) |

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/)
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture Principles
  - **Domain**: Pure Kotlin calculation engine (`ClockGapCalculator`), validators, and formatters.
  - **Data**: Local repository pattern using `SharedPreferences` for user settings and history.
  - **UI / ViewModel**: Unidirectional Data Flow (UDF) powered by `StateFlow` and `Coroutines`.
- **Navigation**: Jetpack Navigation Compose
- **Target SDK**: Android 14+ (API 34)
- **Min SDK**: Android 8.0 (API 26 - Java 8 / `java.time` support)

---

## 📁 Project Structure

```
app/src/main/java/com/example/clockgap/
├── domain/             # Core calculation logic & time formatters
│   ├── ClockGapCalculator.kt
│   ├── ClockGapFormatter.kt
│   └── ClockGapValidator.kt
├── model/              # Data models & state classes
│   ├── AppSettings.kt
│   ├── ClockGapResult.kt
│   ├── ClockGapUiState.kt
│   └── ClockGapHistory.kt
├── repository/         # Local persistence for history & settings
│   ├── ClockGapHistoryRepository.kt
│   └── SettingsRepository.kt
├── ui/                 # Jetpack Compose UI Screens & ViewModels
│   ├── home/           # Main calculator screen & visualizer components
│   ├── history/        # Recent comparisons screen
│   ├── settings/       # App preferences & theme configuration
│   ├── about/          # Mode explanations & app identity
│   ├── theme/          # Material 3 colors, typography, & themes
│   └── ClockGapViewModel.kt
└── util/               # Clipboard, Haptic, & Share helpers
```

---

## 🚀 Getting Started

### Prerequisites
- [Android Studio Jellyfish](https://developer.android.com/studio) or newer
- JDK 17
- Android SDK 34

### Building & Running

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/ClockGap.git
   cd ClockGap
   ```

2. **Build the Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Run Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

---

## 📜 License

```
Copyright (c) 2025 Abdullah Ateeq

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the
```
