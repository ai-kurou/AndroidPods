plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kover)
}

android {
    namespace = "kurou.androidpods.core.domain"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Kotlin Coroutines (Flow)
    implementation(libs.kotlinx.coroutines.core)

    // Hilt (@Inject)
    implementation(libs.hilt.android)
}
