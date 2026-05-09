plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kover)
}

android {
    namespace = "kurou.androidpods.core.domain"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

}

dependencies {
    // Kotlin Coroutines (Flow)
    implementation(libs.kotlinx.coroutines.core)

    // Hilt (@Inject)
    implementation(libs.hilt.android)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
