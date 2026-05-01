# Add project specific ProGuard rules here.

# exp4j rules
-keep class net.objecthunter.exp4j.** { *; }
-keepclassmembers class net.objecthunter.exp4j.** { *; }

# Room rules
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose
-keep class androidx.compose.** { *; }

# Keep data classes
-keep class com.arcalculator.data.** { *; }
