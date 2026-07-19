# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# kotlinx.serialization — DTOs under com.mutissx.dicechallenge.data.remote.model
# are @Serializable and parsed reflectively by the generated $$serializer
# classes; R8 doesn't see that reflective link from the Retrofit converter,
# so it can strip pieces of the serialization machinery under aggressive
# minification unless explicitly kept.
# ---------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class com.mutissx.dicechallenge.**$$serializer { *; }
-keepclassmembers class com.mutissx.dicechallenge.** {
    *** Companion;
}
-keepclasseswithmembers class com.mutissx.dicechallenge.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---------------------------------------------------------------------------
# Retrofit — standard rules from Retrofit's own consumer ProGuard config,
# duplicated here as a safety net. Retrofit resolves service method generics
# and annotations at runtime.
# ---------------------------------------------------------------------------
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# ---------------------------------------------------------------------------
# OkHttp — defensive rules for optional platform classes it probes for at
# runtime that this app doesn't ship (Conscrypt/BouncyCastle/OpenJSSE).
# ---------------------------------------------------------------------------
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ---------------------------------------------------------------------------
# Room — entities/DAOs are referenced by class literal (not string-based
# reflection) throughout the app, so R8 keeps them as reachable by default;
# this is a defensive keep for the generated database implementation.
# ---------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ---------------------------------------------------------------------------
# Koin — DSL-based (module { single { ... } }), not reflection-heavy like
# Dagger/Hilt, but keep the core classes as a safety net since the module
# graph is resolved at runtime rather than compile time.
# ---------------------------------------------------------------------------
-keep class org.koin.** { *; }
-keepclassmembers class * {
    public <init>(...);
}
