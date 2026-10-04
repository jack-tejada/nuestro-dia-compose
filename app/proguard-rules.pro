# ProGuard / R8 rules for Nuestro Día Compose

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt

# Keep Kotlinx Serializable classes and their companion object serializers
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

-keep,allowobfuscation,allowshrinking class * {
    @kotlinx.serialization.Serializable class *;
}

-keepclassmembers class * {
    @kotlinx.serialization.Serializable$Companion class *;
}

-keepclassmembers class **$serializer {
    public static final **$serializer INSTANCE;
}

# Navigation3 NavKey preservation
-keep interface androidx.navigation3.runtime.NavKey { *; }
-keep class * implements androidx.navigation3.runtime.NavKey { *; }

# Firebase Auth, Firestore, and Storage models
-keepattributes Signature
-keepclassmembers class com.google.firebase.** { *; }
-keepclassmembers class com.madrigalsolu.nuestrodia.compose.feature.capture.data.** { *; }
-keepclassmembers class com.madrigalsolu.nuestrodia.compose.feature.gallery.data.** { *; }
-keepclassmembers class com.madrigalsolu.nuestrodia.compose.feature.auth.data.** { *; }

# Compose Runtime
-keepclassmembers class * extends androidx.compose.runtime.State { *; }
