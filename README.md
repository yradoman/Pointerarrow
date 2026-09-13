# PointArrow - Android GPS Navigator Without Maps

Енергоефективний, надлегкий та компактний Android-додаток для прямої навігації на одну цільову GPS-точку без карт та важких залежностей.

## Ключові характеристики за специфікацією:
- **minSdk 26, targetSdk 35**
- **Розмір Release APK:** ~4.5 МБ завдяки R8/ProGuard (`isMinifyEnabled = true`, `isShrinkResources = true`) та відсутності Room / Maps SDK / важких бібліотек
- **Zero-Bloat DataStore:** Збереження лише однієї поточної цільової точки через прямі примітивні ключі (Name, Lat, Lon, Alt)
- **Manual Dependency Injection:** Чистий `AppContainer` (без Hilt/Dagger)
- **Без фонової роботи:** Працює суворо на активному екрані, без Foreground Services та `ACCESS_BACKGROUND_LOCATION`
- **Автоматичне перемикання джерела курсу:**
  - Швидкість < 2.7 км/год: `Sensor.TYPE_ROTATION_VECTOR`
  - Швидкість > 3.3 км/год: GPS Course Over Ground (`Location.getBearing()`)
  - Гістерезис 0.6 км/год проти мерехтіння
- **Компенсація нахилу корпусу:** `SensorManager.remapCoordinateSystem()` для точного азимута при портретному або горизонтальному хваті
- **Фільтрація стрілки:** Circular Low Pass Filter (найкоротший кут без стрибків 359° <-> 0°) + Deadband (0.45°)
- **Vertical Offset (Δh):** Обчислення $\Delta h = h_{\text{цілі}} - h_{\text{поточна}}$. Текстовий бейдж `▲ +X м` / `▼ -X м` відображається тільки якщо $|\Delta h| \ge 10$ м
- **Життєвий цикл та UI:**
  - `collectAsStateWithLifecycle()` з таймаутом 5 секунд після згортання — автоматично зупиняє сенсори та GPS
  - Render Throttling (30 FPS)
  - Справжній AMOLED Black дизайн (`#000000`) з неоновою зеленою стрілкою
- **GitHub Actions CI/CD:** `.github/workflows/android.yml` з автоматичною збіркою Release APK через `./gradlew assembleRelease`
