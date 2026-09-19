# PointArrow (Zero-Bloat Android Navigation)

Сучасний надійний пішохідний навігатор для Android на чистому **Jetpack Compose**, **Kotlin Coroutines / Flow**, **Manual DI** та **DataStore Preferences**.

## Що нового у версії 1.1.0 (Production Release):
1. **Релізна оптимізація та мінімізація бінарника (Release ProGuard / R8):**
   - Увімкнено `isMinifyEnabled = true`, `isShrinkResources = true`.
   - Вирізано всі виклики `android.util.Log` за допомогою `-assumenosideeffects`.
   - NDK ABI фільтр обмежено виключно `arm64-v8a` для зменшення ваги APK.
   - Ресурси локалізації обмежено українською (`uk`) та англійською (`en`).
   - Налагодження вимкнено: `isDebuggable = false` та `android:debuggable="false"`.

2. **WGS-84 геодезична точність та фільтрація:**
   - Розрахунок геодезичної відстані та початкового азимуту за моделлю еліпсоїда WGS-84 через `Location.distanceBetween()`.
   - Експоненційний фільтр низьких частот (Low-Pass Filter, $\alpha = 0.2$) для плавного обертання стрілки без стрибків $0^\circ / 360^\circ$.
   - Пороговий гейтинг: замороження пеленгу при зупинці ($< 0.8$ м/с), високій похибці ($> 25$ м) та прибутті ($< 8$ м).
   - Екранне сповіщення «Пункт призначення досягнуто».

3. **Збережені точки (DataStore Preferences):**
   - Швидке збереження координат та висоти з головного екрана.
   - Список точок з кнопкою «Йти до точки» та видаленням.

---

## Інструкція зі збірки Production Release APK:

### Варіант А: Через термінал (Gradle)
Для збірки саме **Release** версії використовуйте команду `assembleRelease` (НЕ `assembleDebug`):
```bash
chmod +x gradlew
./gradlew assembleRelease
```
Зібраний підписаний APK знаходитиметься за шляхом:
```
app/build/outputs/apk/release/app-release.apk
```

### Варіант Б: В Android Studio
За замовчуванням Android Studio відкриває проєкти в режимі **debug**. Щоб збирати Release APK:
1. Відкрийте вкладку **Build Variants** (нижня ліва вертикальна панель Android Studio).
2. Для модуля `:app` змініть значення стовпця **Active Build Variant** з `debug` на **`release`**.
3. У верхньому меню оберіть **Build -> Build Bundle(s) / APK(s) -> Build APK(s)** або натисніть зелену кнопку **Run**.
4. Студія згенерує `app-release.apk` у папці `app/build/outputs/apk/release/`.
