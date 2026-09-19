# Production Release ProGuard / R8 Rules for PointArrow (com.pointarrow.nav)

-repackageclasses 'com.pointarrow.nav.release'
-allowaccessmodification

# Retain stack trace line numbers for crash reporting while obfuscating source filenames
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Entry Points: Keep Application and Activity classes
-keep public class com.pointarrow.nav.PointArrowApp { *; }
-keep public class com.pointarrow.nav.MainActivity { *; }

# Data Layer: Keep DataStore models and serializable fields intact
-keep class com.pointarrow.nav.data.model.** { *; }
-keepclassmembers class com.pointarrow.nav.data.model.** { *; }
-keepclassmembers class * implements java.io.Serializable { *; }

# Kotlin Coroutines & Flow optimization
-keepclassmembers class kotlinx.coroutines.** {
    *** Companion;
}
-dontwarn kotlinx.coroutines.**

# Strip all Android Logging calls from release byte-code
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(...);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int println(...);
}
