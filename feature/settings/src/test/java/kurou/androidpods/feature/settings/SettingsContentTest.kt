package kurou.androidpods.feature.settings

import android.Manifest
import android.bluetooth.BluetoothAdapter
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import kurou.androidpods.core.domain.OverlayPosition
import kurou.androidpods.core.domain.ThemeSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SettingsContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Suppress("LongParameterList")
    private fun setSettingsContent(
        permissionStates: Map<String, Boolean> = emptyMap(),
        bluetoothAdapterState: Int = BluetoothAdapter.STATE_ON,
        overlayEnabled: Boolean = false,
        overlayPosition: OverlayPosition = OverlayPosition.BOTTOM,
        updateAvailable: Boolean = false,
        isNotificationsDisabled: Boolean = false,
        isDeviceScanChannelDisabled: Boolean = false,
        isServiceRestarting: Boolean = false,
        isBatteryOptimizationExempt: Boolean = false,
        hasUnknownDevices: Boolean = false,
        columns: Int = 1,
        themeSettings: ThemeSettings = ThemeSettings(),
        onPermissionWarningClick: () -> Unit = {},
        onBluetoothWarningClick: () -> Unit = {},
        onNotificationWarningClick: () -> Unit = {},
        onDeviceScanChannelWarningClick: () -> Unit = {},
        onOverlayToggle: (Boolean) -> Unit = {},
        onOverlayPositionClick: () -> Unit = {},
        onRestartServiceClick: () -> Unit = {},
        onBatteryOptimizationClick: () -> Unit = {},
        onThemeModeClick: () -> Unit = {},
        onDynamicColorToggle: (Boolean) -> Unit = {},
        onUpdateClick: () -> Unit = {},
        onUnknownDevicesClick: () -> Unit = {},
        onLicensesClick: () -> Unit = {},
        onDevicesClick: () -> Unit = {},
        onGithubClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = permissionStates,
                bluetoothAdapterState = bluetoothAdapterState,
                overlayEnabled = overlayEnabled,
                overlayPosition = overlayPosition,
                updateAvailable = updateAvailable,
                isNotificationsDisabled = isNotificationsDisabled,
                isDeviceScanChannelDisabled = isDeviceScanChannelDisabled,
                isServiceRestarting = isServiceRestarting,
                isBatteryOptimizationExempt = isBatteryOptimizationExempt,
                hasUnknownDevices = hasUnknownDevices,
                columns = columns,
                themeSettings = themeSettings,
                onPermissionWarningClick = onPermissionWarningClick,
                onBluetoothWarningClick = onBluetoothWarningClick,
                onNotificationWarningClick = onNotificationWarningClick,
                onDeviceScanChannelWarningClick = onDeviceScanChannelWarningClick,
                onOverlayToggle = onOverlayToggle,
                onOverlayPositionClick = onOverlayPositionClick,
                onRestartServiceClick = onRestartServiceClick,
                onBatteryOptimizationClick = onBatteryOptimizationClick,
                onThemeModeClick = onThemeModeClick,
                onDynamicColorToggle = onDynamicColorToggle,
                onUpdateClick = onUpdateClick,
                onUnknownDevicesClick = onUnknownDevicesClick,
                onLicensesClick = onLicensesClick,
                onDevicesClick = onDevicesClick,
                onGithubClick = onGithubClick,
            )
        }
    }

    @Test
    fun `全て許可済みのとき権限警告が表示されない`() {
        setSettingsContent(
            permissionStates = mapOf(
                Manifest.permission.BLUETOOTH_CONNECT to true,
                Manifest.permission.BLUETOOTH_SCAN to true,
            ),
        )

        composeTestRule
            .onNodeWithText(
                "Some required permissions are not granted. Please grant all permissions.",
            ).assertDoesNotExist()
    }

    @Test
    fun `未許可の権限があるとき権限警告が表示され、タップするとコールバックが呼ばれる`() {
        var clicked = false
        setSettingsContent(
            permissionStates = mapOf(
                Manifest.permission.BLUETOOTH_CONNECT to true,
                Manifest.permission.BLUETOOTH_SCAN to false,
            ),
            onPermissionWarningClick = { clicked = true },
        )

        composeTestRule
            .onNodeWithText(
                "Some required permissions are not granted. Please grant all permissions.",
            ).performClick()

        assertTrue(clicked)
    }

    @Test
    fun `通知が無効のとき通知無効警告が表示され、タップするとコールバックが呼ばれる`() {
        var clicked = false
        setSettingsContent(
            isNotificationsDisabled = true,
            onNotificationWarningClick = { clicked = true },
        )

        composeTestRule
            .onNodeWithText("App notifications are disabled. Tap to open notification settings.")
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun `BluetoothがOFFのときBluetooth警告が表示され、タップするとコールバックが呼ばれる`() {
        var clicked = false
        setSettingsContent(
            bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
            onBluetoothWarningClick = { clicked = true },
        )

        composeTestRule
            .onNodeWithText(
                "Bluetooth is off. Please enable Bluetooth.",
            ).performClick()

        assertTrue(clicked)
    }

    @Test
    fun `アップデートバナーをタップするとonUpdateClickが呼ばれる`() {
        var clicked = false
        setSettingsContent(
            updateAvailable = true,
            onUpdateClick = { clicked = true },
        )

        composeTestRule
            .onNodeWithText(
                "A new version is available. Tap to update.",
            ).performClick()

        assertTrue(clicked)
    }

    @Test
    fun `オーバーレイがオフのときアイテムをタップするとtrueでonOverlayToggleが呼ばれる`() {
        var toggledValue: Boolean? = null
        setSettingsContent(
            overlayEnabled = false,
            onOverlayToggle = { toggledValue = it },
        )

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Show battery overlay"))
        composeTestRule.onNodeWithText("Show battery overlay").performClick()

        assertTrue(toggledValue == true)
    }

    @Test
    fun `オーバーレイがオンのときアイテムをタップするとfalseでonOverlayToggleが呼ばれる`() {
        var toggledValue: Boolean? = null
        setSettingsContent(
            overlayEnabled = true,
            onOverlayToggle = { toggledValue = it },
        )

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Show battery overlay"))
        composeTestRule.onNodeWithText("Show battery overlay").performClick()

        assertTrue(toggledValue == false)
    }

    @Test
    fun `オーバーレイ位置アイテムをタップするとonOverlayPositionClickが呼ばれる`() {
        var clicked = false
        setSettingsContent(onOverlayPositionClick = { clicked = true })

        composeTestRule.onNodeWithText("Overlay position").performClick()

        assertTrue(clicked)
    }

    @Test
    fun `再起動アイテムをタップするとonRestartServiceClickが呼ばれる`() {
        var clicked = false
        setSettingsContent(onRestartServiceClick = { clicked = true })

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Restart scan service"))
        composeTestRule.onNodeWithText("Restart scan service").performClick()

        assertTrue(clicked)
    }

    @Test
    fun `isServiceRestartingがtrueのとき再起動アイテムをタップしてもコールバックが呼ばれない`() {
        var clicked = false
        setSettingsContent(
            isServiceRestarting = true,
            onRestartServiceClick = { clicked = true },
        )

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Restart scan service"))
        composeTestRule.onNodeWithText("Restart scan service").performClick()

        assertTrue(!clicked)
    }

    @Test
    fun `バッテリー最適化アイテムをタップするとonBatteryOptimizationClickが呼ばれる`() {
        var clicked = false
        setSettingsContent(onBatteryOptimizationClick = { clicked = true })

        composeTestRule.onAllNodes(hasScrollAction()).onFirst()
            .performScrollToNode(hasText("Disable battery optimization"))
        composeTestRule.onNodeWithText("Disable battery optimization").performClick()

        assertTrue(clicked)
    }

    @Test
    fun `テーマアイテムをタップするとonThemeModeClickが呼ばれる`() {
        var clicked = false
        setSettingsContent(onThemeModeClick = { clicked = true })

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Theme"))
        composeTestRule.onNodeWithText("Theme").performClick()

        assertTrue(clicked)
    }

    @Test
    fun `ダイナミックカラーがオフのときタップするとtrueでonDynamicColorToggleが呼ばれる`() {
        var toggledValue: Boolean? = null
        setSettingsContent(
            themeSettings = ThemeSettings(useDynamicColor = false),
            onDynamicColorToggle = { toggledValue = it },
        )

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Dynamic Color"))
        composeTestRule.onNodeWithText("Dynamic Color").performClick()

        assertEquals(true, toggledValue)
    }

    @Test
    fun `ダイナミックカラーがオンのときタップするとfalseでonDynamicColorToggleが呼ばれる`() {
        var toggledValue: Boolean? = null
        setSettingsContent(
            themeSettings = ThemeSettings(useDynamicColor = true),
            onDynamicColorToggle = { toggledValue = it },
        )

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Dynamic Color"))
        composeTestRule.onNodeWithText("Dynamic Color").performClick()

        assertEquals(false, toggledValue)
    }

    @Test
    @Config(sdk = [30])
    fun `Android12未満ではダイナミックカラーアイテムが表示されない`() {
        setSettingsContent()

        composeTestRule.onNodeWithText("Dynamic Color").assertDoesNotExist()
    }

    @Test
    fun `対応デバイスアイテムをタップするとonDevicesClickが呼ばれる`() {
        var clicked = false
        setSettingsContent(onDevicesClick = { clicked = true })

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Compatible devices"))
        composeTestRule.onNodeWithText("Compatible devices").performClick()

        assertTrue(clicked)
    }

    @Test
    fun `ライセンスアイテムをタップするとonLicensesClickが呼ばれる`() {
        var clicked = false
        setSettingsContent(onLicensesClick = { clicked = true })

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Open Source Licenses"))
        composeTestRule.onNodeWithText("Open Source Licenses").performClick()

        assertTrue(clicked)
    }

    @Test
    fun `GithubレポジトリアイテムをタップするとonGithubClickが呼ばれる`() {
        var clicked = false
        setSettingsContent(onGithubClick = { clicked = true })

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("GitHub Repository"))
        composeTestRule.onNodeWithText("GitHub Repository").performClick()

        assertTrue(clicked)
    }
}
