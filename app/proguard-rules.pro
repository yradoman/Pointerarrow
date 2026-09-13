# Zero-Bloat ProGuard rules for PointArrow
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Kotlin coroutines and Flow internals safe
-keepclassmembers class kotlinx.coroutines.** {
    *** Companion;
}
-keep,allowobfuscation,allowshrinking class com.pointarrow.nav.data.model.** { *; }
