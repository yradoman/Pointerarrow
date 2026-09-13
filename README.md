# PointArrow (Zero-Bloat Android Navigation)

Сучасний надійний пішохідний навігатор для Android на чистому **Jetpack Compose**, **Kotlin Coroutines / Flow**, **Manual DI** та **DataStore Preferences**.

## Що нового у версії 1.1.0:
1. **Виправлення AndroidManifest.xml (AAPT2):**
   - Усунуто залежності від відсутніх mipmap-іконок (`ic_launcher`, `ic_launcher_round`). Встановлено векторну векторну іконку стрілки `@drawable/ic_launcher_foreground`.
2. **Типізація Flow у GpsLocationSource.kt:**
   - Потік оголошено як `val locationFlow: Flow<GpsUpdate?> = callbackFlow<GpsUpdate?> { ... }`, що дозволяє коректно надсилати `trySend(null)` / `emit(null)` при втраті сигналу або вимкненні провайдера.
3. **Нова вкладка «Збережені точки» (DataStore):**
   - Модель `Waypoint`: id (UUID), name, latitude, longitude, altitude, timestamp.
   - Сховище `WaypointPreferences` та репозиторій `WaypointRepository`.
   - Кнопка **«Зберегти»** на головному екрані компаса з діалогом введення назви.
   - Екран **«Точки»** зі списком, кнопкою швидкої навігації **«Йти до точки»** (активує стрілку компаса та перемикає екран) і кнопкою видалення.

## Архітектура
- **Мінімалістичний розмір:** ~4.5 МБ APK без важких ORM та сторонніх бібліотек.
- **AMOLED Dark Mode:** Справжній чорний колір (`#000000`) для збереження батареї.
- **Гібридне джерело курсу:** Магнітний компас на місці + GPS вектор при швидкості > 3 км/год.
