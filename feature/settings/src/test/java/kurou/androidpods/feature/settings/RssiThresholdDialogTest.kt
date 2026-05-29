package kurou.androidpods.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.androidpods.core.domain.RssiThreshold
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RssiThresholdDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `ダイアログが表示されて必要な文字列が全て表示されていずれかを選択する`() {
        var selected: RssiThreshold? = null
        composeTestRule.setContent {
            RssiThresholdDialog(
                currentThreshold = RssiThreshold.VERY_NEAR,
                onDismiss = {},
                onThresholdSelected = { selected = it },
            )
        }

        composeTestRule.onNodeWithText("Signal Strength Filter").assertIsDisplayed()
        composeTestRule.onNodeWithText("All devices").assertIsDisplayed()
        composeTestRule.onNodeWithText("Medium range (~5m)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Near range (~2m)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Very near (~1m)").assertIsDisplayed()

        composeTestRule.onNodeWithText("All devices").performClick()
        assertEquals(RssiThreshold.ALL, selected)
    }

    @Test
    fun `ダイアログが表示されてキャンセルを押す`() {
        var dismissed = false
        composeTestRule.setContent {
            RssiThresholdDialog(
                currentThreshold = RssiThreshold.VERY_NEAR,
                onDismiss = { dismissed = true },
                onThresholdSelected = {},
            )
        }

        composeTestRule.onNodeWithText("Cancel").performClick()
        assertTrue(dismissed)
    }
}
