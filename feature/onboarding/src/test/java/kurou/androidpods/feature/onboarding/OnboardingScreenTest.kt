package kurou.androidpods.feature.onboarding

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30, 31])
class OnboardingScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun btAdapter(context: Context) = context.getSystemService(BluetoothManager::class.java).adapter

    private fun grantRequiredPermissions(context: Context) {
        val permissions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
            } else {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        shadowOf(context as Application).grantPermissions(*permissions)
    }

    @Test
    @Config(qualifiers = "port")
    fun `縦向きでボタンを押してページ1からページ4へ遷移してonCompleteが呼ばれる`() {
        assertNavigationAndComplete()
    }

    @Test
    @Config(qualifiers = "land")
    fun `横向きでボタンを押してページ1からページ4へ遷移してonCompleteが呼ばれる`() {
        assertNavigationAndComplete()
    }

    @Test
    @Config(qualifiers = "port")
    fun `縦向きでスワイプしてもページが遷移しない`() {
        assertSwipeDoesNotNavigate()
    }

    @Test
    @Config(qualifiers = "land")
    fun `横向きでスワイプしてもページが遷移しない`() {
        assertSwipeDoesNotNavigate()
    }

    @Test
    @Config(qualifiers = "port")
    fun `ページ4まで遷移して戻るボタンを4回押して画面が終了する`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        shadowOf(btAdapter(context)).setEnabled(true)
        grantRequiredPermissions(context)
        ShadowSettings.setCanDrawOverlays(true)

        composeTestRule.setContent {
            OnboardingScreen(onComplete = {})
        }

        composeTestRule.onNodeWithText("Next").performClick() // page 0 → 1
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Grant Permission").performClick() // page 1 → 2
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Allow Overlay").performClick() // page 2 → 3
        composeTestRule.waitForIdle()

        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed() // page 3 → 2
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Allow Overlay").assertIsDisplayed()

        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed() // page 2 → 1
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Grant Permission").assertIsDisplayed()

        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed() // page 1 → 0
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Next").assertIsDisplayed()

        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed() // page 0 → 終了
        }
        assertTrue(composeTestRule.activity.isFinishing)
    }

    @Test
    @Config(qualifiers = "port")
    fun `オーバーレイ権限がない状態でAllow Overlayボタンをクリックするとインテントが発行される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)
        ShadowSettings.setCanDrawOverlays(false)

        composeTestRule.setContent {
            OnboardingScreen(onComplete = {})
        }

        composeTestRule.onNodeWithText("Next").performClick() // page 0 → 1
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Grant Permission").performClick() // page 1 → 2
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Allow Overlay").performClick()
        composeTestRule.waitForIdle()

        val started = shadowOf(composeTestRule.activity).nextStartedActivityForResult
        assertEquals(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, started?.intent?.action)
    }

    @Test
    @Config(qualifiers = "port")
    fun `Bluetooth無効状態でEnable Bluetoothボタンをクリックするとインテントが発行される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Bluetooth は有効化しない（デフォルトで無効）
        grantRequiredPermissions(context)
        ShadowSettings.setCanDrawOverlays(true)

        composeTestRule.setContent {
            OnboardingScreen(onComplete = {})
        }

        composeTestRule.onNodeWithText("Next").performClick() // page 0 → 1
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Grant Permission").performClick() // page 1 → 2
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Allow Overlay").performClick() // page 2 → 3
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Enable Bluetooth").performClick()
        composeTestRule.waitForIdle()

        val started = shadowOf(composeTestRule.activity).nextStartedActivityForResult
        assertEquals(BluetoothAdapter.ACTION_REQUEST_ENABLE, started?.intent?.action)
    }

    private fun assertNavigationAndComplete() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        shadowOf(btAdapter(context)).setEnabled(true)
        grantRequiredPermissions(context)
        ShadowSettings.setCanDrawOverlays(true)

        var completed = false
        composeTestRule.setContent {
            OnboardingScreen(onComplete = { completed = true })
        }

        composeTestRule.onNodeWithTag("lottie_animation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Check the remaining battery level of your AirPods.").assertExists()

        composeTestRule.onNodeWithText("Next").performClick() // page 0 → 1
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("lottie_animation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Location permission is required to use Bluetooth.").assertExists()

        composeTestRule.onNodeWithText("Grant Permission").performClick() // page 1 → 2
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("lottie_animation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Allow overlay display to show battery info on screen.").assertExists()

        composeTestRule.onNodeWithText("Allow Overlay").performClick() // page 2 → 3
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("lottie_animation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please enable Bluetooth to connect to your AirPods.").assertExists()
        composeTestRule.onNodeWithText("Enable Bluetooth").assertIsDisplayed()

        composeTestRule.onNodeWithText("Enable Bluetooth").performClick() // onComplete
        assertTrue(completed)
    }

    private fun assertSwipeDoesNotNavigate() {
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
