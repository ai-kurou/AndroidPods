package kurou.androidpods.navigation

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.os.Build
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
@Config(sdk = [35], application = HiltTestApplication::class)
class AppScaffoldTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val shadowApp = Shadows.shadowOf(app)
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        else
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.forEach { shadowApp.grantPermissions(it) }

        Shadows.shadowOf(BluetoothAdapter.getDefaultAdapter()).setEnabled(true)
        ShadowSettings.setCanDrawOverlays(true)
    }

    @Test
    fun `ナビゲーションアイテムが表示される`() {
        composeTestRule.setContent {
            AppScaffold(
                isFirstLaunch = false,
                windowWidthSizeClass = WindowWidthSizeClass.Compact,
                onOnboardingComplete = {},
                onStartScanService = {},
                onStopScanService = {},
            )
        }

        TopLevelDestination.entries.forEach { destination ->
            composeTestRule
                .onNodeWithContentDescription(
                    composeTestRule.activity.getString(destination.labelResId),
                    useUnmergedTree = true,
                )
                .assertExists()
        }
    }

    @Test
    fun `初回起動時はオンボーディングが表示される`() {
        composeTestRule.setContent {
            AppScaffold(
                isFirstLaunch = true,
                windowWidthSizeClass = WindowWidthSizeClass.Compact,
                onOnboardingComplete = {},
                onStartScanService = {},
                onStopScanService = {},
            )
        }

        composeTestRule.onNodeWithText("Next").assertIsDisplayed()
    }

    @Test
    fun `オンボーディングを完了するとコールバックが呼ばれる`() {
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
        composeTestRule.onNodeWithText("Next").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next").performClick()
        composeTestRule.waitForIdle()
        // ページ2: 権限要求ボタンを押す
        composeTestRule.onNodeWithText("Grant Permission").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grant Permission").performClick()
        composeTestRule.waitForIdle()
        // ページ3: オーバーレイ権限ボタンを押す
        composeTestRule.onNodeWithText("Allow Overlay").assertIsDisplayed()
        composeTestRule.onNodeWithText("Allow Overlay").performClick()
        composeTestRule.waitForIdle()
        // ページ4: Bluetooth ONボタンを押す
        composeTestRule.onNodeWithText("Enable Bluetooth").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enable Bluetooth").performClick()
        composeTestRule.waitForIdle()

        assertTrue(completeCalled)
    }
}
