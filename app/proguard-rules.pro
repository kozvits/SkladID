# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Room
-keep class androidx.room.** { *; }

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class **$$serializer {
    *** INSTANCE;
}

# ML Kit
-keep class com.google.mlkit.** { *; }

# Model / DTO classes used with kotlinx.serialization
-keep,includedescriptorclasses class com.kozvits.skladid.**$$serializer { *; }
-keepclassmembers class com.kozvits.skladid.** {
    *** Companion;
}
