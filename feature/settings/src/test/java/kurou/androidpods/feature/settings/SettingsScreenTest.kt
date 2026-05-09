package kurou.androidpods.feature.settings

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kurou.androidpods.core.domain.CheckUpdateUseCase
import kurou.androidpods.core.domain.GetAppleDevicesUseCase
import kurou.androidpods.core.domain.GetBluetoothAdapterStateUseCase
import kurou.androidpods.core.domain.GetOverlaySettingsUseCase
import kurou.androidpods.core.domain.NotificationChannels
import kurou.androidpods.core.domain.OverlayPosition
import kurou.androidpods.core.domain.OverlayPositionUseCase
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
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val btUseCase = mockk<GetBluetoothAdapterStateUseCase>()
    private val appleDevicesUseCase = mockk<GetAppleDevicesUseCase>(relaxUnitFun = true)
    private val overlayUseCase = mockk<GetOverlaySettingsUseCase>(relaxUnitFun = true)
    private val checkUpdateUseCase = mockk<CheckUpdateUseCase>()
    private val themeSettingsUseCase = mockk<ThemeSettingsUseCase>()
    private val overlayPositionUseCase = mockk<OverlayPositionUseCase>(relaxUnitFun = true)

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun windowSizeClassOf(widthDp: Float) =
        WindowSizeClass.BREAKPOINTS_V1.computeWindowSizeClass(widthDp = widthDp, heightDp = 800f)

    private fun grantRequiredPermissions(context: Context) {
        shadowOf(context as Application).grantPermissions(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )
    }

    private fun createViewModel(bluetoothAdapterState: Int): SettingsViewModel {
        every { btUseCase.observe() } returns MutableStateFlow(bluetoothAdapterState)
        every { appleDevicesUseCase.observe() } returns MutableStateFlow(emptyMap())
        every { overlayUseCase.observe() } returns MutableStateFlow(false)
        every { themeSettingsUseCase.observe() } returns MutableStateFlow(ThemeSettings())
        every { overlayPositionUseCase.observe() } returns MutableStateFlow(OverlayPosition.BOTTOM)
        coEvery { themeSettingsUseCase.update(any()) } just Runs
        return SettingsViewModel(
            btUseCase,
            appleDevicesUseCase,
            overlayUseCase,
            checkUpdateUseCase,
            themeSettingsUseCase,
            overlayPositionUseCase,
        )
    }

    @Test
    fun `Compact・Medium・ExpandedのwindowWidthSizeClassでSettingsScreenが表示される`() {
        var windowSizeClass by mutableStateOf(windowSizeClassOf(400f))

        composeTestRule.setContent {
            SettingsScreen(
                windowSizeClass = windowSizeClass,
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = createViewModel(BluetoothAdapter.STATE_ON),
            )
        }

        listOf(
            windowSizeClassOf(400f), // Compact
            windowSizeClassOf(700f), // Medium
            windowSizeClassOf(1000f), // Expanded
        ).forEach { sizeClass ->
            windowSizeClass = sizeClass
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("AndroidPods").assertIsDisplayed()
        }
    }

    @Test
    fun `権限警告をタップするとACTION_APPLICATION_DETAILS_SETTINGSのインテントが発行される`() {
        composeTestRule.setContent {
            SettingsScreen(
                windowSizeClass = windowSizeClassOf(400f),
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = createViewModel(BluetoothAdapter.STATE_ON),
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(
                "Some required permissions are not granted. Please grant all permissions.",
            ).performClick()
        composeTestRule.waitForIdle()

        val started = shadowOf(composeTestRule.activity).nextStartedActivity
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, started?.action)
    }

    @Test
    fun `権限リクエスト後に権限が拒否されるとPermissionRequiredDialogが表示される`() {
        composeTestRule.setContent {
            SettingsScreen(
                windowSizeClass = windowSizeClassOf(400f),
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = createViewModel(BluetoothAdapter.STATE_ON),
            )
        }
        composeTestRule.waitForIdle()

        // RequestMultiplePermissionsランチャーのコールバックを手動で発火してinitialRequestDone=trueにする
        composeTestRule.activityRule.scenario.onActivity { activity ->
            val request = shadowOf(activity).lastRequestedPermission ?: return@onActivity
            activity.onRequestPermissionsResult(
                request.requestCode,
                request.requestedPermissions,
                IntArray(request.requestedPermissions.size) { PackageManager.PERMISSION_DENIED },
            )
        }
        composeTestRule.waitForIdle()

        // ON_RESUMEを再トリガーしてonShowSettingsDialogが呼ばれるようにする
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Bluetooth Permission Required").assertExists()

        composeTestRule.onNodeWithText("Open Settings").performClick()
        composeTestRule.waitForIdle()

        val started = shadowOf(composeTestRule.activity).nextStartedActivity
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, started?.action)
    }

    @Test
    fun `通知無効警告をタップするとACTION_APP_NOTIFICATION_SETTINGSのインテントが発行される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)
        val viewModel = createViewModel(BluetoothAdapter.STATE_ON)

        composeTestRule.setContent {
            SettingsScreen(
                windowSizeClass = windowSizeClassOf(400f),
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = viewModel,
            )
        }
        viewModel.refreshNotificationState(isDisabled = true)
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(hasScrollAction()).onFirst()
            .performScrollToNode(hasText("App notifications are disabled. Tap to open notification settings."))
        composeTestRule
            .onNodeWithText(
                "App notifications are disabled. Tap to open notification settings.",
            ).performClick()
        composeTestRule.waitForIdle()

        val started = shadowOf(composeTestRule.activity).nextStartedActivity
        assertEquals(Settings.ACTION_APP_NOTIFICATION_SETTINGS, started?.action)
        assertEquals(composeTestRule.activity.packageName, started?.getStringExtra(Settings.EXTRA_APP_PACKAGE))
    }

    @Test
    fun `DeviceScanチャンネル無効警告をタップするとACTION_CHANNEL_NOTIFICATION_SETTINGSのインテントが発行される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)
        val viewModel = createViewModel(BluetoothAdapter.STATE_ON)

        composeTestRule.setContent {
            SettingsScreen(
                windowSizeClass = windowSizeClassOf(400f),
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = viewModel,
            )
        }
        viewModel.refreshDeviceScanChannelState(isDisabled = true)
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(hasScrollAction()).onFirst()
            .performScrollToNode(hasText("Device Scan notifications are disabled. Tap to open notification settings."))
        composeTestRule
            .onNodeWithText(
                "Device Scan notifications are disabled. Tap to open notification settings.",
            ).performClick()
        composeTestRule.waitForIdle()

        val started = shadowOf(composeTestRule.activity).nextStartedActivity
        assertEquals(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS, started?.action)
        assertEquals(composeTestRule.activity.packageName, started?.getStringExtra(Settings.EXTRA_APP_PACKAGE))
        assertEquals(NotificationChannels.DEVICE_SCAN, started?.getStringExtra(Settings.EXTRA_CHANNEL_ID))
    }

    @Test
    fun `Bluetooth警告をタップするとACTION_BLUETOOTH_SETTINGSのインテントが発行される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)

        composeTestRule.setContent {
            SettingsScreen(
                windowSizeClass = windowSizeClassOf(400f),
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = createViewModel(bluetoothAdapterState = BluetoothAdapter.STATE_OFF),
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(
                "Bluetooth is off. Please enable Bluetooth.",
            ).performClick()
        composeTestRule.waitForIdle()

        val started = shadowOf(composeTestRule.activity).nextStartedActivity
        assertEquals(Settings.ACTION_BLUETOOTH_SETTINGS, started?.action)
    }

    @Test
    fun `オーバーレイトグルをタップするとACTION_MANAGE_OVERLAY_PERMISSIONのインテントが発行される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)

        composeTestRule.setContent {
            SettingsScreen(
                windowSizeClass = windowSizeClassOf(400f),
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
    fun `オーバーレイ位置アイテムをタップするとダイアログが表示される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)

        composeTestRule.setContent {
            SettingsScreen(
                windowSizeClass = windowSizeClassOf(400f),
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = createViewModel(BluetoothAdapter.STATE_ON),
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Overlay position"))
        composeTestRule.onNodeWithText("Overlay position").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Top").assertExists()

        composeTestRule.onNodeWithText("Top").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Top").assertDoesNotExist()
    }

    @Test
    fun `再起動アイテムをタップするとSnackbarが表示される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)

        composeTestRule.setContent {
            SettingsScreen(
                windowSizeClass = windowSizeClassOf(400f),
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = createViewModel(BluetoothAdapter.STATE_ON),
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Restart scan service").performClick()
        shadowOf(Looper.getMainLooper()).idleFor(5001, TimeUnit.MILLISECONDS)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Scan service restarted").assertIsDisplayed()
    }

    @Test
    fun `テーマアイテムをタップするとダイアログが表示される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)

        composeTestRule.setContent {
            SettingsScreen(
                windowSizeClass = windowSizeClassOf(400f),
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
                windowSizeClass = windowSizeClassOf(400f),
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

        composeTestRule.onNodeWithText("Light").assertDoesNotExist()
    }

    @Test
    fun `ダイナミックカラートグルをタップするとuseDynamicColorが更新される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)

        composeTestRule.setContent {
            SettingsScreen(
                windowSizeClass = windowSizeClassOf(400f),
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = createViewModel(BluetoothAdapter.STATE_ON),
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Dynamic Color"))
        composeTestRule.onNodeWithText("Dynamic Color").performClick()
        composeTestRule.waitForIdle()

        coVerify { themeSettingsUseCase.update(ThemeSettings(useDynamicColor = false)) }
    }

    @Test
    fun `アップデートバナーをタップするとACTION_VIEWのインテントが発行される`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)
        val viewModel = createViewModel(BluetoothAdapter.STATE_ON)
        coEvery { checkUpdateUseCase(any()) } returns Result.success(true)

        composeTestRule.setContent {
            SettingsScreen(
                windowSizeClass = windowSizeClassOf(400f),
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = viewModel,
            )
        }
        viewModel.checkUpdate("0.0.0")
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(hasScrollAction()).onFirst()
            .performScrollToNode(hasText("A new version is available. Tap to update."))
        composeTestRule.onNodeWithText("A new version is available. Tap to update.").performClick()
        composeTestRule.waitForIdle()

        val started = shadowOf(composeTestRule.activity).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, started?.action)
    }

    @Test
    fun `バッテリー最適化未除外のときアイテムをタップするとACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONSのインテントが発行される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)
        val viewModel = createViewModel(BluetoothAdapter.STATE_ON)

        composeTestRule.setContent {
            SettingsScreen(
                windowSizeClass = windowSizeClassOf(400f),
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = viewModel,
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(hasScrollAction()).onFirst()
            .performScrollToNode(hasText("Disable battery optimization"))
        composeTestRule.onNodeWithText("Disable battery optimization").performClick()
        composeTestRule.waitForIdle()

        val started = shadowOf(composeTestRule.activity).nextStartedActivityForResult
        assertEquals(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, started?.intent?.action)
    }

    @Test
    fun `バッテリー最適化除外済みのときアイテムをタップするとACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGSのインテントが発行される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)
        val viewModel = createViewModel(BluetoothAdapter.STATE_ON)
        shadowOf(context.getSystemService(PowerManager::class.java))
            .setIgnoringBatteryOptimizations(context.packageName, true)

        composeTestRule.setContent {
            SettingsScreen(
                windowSizeClass = windowSizeClassOf(400f),
                onStartScanService = {},
                onStopScanService = {},
                onLicensesClick = {},
                onDevicesClick = {},
                viewModel = viewModel,
            )
        }
        viewModel.refreshBatteryOptimizationState(isExempt = true)
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(hasScrollAction()).onFirst()
            .performScrollToNode(hasText("Disable battery optimization"))
        composeTestRule.onNodeWithText("Disable battery optimization").performClick()
        composeTestRule.waitForIdle()

        val started = shadowOf(composeTestRule.activity).nextStartedActivity
        assertEquals(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, started?.action)
    }

    @Test
    fun `GitHubリポジトリをタップするとACTION_VIEWのインテントが発行される`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        grantRequiredPermissions(context)

        composeTestRule.setContent {
            SettingsScreen(
                windowSizeClass = windowSizeClassOf(400f),
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
}
