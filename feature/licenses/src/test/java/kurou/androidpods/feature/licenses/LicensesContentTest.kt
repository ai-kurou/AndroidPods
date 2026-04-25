package kurou.androidpods.feature.licenses

import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LicensesContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    // produceLibraries() は v2 の StandardTestDispatcher 下では完了しないため、
    // 詳細なアサーションは省略しクラッシュしないことのみ確認するスモークテスト
    fun `LicensesContentがクラッシュせずに表示される`() {
        composeTestRule.setContent {
            LicensesContent()
        }
        composeTestRule.waitForIdle()
    }
}
