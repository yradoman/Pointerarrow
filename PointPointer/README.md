# PointPointer - Android GPS Navigator Without Maps

Енергоефективний, легкий та компактний Android-додаток для навігації до обраної GPS-точки без використання карт.

## Основні характеристики:
- **Android 12+ (API 31–35)**
- **Jetpack Compose + Material 3**
- **Чистий MVVM (StateFlow + Coroutines)**
- **Preferences DataStore** (без Room/SQLite)
- **Manual Dependency Injection (AppContainer)** (без Hilt/Dagger)
- **Zero-Maps** (нуль важких картографічних SDK)
- **Автоматичне перемикання напрямку:**
  - Швидкість < 2.7 км/год -> Rotation Vector Sensor
  - Швидкість > 3.3 км/год -> GPS Course Over Ground
- **Circular Low Pass Filter з Deadband (0.45°)** проти тремтіння стрілки
- **Імпорт та експорт точок у JSON** через системний Android Storage Access Framework (SAF)

## Як відкрити проєкт:
1. Розархівуйте вміст архіву у вибрану папку.
2. Відкрийте **Android Studio** -> **File** -> **Open...** та виберіть цю папку.
3. Дочекайтеся завершення Gradle Sync.
4. Запустіть проєкт на пристрої з Android 12+ (API 31+).
