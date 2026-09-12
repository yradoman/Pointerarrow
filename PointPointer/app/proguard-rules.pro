-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class com.pointpointer.nav.data.model.** { *; }
-dontwarn androidx.datastore.**
-keepclassmembers class androidx.compose.material3.** { *; }
