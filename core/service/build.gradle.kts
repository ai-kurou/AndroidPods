plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
}

android {
    namespace = "kurou.androidpods.core.service"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // プロジェクトモジュール
    implementation(project(":core:domain"))
    implementation(project(":core:data"))

    // Hilt (DI)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // AndroidX (ServiceCompat)
    implementation(libs.androidx.core.ktx)

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Unit Test
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.android.compiler)
}
