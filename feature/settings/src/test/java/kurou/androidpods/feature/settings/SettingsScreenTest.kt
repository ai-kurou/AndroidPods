package kurou.androidpods.feature.settings

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.CheckUpdateUseCase
import kurou.androidpods.core.domain.GetAppleDevicesUseCase
import kurou.androidpods.core.domain.GetBluetoothAdapterStateUseCase
import kurou.androidpods.core.domain.GetOverlaySettingsUseCase
import kurou.androidpods.core.domain.ThemeSettings
import kurou.androidpods.core.domain.ThemeSettingsUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val btUseCase = mockk<GetBluetoothAdapterStateUseCase>()
    private val appleDevicesUseCase = mockk<GetAppleDevicesUseCase>(relaxUnitFun = true)
    private val overlayUseCase = mockk<GetOverlaySettingsUseCase>()
    private val checkUpdateUseCase = mockk<CheckUpdateUseCase>()
    private val themeSettingsUseCase = mockk<ThemeSettingsUseCase>()

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun grantRequiredPermissions(context: Context) {
        shadowOf(context as Application).grantPermissions(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )
    }

    private fun createViewModel(bluetoothAdapterState: Int): SettingsViewModel {
        every { btUseCase.observe() } returns MutableStateFlow(bluetoothAdapterState)
        every { appleDevicesUseCase.observe() } returns MutableStateFlow<Map<String, AppleDevice>>(emptyMap())
        every { overlayUseCase.isEnabled() } returns false
        coEvery { checkUpdateUseCase(any()) } returns false
        every { themeSettingsUseCase.observe() } returns MutableStateFlow(ThemeSettings())
        coEvery { themeSettingsUseCase.update(any()) } just Runs
        return SettingsViewModel(btUseCase, appleDevicesUseCase, overlayUseCase, checkUpdateUseCase, themeSettingsUseCase)
    }

    @Test
    fun `Compact・Medium・ExpandedのwindowWidthSizeClassでSettingsScreenが表示される`() {
        var widthSizeClass by mutableStateOf(WindowWidthSizeClass.Compact)

        composeTestRule.setContent {
            SettingsScreen(
                windowWidthSizeClass = widthSizeClass,
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = createViewModel(BluetoothAdapter.STATE_ON),
            )
        }

        listOf(
            WindowWidthSizeClass.Compact,
            WindowWidthSizeClass.Medium,
            WindowWidthSizeClass.Expanded,
        ).forEach { sizeClass ->
            widthSizeClass = sizeClass
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("AndroidPods").assertIsDisplayed()
        }
    }

    @Test
    fun `権限警告をタップするとACTION_APPLICATION_DETAILS_SETTINGSのインテントが発行される`() {
        composeTestRule.setContent {
            SettingsScreen(
                windowWidthSizeClass = WindowWidthSizeClass.Compact,
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = createViewModel(BluetoothAdapter.STATE_ON),
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(
            "Some required permissions are not granted. Please grant all permissions."
        ).performClick()
        composeTestRule.waitForIdle()

        val started = shadowOf(composeTestRule.activity).nextStartedActivity
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, started?.action)
    }

    @Test
    fun `Bluetooth警告をタップするとACTION_BLUETOOTH_SETTINGSのインテントが発行される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)

        composeTestRule.setContent {
            SettingsScreen(
                windowWidthSizeClass = WindowWidthSizeClass.Compact,
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = createViewModel(bluetoothAdapterState = BluetoothAdapter.STATE_OFF),
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(
            "Bluetooth is off. Please enable Bluetooth."
        ).performClick()
        composeTestRule.waitForIdle()

        val started = shadowOf(composeTestRule.activity).nextStartedActivity
        assertEquals(Settings.ACTION_BLUETOOTH_SETTINGS, started?.action)
    }

    @Test
    fun `GitHubリポジトリをタップするとACTION_VIEWのインテントが発行される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)

        composeTestRule.setContent {
            SettingsScreen(
                windowWidthSizeClass = WindowWidthSizeClass.Compact,
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = createViewModel(BluetoothAdapter.STATE_ON),
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("GitHub Repository"))
        composeTestRule.onNodeWithText("GitHub Repository").performClick()
        composeTestRule.waitForIdle()

        val started = shadowOf(composeTestRule.activity).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, started?.action)
        assertEquals("https://github.com/ai-kurou/AndroidPods", started?.dataString)
    }

    @Test
    fun `オーバーレイトグルをタップするとACTION_MANAGE_OVERLAY_PERMISSIONのインテントが発行される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)

        composeTestRule.setContent {
            SettingsScreen(
                windowWidthSizeClass = WindowWidthSizeClass.Compact,
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = createViewModel(BluetoothAdapter.STATE_ON),
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Show battery overlay").performClick()
        composeTestRule.waitForIdle()

        val started = shadowOf(composeTestRule.activity).nextStartedActivityForResult
        assertEquals(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, started?.intent?.action)
    }

    @Test
    fun `再起動アイテムをタップするとSnackbarが表示される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)

        composeTestRule.setContent {
            SettingsScreen(
                windowWidthSizeClass = WindowWidthSizeClass.Compact,
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = createViewModel(BluetoothAdapter.STATE_ON),
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Restart scan service").performClick()
        composeTestRule.mainClock.advanceTimeBy(5001)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Scan service restarted").assertIsDisplayed()
    }

    @Test
    fun `テーマアイテムをタップするとダイアログが表示される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)

        composeTestRule.setContent {
            SettingsScreen(
                windowWidthSizeClass = WindowWidthSizeClass.Compact,
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = createViewModel(BluetoothAdapter.STATE_ON),
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Theme"))
        composeTestRule.onNodeWithText("Theme").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Light").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark").assertIsDisplayed()
    }

    @Test
    fun `ダイアログでモードを選択するとダイアログが閉じる`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)

        composeTestRule.setContent {
            SettingsScreen(
                windowWidthSizeClass = WindowWidthSizeClass.Compact,
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = createViewModel(BluetoothAdapter.STATE_ON),
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Theme"))
        composeTestRule.onNodeWithText("Theme").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Dark").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Light").assertIsNotDisplayed()
    }
}
