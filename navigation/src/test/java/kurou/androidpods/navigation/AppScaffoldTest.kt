package kurou.androidpods.navigation

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.os.Build
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-port-xxhdpi", application = HiltTestApplication::class)
class AppScaffoldTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val shadowApp = Shadows.shadowOf(app)
        val permissions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
            } else {
                listOf(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        permissions.forEach { shadowApp.grantPermissions(it) }

        Shadows.shadowOf(BluetoothAdapter.getDefaultAdapter()).setEnabled(true)
        ShadowSettings.setCanDrawOverlays(true)
    }

    @Test
    fun `初回起動時はオンボーディングが表示され、完了するとコールバックが呼ばれる`() {
        var completeCalled = false
        composeTestRule.setContent {
            AppScaffold(
                isFirstLaunch = true,
                windowWidthSizeClass = WindowWidthSizeClass.Compact,
                onOnboardingComplete = { completeCalled = true },
                onStartScanService = {},
                onStopScanService = {},
            )
        }

        // ページ1 → ページ2
        composeTestRule.onNodeWithText("Next").performClick()
        composeTestRule.waitForIdle()
        // ページ2: 権限要求ボタンを押す
        composeTestRule.onNodeWithText("Grant Permission").performClick()
        composeTestRule.waitForIdle()
        // ページ3: オーバーレイ権限ボタンを押す
        composeTestRule.onNodeWithText("Allow Overlay").performClick()
        composeTestRule.waitForIdle()
        // ページ4: Bluetooth ONボタンを押す
        composeTestRule.onNodeWithText("Enable Bluetooth").performClick()
        composeTestRule.waitForIdle()

        assertTrue(completeCalled)
    }

    @Test
    fun `初回起動以降はSettings画面が表示される`() {
        composeTestRule.setContent {
            AppScaffold(
                isFirstLaunch = false,
                windowWidthSizeClass = WindowWidthSizeClass.Compact,
                onOnboardingComplete = {},
                onStartScanService = {},
                onStopScanService = {},
            )
        }

        composeTestRule.onNodeWithText("AndroidPods").assertIsDisplayed()

        // SettingsScreen → DevicesScreen
        composeTestRule.onNodeWithTag("SettingsGrid").performScrollToNode(hasText("Compatible devices"))
        composeTestRule.onNodeWithText("Compatible devices").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Compatible devices").assertIsDisplayed()

        // DevicesScreen → SettingsScreen
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("AndroidPods").assertIsDisplayed()

        // SettingsScreen → LicensesScreen
        composeTestRule.onNodeWithTag("SettingsGrid").performScrollToNode(hasText("Open Source Licenses"))
        composeTestRule.onNodeWithText("Open Source Licenses").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Open Source Licenses").assertIsDisplayed()

        // LicensesScreen → SettingsScreen
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("AndroidPods").assertIsDisplayed()
    }
}
