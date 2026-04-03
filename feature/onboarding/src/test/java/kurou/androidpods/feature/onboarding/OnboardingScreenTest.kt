package kurou.androidpods.feature.onboarding

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30, 31])
class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun btAdapter(context: Context) =
        context.getSystemService(BluetoothManager::class.java).adapter

    private fun grantRequiredPermissions(context: Context) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        else
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        shadowOf(context as Application).grantPermissions(*permissions)
    }

    @Test
    fun `ボタンを押してページ1からページ3へ遷移してonCompleteが呼ばれる`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        shadowOf(btAdapter(context)).setEnabled(true)
        grantRequiredPermissions(context)

        var completed = false
        composeTestRule.setContent {
            OnboardingScreen(onComplete = { completed = true })
        }

        composeTestRule.onNodeWithTag("lottie_animation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Check the remaining battery level of your AirPods.").assertExists()

        composeTestRule.onNodeWithText("Next").performClick()           // page 0 → 1
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("lottie_animation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Location permission is required to use Bluetooth.").assertExists()

        composeTestRule.onNodeWithText("Grant Permission").performClick() // page 1 → 2
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("lottie_animation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please enable Bluetooth to connect to your AirPods.").assertExists()
        composeTestRule.onNodeWithText("Enable Bluetooth").assertIsDisplayed()

        composeTestRule.onNodeWithText("Enable Bluetooth").performClick() // onComplete
        assertTrue(completed)
    }

    @Test
    fun `スワイプしてもページが遷移しない`() {
        composeTestRule.setContent {
            OnboardingScreen(onComplete = {})
        }

        composeTestRule.onNodeWithTag("lottie_animation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Check the remaining battery level of your AirPods.").assertExists()

        composeTestRule.onRoot().performTouchInput { swipeLeft() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("lottie_animation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Check the remaining battery level of your AirPods.").assertExists()
        composeTestRule.onNodeWithText("Next").assertIsDisplayed()
    }
}
