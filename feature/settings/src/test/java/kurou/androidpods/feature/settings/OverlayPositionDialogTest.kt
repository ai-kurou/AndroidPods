package kurou.androidpods.feature.settings

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import kurou.androidpods.core.domain.OverlayPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OverlayPositionDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `タイトルと選択肢が表示されEscapeキーでonDismissが呼ばれる`() {
        var dismissed = false
        composeTestRule.setContent {
            OverlayPositionDialog(
                currentPosition = OverlayPosition.TOP,
                onDismiss = { dismissed = true },
                onPositionSelected = {},
            )
        }

        composeTestRule.onNodeWithText("Overlay position").assertIsDisplayed()
        composeTestRule.onNodeWithText("Top").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bottom").assertIsDisplayed()

        composeTestRule.onNodeWithText("Overlay position")
            .performKeyInput { pressKey(Key.Escape) }
        assertTrue(dismissed)
    }

    @Test
    fun `選択肢をタップするとonPositionSelectedが呼ばれる`() {
        var selected: OverlayPosition? = null
        composeTestRule.setContent {
            OverlayPositionDialog(
                currentPosition = OverlayPosition.TOP,
                onDismiss = {},
                onPositionSelected = { selected = it },
            )
        }

        composeTestRule.onNodeWithText("Bottom").performClick()
        assertEquals(OverlayPosition.BOTTOM, selected)
    }

    @Test
    fun `CancelボタンをタップするとonDismissが呼ばれる`() {
        var dismissed = false
        composeTestRule.setContent {
            OverlayPositionDialog(
                currentPosition = OverlayPosition.TOP,
                onDismiss = { dismissed = true },
                onPositionSelected = {},
            )
        }

        composeTestRule.onNodeWithText("Cancel").performClick()
        assertTrue(dismissed)
    }
}
