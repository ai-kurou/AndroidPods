package kurou.androidpods.feature.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PermissionDeniedDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `タイトルとメッセージが表示される`() {
        composeTestRule.setContent {
            PermissionDeniedDialog(onDismiss = {}, onConfirm = {})
        }

        composeTestRule.onNodeWithText("Permission Required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bluetooth permissions were denied. Please grant them in Settings.").assertIsDisplayed()
    }

    @Test
    fun `Open SettingsボタンをタップするとonConfirmが呼ばれる`() {
        var confirmed = false
        composeTestRule.setContent {
            PermissionDeniedDialog(onDismiss = {}, onConfirm = { confirmed = true })
        }

        composeTestRule.onNodeWithText("Open Settings").performClick()
        assertTrue(confirmed)
    }

    @Test
    fun `CancelボタンをタップするとonDismissが呼ばれる`() {
        var dismissed = false
        composeTestRule.setContent {
            PermissionDeniedDialog(onDismiss = { dismissed = true }, onConfirm = {})
        }

        composeTestRule.onNodeWithText("Cancel").performClick()
        assertTrue(dismissed)
    }
}
