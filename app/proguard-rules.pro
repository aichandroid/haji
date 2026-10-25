# ============================================================================
# R8 / Proguard Rules — Panduan Haji 2027 (Kepatuhan Privasi & Minifikasi Rilis)
# ============================================================================

# 1. PENCEGAHAN KEBOCORAN PII PADA LOGCAT (Section 36 & 37.1)
# Menghapus seluruh panggilan android.util.Log (v, d, i, w) pada build rilis agar
# tidak ada data koordinat GPS, token, atau informasi pribadi yang tercecer di log.
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

# 2. KOTLIN SERIALIZATION & DTO (ADR-003 & Network DTOs)
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * extends kotlinx.serialization.internal.GeneratedSerializer {
    <fields>;
    <methods>;
}
-keep,allowobfuscation,allowshrinking class com.haji2027.core.model.** { *; }
-keep,allowobfuscation,allowshrinking class com.haji2027.core.network.dto.** { *; }
-keep,allowobfuscation,allowshrinking class com.haji2027.feature.settings.ota.OtaManifest { *; }

# 3. ROOM DATABASE (SQLite Offline-First)
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <methods>;
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# 4. JETPACK COMPOSE RUNTIME
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# 5. KOTLINX COROUTINES
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# 6. PLAY SERVICES LOCATION & SENSORS
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.location.**
