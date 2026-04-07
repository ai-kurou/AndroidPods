package kurou.androidpods.feature.licenses

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
class LicensesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `戻るボタンをタップするとonBackが呼ばれる`() {
        var backCalled = false
        composeTestRule.setContent {
            LicensesScreen(onBack = { backCalled = true })
        }

        composeTestRule.onNodeWithText("Open Source Licenses").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backCalled)
    }
}
