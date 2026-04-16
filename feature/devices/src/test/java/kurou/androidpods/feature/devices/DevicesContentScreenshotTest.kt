package kurou.androidpods.feature.devices

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import kurou.androidpods.core.domain.CompatibleDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-port-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DevicesContentScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleDevices = listOf(
        CompatibleDevice(name = "AirPods Pro (2nd Gen)", images = null),
        CompatibleDevice(name = "AirPods (4th Gen)", images = null),
        CompatibleDevice(name = "AirPods Max", images = null),
        CompatibleDevice(name = "Beats Studio Buds+", images = null),
        CompatibleDevice(name = "Powerbeats Pro", images = null),
        CompatibleDevice(name = "Beats Solo3", images = null),
    )

    @Test
    fun `デバイスリスト_2列_縦向き`() {
        composeTestRule.setContent {
            DevicesContent(devices = sampleDevices, columns = 2)
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun `デバイスリスト_3列_縦向き`() {
        composeTestRule.setContent {
            DevicesContent(devices = sampleDevices, columns = 3)
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun `デバイスリスト_4列_縦向き`() {
        composeTestRule.setContent {
            DevicesContent(devices = sampleDevices, columns = 4)
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    @Config(qualifiers = "w640dp-h360dp-land-xxhdpi")
    fun `デバイスリスト_2列_横向き`() {
        composeTestRule.setContent {
            DevicesContent(devices = sampleDevices, columns = 2)
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    @Config(qualifiers = "w640dp-h360dp-land-xxhdpi")
    fun `デバイスリスト_3列_横向き`() {
        composeTestRule.setContent {
            DevicesContent(devices = sampleDevices, columns = 3)
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    @Config(qualifiers = "w640dp-h360dp-land-xxhdpi")
    fun `デバイスリスト_4列_横向き`() {
        composeTestRule.setContent {
            DevicesContent(devices = sampleDevices, columns = 4)
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
