package kurou.androidpods.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PermissionRequiredDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `タイトルとメッセージが表示される`() {
        composeTestRule.setContent {
            PermissionRequiredDialog(onDismiss = {}, onConfirm = {})
        }

        composeTestRule.onNodeWithText("Bluetooth Permission Required").assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                "This app requires Bluetooth permissions to connect to devices. Please grant permissions in Settings.",
            ).assertIsDisplayed()
    }

    @Test
    fun `Open SettingsボタンをタップするとonConfirmが呼ばれる`() {
        var confirmed = false
        composeTestRule.setContent {
            PermissionRequiredDialog(onDismiss = {}, onConfirm = { confirmed = true })
        }

        composeTestRule.onNodeWithText("Open Settings").performClick()
        assertTrue(confirmed)
    }

    @Test
    fun `CancelボタンをタップするとonDismissが呼ばれる`() {
        var dismissed = false
        composeTestRule.setContent {
            PermissionRequiredDialog(onDismiss = { dismissed = true }, onConfirm = {})
        }

        composeTestRule.onNodeWithText("Cancel").performClick()
        assertTrue(dismissed)
    }
}
