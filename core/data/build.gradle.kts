plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
}

android {
    namespace = "kurou.androidpods.core.data"
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

    // Hilt (DI)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // DataStore (永続化)
    implementation(libs.androidx.datastore.preferences)

    // Unit Test
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.test.core)
}
