package kurou.androidpods.feature.devices

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import kurou.androidpods.core.domain.CompatibleDevice
import kurou.androidpods.core.domain.DeviceImages
import org.junit.Assert.assertTrue
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

    @Test
    fun `TWSの画像があるデバイスの名前が表示される`() {
        val device =
            CompatibleDevice(
                name = "AirPods Pro (2nd Gen)",
                images =
                    DeviceImages.Tws(
                        left = android.R.drawable.ic_menu_camera,
                        right = android.R.drawable.ic_menu_camera,
                        case = android.R.drawable.ic_menu_camera,
                    ),
            )
        composeTestRule.setContent {
            DevicesContent(devices = listOf(device), columns = 2)
        }

        composeTestRule.onNodeWithText("AirPods Pro (2nd Gen)").assertExists()
    }

    @Test
    fun `Singleの画像があるデバイスの名前が表示される`() {
        val device =
            CompatibleDevice(
                name = "AirPods Max",
                images = DeviceImages.Single(body = android.R.drawable.ic_menu_camera),
            )
        composeTestRule.setContent {
            DevicesContent(devices = listOf(device), columns = 2)
        }

        composeTestRule.onNodeWithText("AirPods Max").assertExists()
    }

    @Test
    fun `DirectionDownキーでスクロール量が増えDirectionUpキーで減る`() {
        val devices = (1..20).map { CompatibleDevice(name = "Device $it", images = null) }
        lateinit var gridState: LazyGridState
        composeTestRule.setContent {
            val state = rememberLazyGridState()
            gridState = state
            DevicesContent(devices = devices, columns = 2, gridState = state)
        }

        composeTestRule.onRoot().performKeyInput { pressKey(Key.DirectionDown) }
        composeTestRule.waitForIdle()
        val scrollAfterDown =
            gridState.firstVisibleItemIndex * 1000 + gridState.firstVisibleItemScrollOffset
        assertTrue("DirectionDownでスクロールされること", scrollAfterDown > 0)

        composeTestRule.onRoot().performKeyInput { pressKey(Key.DirectionUp) }
        composeTestRule.waitForIdle()
        val scrollAfterUp =
            gridState.firstVisibleItemIndex * 1000 + gridState.firstVisibleItemScrollOffset
        assertTrue("DirectionUpでスクロールが戻ること", scrollAfterUp < scrollAfterDown)
    }
}
