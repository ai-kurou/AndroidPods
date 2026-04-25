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

    @Test
    fun `全て許可済みのとき権限警告が表示されない`() {
        composeTestRule.setContent {
            SettingsContent(
                permissionStates =
                    mapOf(
                        Manifest.permission.BLUETOOTH_CONNECT to true,
                        Manifest.permission.BLUETOOTH_SCAN to true,
                    ),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                updateAvailable = false,
                isNotificationsDisabled = false,
                isDeviceScanChannelDisabled = false,
                isServiceRestarting = false,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onNotificationWarningClick = {},
                onDeviceScanChannelWarningClick = {},
                onOverlayToggle = {},
                overlayPosition = OverlayPosition.BOTTOM,
                onOverlayPositionClick = {},
                onRestartServiceClick = {},
                themeSettings = ThemeSettings(),
                onThemeModeClick = {},
                onDynamicColorToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }

        composeTestRule
            .onNodeWithText(
                "Some required permissions are not granted. Please grant all permissions.",
            ).assertDoesNotExist()
    }

    @Test
    fun `未許可の権限があるとき権限警告が表示され、タップするとコールバックが呼ばれる`() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                permissionStates =
                    mapOf(
                        Manifest.permission.BLUETOOTH_CONNECT to true,
                        Manifest.permission.BLUETOOTH_SCAN to false,
                    ),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                updateAvailable = false,
                isNotificationsDisabled = false,
                isDeviceScanChannelDisabled = false,
                isServiceRestarting = false,
                columns = 1,
                onPermissionWarningClick = { clicked = true },
                onBluetoothWarningClick = {},
                onNotificationWarningClick = {},
                onDeviceScanChannelWarningClick = {},
                onOverlayToggle = {},
                overlayPosition = OverlayPosition.BOTTOM,
                onOverlayPositionClick = {},
                onRestartServiceClick = {},
                themeSettings = ThemeSettings(),
                onThemeModeClick = {},
                onDynamicColorToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }

        composeTestRule
            .onNodeWithText(
                "Some required permissions are not granted. Please grant all permissions.",
            ).performClick()

        assertTrue(clicked)
    }

    @Test
    fun `通知が無効のとき通知無効警告が表示され、タップするとコールバックが呼ばれる`() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                updateAvailable = false,
                isNotificationsDisabled = true,
                isDeviceScanChannelDisabled = false,
                isServiceRestarting = false,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onNotificationWarningClick = { clicked = true },
                onDeviceScanChannelWarningClick = {},
                onOverlayToggle = {},
                overlayPosition = OverlayPosition.BOTTOM,
                onOverlayPositionClick = {},
                onRestartServiceClick = {},
                themeSettings = ThemeSettings(),
                onThemeModeClick = {},
                onDynamicColorToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }

        composeTestRule
            .onNodeWithText("App notifications are disabled. Tap to open notification settings.")
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun `BluetoothがOFFのときBluetooth警告が表示され、タップするとコールバックが呼ばれる`() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
                overlayEnabled = false,
                updateAvailable = false,
                isNotificationsDisabled = false,
                isDeviceScanChannelDisabled = false,
                isServiceRestarting = false,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = { clicked = true },
                onNotificationWarningClick = {},
                onDeviceScanChannelWarningClick = {},
                onOverlayToggle = {},
                overlayPosition = OverlayPosition.BOTTOM,
                onOverlayPositionClick = {},
                onRestartServiceClick = {},
                themeSettings = ThemeSettings(),
                onThemeModeClick = {},
                onDynamicColorToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }

        composeTestRule
            .onNodeWithText(
                "Bluetooth is off. Please enable Bluetooth.",
            ).performClick()

        assertTrue(clicked)
    }

    @Test
    fun `対応デバイスアイテムをタップするとonDevicesClickが呼ばれる`() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                updateAvailable = false,
                isNotificationsDisabled = false,
                isDeviceScanChannelDisabled = false,
                isServiceRestarting = false,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onNotificationWarningClick = {},
                onDeviceScanChannelWarningClick = {},
                onOverlayToggle = {},
                overlayPosition = OverlayPosition.BOTTOM,
                onOverlayPositionClick = {},
                onRestartServiceClick = {},
                themeSettings = ThemeSettings(),
                onThemeModeClick = {},
                onDynamicColorToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = { clicked = true },
                onGithubClick = {},
            )
        }

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Compatible devices"))
        composeTestRule.onNodeWithText("Compatible devices").performClick()

        assertTrue(clicked)
    }

    @Test
    fun `ライセンスアイテムをタップするとonLicensesClickが呼ばれる`() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                updateAvailable = false,
                isNotificationsDisabled = false,
                isDeviceScanChannelDisabled = false,
                isServiceRestarting = false,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onNotificationWarningClick = {},
                onDeviceScanChannelWarningClick = {},
                onOverlayToggle = {},
                overlayPosition = OverlayPosition.BOTTOM,
                onOverlayPositionClick = {},
                onRestartServiceClick = {},
                themeSettings = ThemeSettings(),
                onThemeModeClick = {},
                onDynamicColorToggle = {},
                onUpdateClick = {},
                onLicensesClick = { clicked = true },
                onDevicesClick = {},
                onGithubClick = {},
            )
        }

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Open Source Licenses"))
        composeTestRule.onNodeWithText("Open Source Licenses").performClick()

        assertTrue(clicked)
    }

    @Test
    fun `GithubレポジトリアイテムをタップするとonGithubClickが呼ばれる`() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                updateAvailable = false,
                isNotificationsDisabled = false,
                isDeviceScanChannelDisabled = false,
                isServiceRestarting = false,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onNotificationWarningClick = {},
                onDeviceScanChannelWarningClick = {},
                onOverlayToggle = {},
                overlayPosition = OverlayPosition.BOTTOM,
                onOverlayPositionClick = {},
                onRestartServiceClick = {},
                themeSettings = ThemeSettings(),
                onThemeModeClick = {},
                onDynamicColorToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = { clicked = true },
            )
        }

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("GitHub Repository"))
        composeTestRule.onNodeWithText("GitHub Repository").performClick()

        assertTrue(clicked)
    }

    @Test
    fun `オーバーレイがオフのときアイテムをタップするとtrueでonOverlayToggleが呼ばれる`() {
        var toggledValue: Boolean? = null
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                updateAvailable = false,
                isNotificationsDisabled = false,
                isDeviceScanChannelDisabled = false,
                isServiceRestarting = false,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onNotificationWarningClick = {},
                onDeviceScanChannelWarningClick = {},
                onOverlayToggle = { toggledValue = it },
                overlayPosition = OverlayPosition.BOTTOM,
                onOverlayPositionClick = {},
                onRestartServiceClick = {},
                themeSettings = ThemeSettings(),
                onThemeModeClick = {},
                onDynamicColorToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Show battery overlay"))
        composeTestRule.onNodeWithText("Show battery overlay").performClick()

        assertTrue(toggledValue == true)
    }

    @Test
    fun `オーバーレイがオンのときアイテムをタップするとfalseでonOverlayToggleが呼ばれる`() {
        var toggledValue: Boolean? = null
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = true,
                updateAvailable = false,
                isNotificationsDisabled = false,
                isDeviceScanChannelDisabled = false,
                isServiceRestarting = false,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onNotificationWarningClick = {},
                onDeviceScanChannelWarningClick = {},
                onOverlayToggle = { toggledValue = it },
                overlayPosition = OverlayPosition.BOTTOM,
                onOverlayPositionClick = {},
                onRestartServiceClick = {},
                themeSettings = ThemeSettings(),
                onThemeModeClick = {},
                onDynamicColorToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Show battery overlay"))
        composeTestRule.onNodeWithText("Show battery overlay").performClick()

        assertTrue(toggledValue == false)
    }

    @Test
    fun `再起動アイテムをタップするとonRestartServiceClickが呼ばれる`() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                updateAvailable = false,
                isNotificationsDisabled = false,
                isDeviceScanChannelDisabled = false,
                isServiceRestarting = false,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onNotificationWarningClick = {},
                onDeviceScanChannelWarningClick = {},
                onOverlayToggle = {},
                overlayPosition = OverlayPosition.BOTTOM,
                onOverlayPositionClick = {},
                onRestartServiceClick = { clicked = true },
                themeSettings = ThemeSettings(),
                onThemeModeClick = {},
                onDynamicColorToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Restart scan service"))
        composeTestRule.onNodeWithText("Restart scan service").performClick()

        assertTrue(clicked)
    }

    @Test
    fun `isServiceRestartingがtrueのとき再起動アイテムをタップしてもコールバックが呼ばれない`() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                updateAvailable = false,
                isNotificationsDisabled = false,
                isDeviceScanChannelDisabled = false,
                isServiceRestarting = true,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onNotificationWarningClick = {},
                onDeviceScanChannelWarningClick = {},
                onOverlayToggle = {},
                overlayPosition = OverlayPosition.BOTTOM,
                onOverlayPositionClick = {},
                onRestartServiceClick = { clicked = true },
                themeSettings = ThemeSettings(),
                onThemeModeClick = {},
                onDynamicColorToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Restart scan service"))
        composeTestRule.onNodeWithText("Restart scan service").performClick()

        assertTrue(!clicked)
    }

    @Test
    fun `アップデートバナーをタップするとonUpdateClickが呼ばれる`() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                updateAvailable = true,
                isNotificationsDisabled = false,
                isDeviceScanChannelDisabled = false,
                isServiceRestarting = false,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onNotificationWarningClick = {},
                onDeviceScanChannelWarningClick = {},
                onOverlayToggle = {},
                overlayPosition = OverlayPosition.BOTTOM,
                onOverlayPositionClick = {},
                onRestartServiceClick = {},
                themeSettings = ThemeSettings(),
                onThemeModeClick = {},
                onDynamicColorToggle = {},
                onUpdateClick = { clicked = true },
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }

        composeTestRule
            .onNodeWithText(
                "A new version is available. Tap to update.",
            ).performClick()

        assertTrue(clicked)
    }

    @Test
    fun `テーマアイテムをタップするとonThemeModeClickが呼ばれる`() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                updateAvailable = false,
                isNotificationsDisabled = false,
                isDeviceScanChannelDisabled = false,
                isServiceRestarting = false,
                columns = 1,
                themeSettings = ThemeSettings(),
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onNotificationWarningClick = {},
                onDeviceScanChannelWarningClick = {},
                onOverlayToggle = {},
                overlayPosition = OverlayPosition.BOTTOM,
                onOverlayPositionClick = {},
                onRestartServiceClick = {},
                onThemeModeClick = { clicked = true },
                onDynamicColorToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Theme"))
        composeTestRule.onNodeWithText("Theme").performClick()

        assertTrue(clicked)
    }

    @Test
    fun `ダイナミックカラーがオフのときタップするとtrueでonDynamicColorToggleが呼ばれる`() {
        var toggledValue: Boolean? = null
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                updateAvailable = false,
                isNotificationsDisabled = false,
                isDeviceScanChannelDisabled = false,
                isServiceRestarting = false,
                columns = 1,
                themeSettings = ThemeSettings(useDynamicColor = false),
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onNotificationWarningClick = {},
                onDeviceScanChannelWarningClick = {},
                onOverlayToggle = {},
                overlayPosition = OverlayPosition.BOTTOM,
                onOverlayPositionClick = {},
                onRestartServiceClick = {},
                onThemeModeClick = {},
                onDynamicColorToggle = { toggledValue = it },
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Dynamic Color"))
        composeTestRule.onNodeWithText("Dynamic Color").performClick()

        assertEquals(true, toggledValue)
    }

    @Test
    fun `ダイナミックカラーがオンのときタップするとfalseでonDynamicColorToggleが呼ばれる`() {
        var toggledValue: Boolean? = null
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                updateAvailable = false,
                isNotificationsDisabled = false,
                isDeviceScanChannelDisabled = false,
                isServiceRestarting = false,
                columns = 1,
                themeSettings = ThemeSettings(useDynamicColor = true),
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onNotificationWarningClick = {},
                onDeviceScanChannelWarningClick = {},
                onOverlayToggle = {},
                overlayPosition = OverlayPosition.BOTTOM,
                onOverlayPositionClick = {},
                onRestartServiceClick = {},
                onThemeModeClick = {},
                onDynamicColorToggle = { toggledValue = it },
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Dynamic Color"))
        composeTestRule.onNodeWithText("Dynamic Color").performClick()

        assertEquals(false, toggledValue)
    }

    @Test
    @Config(sdk = [30])
    fun `Android12未満ではダイナミックカラーアイテムが表示されない`() {
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                updateAvailable = false,
                isNotificationsDisabled = false,
                isDeviceScanChannelDisabled = false,
                isServiceRestarting = false,
                columns = 1,
                themeSettings = ThemeSettings(),
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onNotificationWarningClick = {},
                onDeviceScanChannelWarningClick = {},
                onOverlayToggle = {},
                overlayPosition = OverlayPosition.BOTTOM,
                onOverlayPositionClick = {},
                onRestartServiceClick = {},
                onThemeModeClick = {},
                onDynamicColorToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }

        composeTestRule.onNodeWithText("Dynamic Color").assertDoesNotExist()
    }

    @Test
    fun `オーバーレイ位置アイテムをタップするとonOverlayPositionClickが呼ばれる`() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                overlayPosition = OverlayPosition.BOTTOM,
                updateAvailable = false,
                isNotificationsDisabled = false,
                isDeviceScanChannelDisabled = false,
                isServiceRestarting = false,
                columns = 1,
                themeSettings = ThemeSettings(),
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onNotificationWarningClick = {},
                onDeviceScanChannelWarningClick = {},
                onOverlayToggle = {},
                onOverlayPositionClick = { clicked = true },
                onRestartServiceClick = {},
                onThemeModeClick = {},
                onDynamicColorToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }

        composeTestRule.onNodeWithText("Overlay position").performClick()

        assertTrue(clicked)
    }
}
