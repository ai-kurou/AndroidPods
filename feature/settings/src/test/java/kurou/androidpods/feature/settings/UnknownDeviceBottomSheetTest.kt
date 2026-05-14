package kurou.androidpods.feature.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UnknownDeviceBottomSheetTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `デバイス名を入力してReportをタップするとonReportが正しい引数で呼ばれる`() {
        var reportedModelCode: String? = null
        var reportedDeviceName: String? = null
        composeTestRule.setContent {
            UnknownDeviceBottomSheet(
                modelCode = "0x1234",
                onDismiss = {},
                onReport = { modelCode, deviceName ->
                    reportedModelCode = modelCode
                    reportedDeviceName = deviceName
                },
            )
        }

        composeTestRule.onNodeWithText("Device name").performTextInput("AirPods Max")
        composeTestRule.onNodeWithText("Report").performClick()

        assertEquals("0x1234", reportedModelCode)
        assertEquals("AirPods Max", reportedDeviceName)
    }

    @Test
    fun `Reportをタップした後はボタンが無効化される`() {
        composeTestRule.setContent {
            UnknownDeviceBottomSheet(
                modelCode = "0x1234",
                onDismiss = {},
                onReport = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithText("Device name").performTextInput("AirPods Max")
        composeTestRule.onNodeWithText("Report").assertIsEnabled()

        composeTestRule.onNodeWithText("Report").performClick()

        composeTestRule.onNodeWithText("Report").assertIsNotEnabled()
    }

    @Test
    fun `デバイス名が空または空白のみのときReportボタンは無効化されている`() {
        composeTestRule.setContent {
            UnknownDeviceBottomSheet(
                modelCode = "0x1234",
                onDismiss = {},
                onReport = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithText("Report").assertIsNotEnabled()

        composeTestRule.onNodeWithText("Device name").performTextInput("   ")
        composeTestRule.onNodeWithText("Report").assertIsNotEnabled()
    }

    @Test
    fun `シートをスワイプダウンするとonDismissが呼ばれる`() {
        var dismissed = false
        composeTestRule.setContent {
            UnknownDeviceBottomSheet(
                modelCode = "0x1234",
                onDismiss = { dismissed = true },
                onReport = { _, _ -> },
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Report Unknown Device")
            .performTouchInput { swipeDown() }
        composeTestRule.waitForIdle()

        assertTrue(dismissed)
    }
}
