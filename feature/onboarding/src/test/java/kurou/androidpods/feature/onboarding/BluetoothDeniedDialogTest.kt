package kurou.androidpods.feature.onboarding

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
class BluetoothDeniedDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `タイトルとメッセージが表示される`() {
        composeTestRule.setContent {
            BluetoothDeniedDialog(onDismiss = {}, onConfirm = {})
        }

        composeTestRule.onNodeWithText("Bluetooth is Off").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bluetooth was not enabled. Please enable it in Settings.").assertIsDisplayed()
    }

    @Test
    fun `Open SettingsボタンをタップするとonConfirmが呼ばれる`() {
        var confirmed = false
        composeTestRule.setContent {
            BluetoothDeniedDialog(onDismiss = {}, onConfirm = { confirmed = true })
        }

        composeTestRule.onNodeWithText("Open Settings").performClick()
        assertTrue(confirmed)
    }

    @Test
    fun `CancelボタンをタップするとonDismissが呼ばれる`() {
        var dismissed = false
        composeTestRule.setContent {
            BluetoothDeniedDialog(onDismiss = { dismissed = true }, onConfirm = {})
        }

        composeTestRule.onNodeWithText("Cancel").performClick()
        assertTrue(dismissed)
    }
}
