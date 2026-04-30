package kurou.androidpods.feature.settings

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import kurou.androidpods.core.domain.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ThemeModeDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `タイトルと選択肢が表示されEscapeキーでonDismissが呼ばれる`() {
        var dismissed = false
        composeTestRule.setContent {
            ThemeModeDialog(currentMode = ThemeMode.SYSTEM, onDismiss = { dismissed = true }, onModeSelected = {})
        }

        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
        composeTestRule.onNodeWithText("System").assertIsDisplayed()
        composeTestRule.onNodeWithText("Light").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark").assertIsDisplayed()

        composeTestRule.onNodeWithText("Theme")
            .performKeyInput { pressKey(Key.Escape) }
        assertTrue(dismissed)
    }

    @Test
    fun `選択肢をタップするとonModeSelectedが呼ばれる`() {
        var selected: ThemeMode? = null
        composeTestRule.setContent {
            ThemeModeDialog(
                currentMode = ThemeMode.SYSTEM,
                onDismiss = {},
                onModeSelected = { selected = it },
            )
        }

        composeTestRule.onNodeWithText("Dark").performClick()
        assertEquals(ThemeMode.DARK, selected)
    }
}
