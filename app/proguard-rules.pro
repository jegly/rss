# ─── SecureRSS ProGuard / R8 rules ───────────────────────────────────────────
# Base optimisation file is proguard-android-optimize.txt (set in build.gradle).
# Rules here are additive; they cover every library in the dependency graph.

# ── Attributes required by many libraries ─────────────────────────────────────
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations

# ── Kotlin ────────────────────────────────────────────────────────────────────
# Kotlin metadata is read at runtime by Kotlin reflection and many libraries.
-keep class kotlin.Metadata { *; }
-keep class kotlin.** { *; }
-dontwarn kotlin.**

# Kotlin coroutines – internal dispatcher and exception handler are loaded by name.
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ── App domain / data models ──────────────────────────────────────────────────
# Room generates code that accesses entity fields by name. Data class members
# must survive shrinking so the generated *_Impl classes can call them.
-keep class com.jegly.rss.domain.model.** { *; }
-keep class com.jegly.rss.data.local.** { *; }

# ── Room ──────────────────────────────────────────────────────────────────────
# Room ships its own consumer rules but explicit rules add a safety net.
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-dontwarn androidx.room.**

# ── Hilt / Dagger ─────────────────────────────────────────────────────────────
# Hilt ships its own consumer rules; these cover edge cases and generated names.
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.Module class * { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-dontwarn dagger.**
-dontwarn javax.inject.**

# ── Retrofit ──────────────────────────────────────────────────────────────────
# Retrofit generates interface proxies at runtime; keep annotations + interface methods.
-keep class retrofit2.** { *; }
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# ── OkHttp / Okio ─────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
# Public suffix database is read by name from a resource file.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase { *; }
# DoH module loads its DnsRecord codec via reflection in some configurations.
-keep class okhttp3.dnsoverhttps.** { *; }

# ── SQLCipher ─────────────────────────────────────────────────────────────────
# Loaded via JNI (System.loadLibrary("sqlcipher")); all classes must survive.
-keep class net.sqlcipher.** { *; }
-keep class net.zetetic.** { *; }
-dontwarn net.sqlcipher.**
-dontwarn net.zetetic.**

# ── Google Tink ───────────────────────────────────────────────────────────────
# Tink uses a registry pattern: primitives are registered and looked up by class
# object (getPrimitive(Aead::class.java)) and by string key template name.
# Every class in the tink tree must be kept to preserve these registrations.
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# ── AndroidX Security – EncryptedSharedPreferences ────────────────────────────
# Internally accesses MasterKey and AES-SIV/GCM implementations by class name.
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.**

# ── AndroidX Biometric ────────────────────────────────────────────────────────
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# ── AndroidX Browser – Custom Tabs ────────────────────────────────────────────
-keep class androidx.browser.customtabs.** { *; }
-dontwarn androidx.browser.**

# ── Jetpack Compose ───────────────────────────────────────────────────────────
# Compose's compiler plugin runs at compile time; runtime annotations are safe
# to strip. R8 handles Compose well in full mode. Only warn suppression needed.
-dontwarn androidx.compose.**

# ── Android OS ────────────────────────────────────────────────────────────────
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclasseswithmembernames class * {
    native <methods>;
}

# Honour @Keep from AndroidX annotations.
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# ── Logging – strip in release ─────────────────────────────────────────────────
# R8 removes these call sites entirely; the expressions passed as arguments are
# also elided when they have no other side effects.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
    public static boolean isLoggable(...);
}
-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
}
