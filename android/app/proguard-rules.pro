# ================= Electro phone-android =================
# R8/обфускация. Держим только то, что читается рефлексией.

# Общие атрибуты, нужные Retrofit/Moshi/корутинам.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# --- DTO-модели: Moshi читает поля рефлексией, имена ломать нельзя ---
-keep class uz.electro.remote.data.** { *; }
-keepclassmembers class uz.electro.remote.data.** { *; }

# --- Retrofit ---
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# --- Moshi (рефлективный адаптер по Kotlin metadata) ---
-keep class kotlin.Metadata { *; }
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
-keep,allowobfuscation @interface com.squareup.moshi.JsonQualifier

# --- OkHttp/Okio (безопасно игнорировать опциональные зависимости) ---
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
