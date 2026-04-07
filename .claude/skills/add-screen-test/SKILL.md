---
name: add-screen-test
description: "画面・Composableのテスト追加を依頼されたとき（例: '〇〇のテストを書いて'、'ViewModelのテストを追加して'）に、Robolectric + MockK のパターンに従ってテストクラスを作成する"
---

# Add Screen Test

ViewModel または Composable UI のテストを追加する。

## 引数

$ARGUMENTS にテスト対象と概要を指定する（例: "SettingsViewModel BT状態の更新" または "LicensesScreen 戻るボタンの動作"）。

## 手順

以下の手順をすべて実行すること:

### 0. テスト対象のソースコードを読む

テスト対象のクラス（ViewModel または Composable）を先に読み、公開API・引数・StateFlowの型を把握する。

### 1. テストの種類を判断

- **ViewModel** → パターンA（MockK + UnconfinedTestDispatcher）
- **Composable（UI）** → パターンB（Robolectric + ComposeRule）
- **両方必要** → 両パターンで別ファイルを作成

### 2a. パターンA: ViewModel テスト

パス: `feature/{name}/src/test/java/kurou/androidpods/feature/{name}/{Name}ViewModelTest.kt`

```kotlin
package kurou.androidpods.feature.{name}

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.coVerify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.androidpods.core.domain.{UseCaseName}
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class {Name}ViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val fakeFlow = MutableStateFlow<{Type}>({initialValue})
    private val useCase = mockk<{UseCaseName}>(relaxUnitFun = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { useCase.observe() } returns fakeFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態は{期待値}を返す`() {
        val viewModel = {Name}ViewModel(useCase)

        // assertNull(viewModel.{state}.value) または assertEquals({expected}, viewModel.{state}.value)
    }

    @Test
    fun `observeのFlowに値を流すと{stateField}が更新される`() = runTest {
        val viewModel = {Name}ViewModel(useCase)

        val job = launch(testDispatcher) { viewModel.{stateField}.collect {} }
        fakeFlow.value = {newValue}
        assertEquals({newValue}, viewModel.{stateField}.value)
        job.cancel()
    }
}
```

ルール:
- Flowの観測には `launch(testDispatcher) { ... }` → アサート → `job.cancel()` のパターン
- 単発アクションの検証は `verify(exactly = 1) { useCase.{method}() }` または `coVerify` (suspend)
- `relaxUnitFun = true` にすると戻り値のないメソッドをスタブ不要にできる

### 2b. パターンB: Composable UI テスト

パス: `feature/{name}/src/test/java/kurou/androidpods/feature/{name}/{Name}ScreenTest.kt`

#### シンプルなComposable（Activityアクセス不要）

```kotlin
package kurou.androidpods.feature.{name}

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class {Name}ScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `{テスト内容を日本語で記述}`() {
        composeTestRule.setContent {
            {Name}Screen(/* 引数 */)
        }

        // アサーション
    }
}
```

#### Activityアクセスが必要（戻るボタン、onBackPressedDispatcher等）

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class {Name}ScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `戻るボタンをタップするとonBackが呼ばれる`() {
        var backCalled = false
        composeTestRule.setContent {
            {Name}Screen(onBack = { backCalled = true })
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backCalled)
    }

    @Test
    fun `ハードウェア戻るキーで画面を閉じる`() {
        composeTestRule.setContent {
            {Name}Screen(onBack = {})
        }

        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()

        assertTrue(composeTestRule.activity.isFinishing)
    }
}
```

Composable UIテストの注意点:
- `HorizontalPager` 内のノードは `assertIsDisplayed()` が失敗することがある。代わりに `assertExists()` を使う
- 画面の向きを指定する場合: `@Config(qualifiers = "port")` または `@Config(qualifiers = "land")`
- 複数のAPI levelを検証したい場合: `@Config(sdk = [30, 31])`
- Robolectricでシステムサービスが必要な場合: `ApplicationProvider.getApplicationContext<Context>()` を使う

### 3. テスト名のルール

- すべてのテスト名は**日本語**のバッククォート記法 `` `テスト内容を日本語で記述`() ``
- 「〇〇のとき〇〇が〇〇される/返される」の形式を基本とする
- 「初期状態は〇〇を返す」「〇〇をタップすると〇〇が呼ばれる」など状態・操作・結果を明記

### 4. テスト実行確認

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :feature:{name}:testDebugUnitTest
```

失敗した場合はエラー内容を分析し、原因と修正案を提示する。
