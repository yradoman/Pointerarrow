# Zero-Bloat ProGuard rules for PointArrow
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 1. Захист класу додатка (відповідає за запобігання ClassNotFoundException)
-keep class com.pointarrow.nav.PointArrowApp { *; }

# 2. Збереження Kotlin Coroutines та Flow
-keepclassmembers class kotlinx.coroutines.** {
    *** Companion;
}

# 3. Захист моделей даних для DataStore Preferences
-keepclassmembers class com.pointarrow.nav.data.model.** { *; }

# 4. Повне видалення Logcat логів у релізній збірці для зменшення розміру та прискорення
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}