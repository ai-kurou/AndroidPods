plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
}

android {
    namespace = "kurou.androidpods.core.service"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }


}

dependencies {
    // プロジェクトモジュール
    implementation(project(":core:domain"))

    // Hilt (DI)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // AndroidX (ServiceCompat)
    implementation(libs.androidx.core.ktx)

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Unit Test
    testImplementation(project(":core:data"))
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.android.compiler)

    // Instrumented Test
    androidTestImplementation(libs.androidx.test.runner)
}
