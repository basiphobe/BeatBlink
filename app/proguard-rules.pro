# BeatBlink ProGuard Rules
# Optimizations and obfuscation rules for release builds

# Keep all classes in the main package
-keep class com.beatblink.** { *; }

# Keep TarsosDSP library classes (audio processing)
-keep class be.tarsos.dsp.** { *; }
-dontwarn be.tarsos.dsp.**

# Keep AudioBeatDetector specifically
-keepclassmembers class com.beatblink.AudioBeatDetector {
    public *;
}

# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# Keep Compose UI classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Accompanist permissions
-keep class com.google.accompanist.permissions.** { *; }

# Keep Kotlin coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep StateFlow and Flow classes
-keep class kotlinx.coroutines.flow.** { *; }

# Android specific rules
-keep class android.media.** { *; }
-keep class androidx.lifecycle.** { *; }

# Remove logs in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable classes
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Don't warn about missing classes that aren't available on Android
-dontwarn java.awt.**
-dontwarn javax.sound.**
-dontwarn sun.misc.**
-dontwarn java.lang.management.**
-dontwarn javax.naming.**