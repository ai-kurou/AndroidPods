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
class BluetoothUnavailableDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `ダイアログが表示されて必要な文字列が全て表示されてOKを押す`() {
        var dismissed = false
        composeTestRule.setContent {
            BluetoothUnavailableDialog(onDismiss = { dismissed = true })
        }

        composeTestRule.onNodeWithText("Bluetooth Unavailable").assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                "This device does not have Bluetooth, so this app cannot be used.",
            ).assertIsDisplayed()

        composeTestRule.onNodeWithText("OK").performClick()
        assertTrue(dismissed)
    }
}
