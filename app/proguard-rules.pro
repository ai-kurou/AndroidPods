# Stack trace の行番号を保持（Crashlytics でのデバッグに必要）
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin Serialization — @Serializable data class のフィールドを保持
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class **$$serializer {
    static **$$serializer INSTANCE;
}
-keep @kotlinx.serialization.Serializable class * {
    static ** Companion;
    static ** $$serializer;
    private final <fields>;
}

# Ktor — OkHttp エンジン
-dontwarn org.slf4j.**
-keep class io.ktor.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Hilt / Dagger — コード生成クラスを保持
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclasseswithmembernames class * {
    @dagger.* <fields>;
    @javax.inject.* <fields>;
}