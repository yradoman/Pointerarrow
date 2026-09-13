# Aggressive optimization and shrinking for minimal APK size (~4.5 MB)
-keepattributes *Annotation*, InnerClasses
-dontwarn java.lang.invoke.**
-keepclassmembers class * {
    *** Companion;
}
-keep,allowobfuscation,allowshrinking class com.pointarrow.nav.data.model.** { *; }
