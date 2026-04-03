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
class BluetoothUnavailableDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `タイトルとメッセージが表示される`() {
        composeTestRule.setContent {
            BluetoothUnavailableDialog(onDismiss = {})
        }

        composeTestRule.onNodeWithText("Bluetooth Unavailable").assertIsDisplayed()
        composeTestRule.onNodeWithText("This device does not have Bluetooth, so this app cannot be used.").assertIsDisplayed()
    }

    @Test
    fun `OKボタンをタップするとonDismissが呼ばれる`() {
        var dismissed = false
        composeTestRule.setContent {
            BluetoothUnavailableDialog(onDismiss = { dismissed = true })
        }

        composeTestRule.onNodeWithText("OK").performClick()
        assertTrue(dismissed)
    }
}
