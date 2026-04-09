package kurou.androidpods.feature.devices

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.androidpods.core.domain.CompatibleDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DevicesContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `複数のデバイスが全て表示される`() {
        val devices = listOf(
            CompatibleDevice(name = "AirPods Pro (2nd Gen)", images = null),
            CompatibleDevice(name = "AirPods Max", images = null),
            CompatibleDevice(name = "Beats Studio Buds+", images = null),
        )
        composeTestRule.setContent {
            DevicesContent(devices = devices, columns = 2)
        }

        composeTestRule.onNodeWithText("AirPods Pro (2nd Gen)").assertExists()
        composeTestRule.onNodeWithText("AirPods Max").assertExists()
        composeTestRule.onNodeWithText("Beats Studio Buds+").assertExists()
    }
}
