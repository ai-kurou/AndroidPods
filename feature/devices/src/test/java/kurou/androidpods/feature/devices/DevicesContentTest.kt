package kurou.androidpods.feature.devices

import androidx.compose.ui.test.junit4.v2.createComposeRule
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
        val devices =
            listOf(
                CompatibleDevice(name = "AirPods Pro (2nd Gen)", modelCode = 0x1420),
                CompatibleDevice(name = "AirPods Max", modelCode = 0x0A20),
                CompatibleDevice(name = "Beats Studio Buds+", modelCode = 0x1620),
            )
        composeTestRule.setContent {
            DevicesContent(devices = devices, columns = 2)
        }

        composeTestRule.onNodeWithText("AirPods Pro (2nd Gen)").assertExists()
        composeTestRule.onNodeWithText("AirPods Max").assertExists()
        composeTestRule.onNodeWithText("Beats Studio Buds+").assertExists()
    }

    @Test
    fun `TWSデバイスの名前が表示される`() {
        val device = CompatibleDevice(name = "AirPods Pro (2nd Gen)", modelCode = 0x1420)
        composeTestRule.setContent {
            DevicesContent(devices = listOf(device), columns = 2)
        }

        composeTestRule.onNodeWithText("AirPods Pro (2nd Gen)").assertExists()
    }

    @Test
    fun `Singleデバイスの名前が表示される`() {
        val device = CompatibleDevice(name = "AirPods Max", modelCode = 0x0A20)
        composeTestRule.setContent {
            DevicesContent(devices = listOf(device), columns = 2)
        }

        composeTestRule.onNodeWithText("AirPods Max").assertExists()
    }
}
