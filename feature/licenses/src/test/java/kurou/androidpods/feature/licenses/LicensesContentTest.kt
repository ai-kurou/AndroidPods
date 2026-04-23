package kurou.androidpods.feature.licenses

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
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
    fun `LicensesContentが表示される`() {
        composeTestRule.setContent {
            LicensesContent()
        }

        composeTestRule.onRoot().assertIsDisplayed()
    }
}
