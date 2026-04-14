---
name: add-feature
description: "新しい画面・Featureモジュールの追加を依頼されたとき（例: '設定画面を作って'、'新しいFeatureモジュールを追加して'）に、build.gradle.kts・ViewModel・Screen・Navigation登録の雛形を一括で作成する"
---

# Add Feature Module

新しいFeatureモジュールをプロジェクトに追加する。

## 引数

$ARGUMENTS に Feature名を指定する（例: "settings"）。

## 手順

以下の手順をすべて実行すること:

### 1. settings.gradle.kts にモジュールを追加

`include(":feature:{name}")` を既存の feature モジュールの下に追加する。

### 2. build.gradle.kts を作成

パス: `feature/{name}/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "kurou.androidpods.feature.{name}"
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

    buildFeatures {
        compose = true
    }
}

dependencies {
    // プロジェクトモジュール
    implementation(project(":core:domain"))

    // Hilt (DI)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // AndroidX
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Unit Test
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
```

### 3. ViewModel を作成

パス: `feature/{name}/src/main/java/kurou/androidpods/feature/{name}/{Name}ViewModel.kt`

```kotlin
package kurou.androidpods.feature.{name}

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class {Name}ViewModel @Inject constructor(
    // UseCaseを注入
) : ViewModel() {
    // MutableStateFlow を private に持ち、StateFlow で公開する
}
```

### 4. Screen Composable を作成

パス: `feature/{name}/src/main/java/kurou/androidpods/feature/{name}/{Name}Screen.kt`

```kotlin
package kurou.androidpods.feature.{name}

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun {Name}Screen(
    modifier: Modifier = Modifier,
    viewModel: {Name}ViewModel = hiltViewModel(),
) {
    // UI実装
}
```

### 5. navigation モジュールに画面を登録

`navigation/src/main/java/kurou/androidpods/navigation/AppNavHost.kt` に以下を追加:
- Route 定数の追加（`Route.kt` に）
- `composable(Route.{NAME})` ブロックの追加

`navigation/build.gradle.kts` に新しい feature モジュールへの依存を追加:
```kotlin
implementation(project(":feature:{name}"))
```

### 6. ビルド確認

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :feature:{name}:testDebugUnitTest
```

ビルドが通ることを確認する。
