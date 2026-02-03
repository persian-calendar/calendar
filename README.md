# Persian Calendar - Kotlin Multiplatform

[![](https://jitpack.io/v/persian-calendar/calendar.svg)](https://jitpack.io/#persian-calendar/calendar)
[![Build Status](https://github.com/persian-calendar/calendar/workflows/KMP%20Build/badge.svg)](https://github.com/persian-calendar/calendar/actions)

A **Kotlin Multiplatform** library for Persian (Jalali), Islamic (Hijri), Nepali, and Gregorian calendar conversions.

## 🌟 Features

- ✅ **Pure Kotlin** - No platform dependencies
- ✅ **Multiplatform** - Android, iOS, JVM, JS, WASM
- ✅ **Lightweight** - Zero external dependencies
- ✅ **Accurate** - Based on astronomical calculations
- ✅ **Fast** - Optimized lookup tables where possible
- ✅ **Well-tested** - Comprehensive test coverage

## 📦 Installation

### Kotlin Multiplatform

```kotlin
// settings.gradle.kts
repositories {
    maven("https://jitpack.io")
}

// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.github.persian-calendar:calendar:1.5.0")
        }
    }
}
```

### Android Only

```kotlin
dependencies {
    implementation("com.github.persian-calendar:calendar-android:1.5.0")
}
```

### JVM / Desktop

```kotlin
dependencies {
    implementation("com.github.persian-calendar:calendar-jvm:1.5.0")
}
```

## 🚀 Quick Start

### Basic Usage

```kotlin
import io.github.persiancalendar.calendar.*

// Create a Persian date
val persian = PersianDate(1403, 11, 12)

// Convert to Gregorian
val gregorian = CivilDate(persian)
println(gregorian) // CivilDate(2025, 2, 1)

// Convert to Islamic
val islamic = IslamicDate(persian)
println(islamic) // IslamicDate(1446, 7, 21)

// Use Julian Day Number for conversions
val jdn = persian.toJdn()
val backToPersian = PersianDate(jdn)
```

### Working with Components

```kotlin
// Destructuring
val (year, month, day) = PersianDate(1403, 1, 1)

// Month operations
val nextMonth = persian.monthStartOfMonthsDistance(1)
val distance = persian.monthsDistanceTo(PersianDate(1404, 1, 1))
```

### Supported Calendars

| Calendar | Class | Example |
|----------|-------|---------|
| Persian (Jalali) | `PersianDate` | `PersianDate(1403, 11, 12)` |
| Gregorian | `CivilDate` | `CivilDate(2025, 2, 1)` |
| Islamic (Hijri) | `IslamicDate` | `IslamicDate(1446, 7, 21)` |
| Nepali (Bikram Sambat) | `NepaliDate` | `NepaliDate(2081, 10, 18)` |

### Islamic Calendar Variants

```kotlin
// Use Umm al-Qura calendar (Saudi Arabia)
IslamicDate.useUmmAlQura = true

// Apply offset to Islamic dates
IslamicDate.islamicOffset = -1
```

## 📱 Platform Examples

### Android with Jetpack Compose

```kotlin
@Composable
fun PersianDateDisplay() {
    val today = remember {
        val now = LocalDate.now()
        PersianDate(CivilDate(now.year, now.monthValue, now.dayOfMonth))
    }

    Text("امروز: ${today.year}/${today.month}/${today.dayOfMonth}")
}
```

### iOS with SwiftUI

```swift
import PersianCalendar

struct ContentView: View {
    let persianToday: PersianDate

    init() {
        let now = Date()
        let calendar = Calendar.current
        let components = calendar.dateComponents([.year, .month, .day], from: now)

        let civil = CivilDate(
            year: Int32(components.year!),
            month: Int32(components.month!),
            dayOfMonth: Int32(components.day!)
        )

        persianToday = PersianDate(date: civil)
    }

    var body: some View {
        Text("امروز: \(persianToday.year)/\(persianToday.month)/\(persianToday.dayOfMonth)")
    }
}
```

### Compose Multiplatform

```kotlin
@Composable
fun CalendarApp() {
    var selectedDate by remember { mutableStateOf(PersianDate(1403, 1, 1)) }

    Column {
        Text("Persian: ${selectedDate.year}/${selectedDate.month}/${selectedDate.dayOfMonth}")

        val civil = CivilDate(selectedDate)
        Text("Gregorian: ${civil.year}/${civil.month}/${civil.dayOfMonth}")

        val islamic = IslamicDate(selectedDate)
        Text("Islamic: ${islamic.year}/${islamic.month}/${islamic.dayOfMonth}")
    }
}
```

## 🔧 Advanced Features

### Leap Year Detection

```kotlin
// Persian calendar leap year check (via JDN conversion)
val isPersianLeap = PersianDate(1403, 12, 30).let { date ->
    try {
        date.toJdn()
        true
    } catch (e: Exception) {
        false
    }
}
```

### Date Arithmetic

```kotlin
// Add months
val nextYear = persianDate.monthStartOfMonthsDistance(12)

// Calculate duration
val monthsDiff = startDate.monthsDistanceTo(endDate)
```

### JDN Conversions

```kotlin
// All calendars use Julian Day Number as the universal converter
val jdn = 2460676L

val persian = PersianDate(jdn)
val civil = CivilDate(jdn)
val islamic = IslamicDate(jdn)
val nepali = NepaliDate(jdn)
```

## 🏗️ Architecture

```
Calendar System
├── AbstractDate (base class)
│   ├── toJdn(): Long (convert to Julian Day Number)
│   └── fromJdn(jdn): DateTriplet (convert from JDN)
│
├── PersianDate (astronomical + lookup tables)
├── CivilDate (Gregorian/Julian hybrid)
├── IslamicDate (multiple calculation methods)
└── NepaliDate (Bikram Sambat)
```

## 🧪 Testing

```bash
# Run all tests
./gradlew allTests

# Platform-specific tests
./gradlew jvmTest
./gradlew androidTest
./gradlew iosX64Test
./gradlew jsTest
```

## 📊 Supported Date Ranges

| Calendar | Range | Notes |
|----------|-------|-------|
| Persian | 1220-1498 | Lookup table + astronomical |
| Islamic | 1265-1449 | Iranian Islamic + Umm al-Qura |
| Nepali | 1975-2199 | Full lookup table |
| Gregorian | Unlimited | Algorithmic |

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md).

## 📄 License

```
Copyright 2024 Persian Calendar Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

## 🙏 Credits

- Astronomical calculations based on [Calendrical Calculations](http://www.calendarists.com/)
- Islamic calendar data from [roozbehp/qamari](https://github.com/roozbehp/qamari)
- Persian calendar tables from [calendar.ut.ac.ir](https://calendar.ut.ac.ir/)

## 📚 Documentation

- [Full Migration Guide](KMP_MIGRATION_GUIDE.md)
- [API Documentation](https://jitpack.io/com/github/persian-calendar/calendar/latest/javadoc/)
- [Sample Projects](examples/)

## 🌐 Related Projects

- [Persian Calendar Android App](https://github.com/persian-calendar/persian-calendar)
- [Islamic Calendar Converter](https://github.com/roozbehp/qamari)

---

**Made with ❤️ by the Persian Calendar team**

[![Star History](https://api.star-history.com/svg?repos=persian-calendar/calendar&type=Date)](https://star-history.com/#persian-calendar/calendar&Date)
