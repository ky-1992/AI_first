# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.example.neonataldiary.data.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep Coil
-dontwarn coil.**
