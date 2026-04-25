package kurou.androidpods.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ThemeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `ライトテーマでprimaryカラーが正しい`() {
        composeTestRule.setContent {
            AndroidPodsTheme(darkTheme = false, dynamicColor = false) {
                val primary = MaterialTheme.colorScheme.primary
                assertEquals(Purple40, primary)
            }
        }
    }

    @Test
    fun `ダークテーマでprimaryカラーが正しい`() {
        composeTestRule.setContent {
            AndroidPodsTheme(darkTheme = true, dynamicColor = false) {
                val primary = MaterialTheme.colorScheme.primary
                assertEquals(Purple80, primary)
            }
        }
    }

    @Test
    fun `ライトテーマでsecondaryカラーが正しい`() {
        composeTestRule.setContent {
            AndroidPodsTheme(darkTheme = false, dynamicColor = false) {
                val secondary = MaterialTheme.colorScheme.secondary
                assertEquals(PurpleGrey40, secondary)
            }
        }
    }

    @Test
    fun `ダークテーマでsecondaryカラーが正しい`() {
        composeTestRule.setContent {
            AndroidPodsTheme(darkTheme = true, dynamicColor = false) {
                val secondary = MaterialTheme.colorScheme.secondary
                assertEquals(PurpleGrey80, secondary)
            }
        }
    }

    @Test
    fun `ライトテーマでtertiaryカラーが正しい`() {
        composeTestRule.setContent {
            AndroidPodsTheme(darkTheme = false, dynamicColor = false) {
                val tertiary = MaterialTheme.colorScheme.tertiary
                assertEquals(Pink40, tertiary)
            }
        }
    }

    @Test
    fun `ダークテーマでtertiaryカラーが正しい`() {
        composeTestRule.setContent {
            AndroidPodsTheme(darkTheme = true, dynamicColor = false) {
                val tertiary = MaterialTheme.colorScheme.tertiary
                assertEquals(Pink80, tertiary)
            }
        }
    }

    @Test
    fun `typographyがThemeのTypographyと一致する`() {
        composeTestRule.setContent {
            AndroidPodsTheme(darkTheme = false, dynamicColor = false) {
                val bodyLarge = MaterialTheme.typography.bodyLarge
                assertEquals(Typography.bodyLarge.fontSize, bodyLarge.fontSize)
                assertEquals(Typography.bodyLarge.lineHeight, bodyLarge.lineHeight)
                assertEquals(Typography.bodyLarge.letterSpacing, bodyLarge.letterSpacing)
            }
        }
    }

    @Test
    @Config(sdk = [31])
    fun `Android12以上でdynamicColorオンのライトテーマはcolorSchemeが設定される`() {
        var primary = Color.Unspecified
        composeTestRule.setContent {
            AndroidPodsTheme(darkTheme = false, dynamicColor = true) {
                primary = MaterialTheme.colorScheme.primary
            }
        }
        assertNotEquals(Color.Unspecified, primary)
        assertNotEquals(Purple40, primary)
    }

    @Test
    @Config(sdk = [31])
    fun `Android12以上でdynamicColorオンのダークテーマはcolorSchemeが設定される`() {
        var primary = Color.Unspecified
        composeTestRule.setContent {
            AndroidPodsTheme(darkTheme = true, dynamicColor = true) {
                primary = MaterialTheme.colorScheme.primary
            }
        }
        assertNotEquals(Color.Unspecified, primary)
        assertNotEquals(Purple80, primary)
    }
}
