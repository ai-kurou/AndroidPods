---
name: add-screenshot-test
description: "スクリーンショットテストの追加を依頼されたとき（例: '〇〇のスクリーンショットテストを書いて'、'Roborazziテストを追加して'）に、Roborazzi + Robolectric のパターンに従ってスクリーンショットテストクラスを作成し、ベースライン画像を記録する"
---

# Add Screenshot Test

Roborazzi を使ったスクリーンショットテストを追加する。

## 引数

$ARGUMENTS にテスト対象のComposable名を指定する（例: "SettingsContent" または "DevicesContent 縦横2パターン"）。

## 手順

以下の手順をすべて実行すること:

### 0. テスト対象のソースコードを読む

テスト対象のComposableを先に読み、引数・プレビューパターンを把握する。

### 1. テストファイルを作成する

パス: `feature/{name}/src/test/java/kurou/androidpods/feature/{name}/{Name}ScreenshotTest.kt`

```kotlin
package kurou.androidpods.feature.{name}

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-port-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class {Name}ScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `{状態の説明}`() {
        composeTestRule.setContent {
            {Name}(/* 引数 */)
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    @Config(qualifiers = "w640dp-h360dp-land-xxhdpi")
    fun `{状態の説明}_横向き`() {
        composeTestRule.setContent {
            {Name}(/* 引数 */)
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
```

#### アノテーションのルール

- クラスに `@Config(sdk = [35], qualifiers = "w360dp-h640dp-port-xxhdpi")` — 縦向き360×640dp xxhdpiを標準とする
- クラスに `@GraphicsMode(GraphicsMode.Mode.NATIVE)` — ネイティブレンダリングを有効化（必須）
- 横向きパターンはメソッドに `@Config(qualifiers = "w640dp-h360dp-land-xxhdpi")` を付与

#### `RoborazziOptions` を使うべき場合

`HorizontalPager`・アニメーション・Lottieなど描画が非決定的なComposableは差分しきい値を設定する:

```kotlin
private val roborazziOptions = RoborazziOptions(
    compareOptions = RoborazziOptions.CompareOptions(
        changeThreshold = 0.05f,  // 5%以内の差分は許容
    ),
)

// 使用例
composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
```

### 2. テストケースの設計

以下のパターンを網羅する:
- 通常状態（最小引数・最大引数）
- エラー・警告が表示される状態
- 縦向き / 横向き（画面が向きに応じてレイアウトが変わる場合）
- 列数や表示数が変化する場合はそのバリエーション

### 3. ベースライン画像の記録

テストファイル作成後、ベースライン画像を生成する:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :feature:{name}:recordRoborazziDebug
```

成功すると `feature/{name}/src/test/snapshots/` に PNG が生成される。

### 4. 検証実行

記録後に検証が通ることを確認する:

```bash
./gradlew :feature:{name}:verifyRoborazziDebug
```

失敗した場合はエラー内容を分析し、原因と修正案を提示する。

### 5. 完了報告

- 作成したテストケース一覧
- 生成されたスナップショット数
- `recordRoborazziDebug` と `verifyRoborazziDebug` の結果
を報告する。スナップショットのコミットはユーザーの指示を待つ。
