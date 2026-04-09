package kurou.androidpods.feature.settings

import android.Manifest
import android.bluetooth.BluetoothAdapter
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
                permissionStates = mapOf(
                    Manifest.permission.BLUETOOTH_CONNECT to true,
                    Manifest.permission.BLUETOOTH_SCAN to true,
                ),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onOverlayToggle = {},
                onLicensesClick = {},
                onDevicesClick = {},
            )
        }

        composeTestRule.onNodeWithText(
            "Some required permissions are not granted. Please grant all permissions."
        ).assertDoesNotExist()
    }

    @Test
    fun `未許可の権限があるとき権限警告が表示され、タップするとコールバックが呼ばれる`() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = mapOf(
                    Manifest.permission.BLUETOOTH_CONNECT to true,
                    Manifest.permission.BLUETOOTH_SCAN to false,
                ),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                columns = 1,
                onPermissionWarningClick = { clicked = true },
                onBluetoothWarningClick = {},
                onOverlayToggle = {},
                onLicensesClick = {},
                onDevicesClick = {},
            )
        }

        composeTestRule.onNodeWithText(
            "Some required permissions are not granted. Please grant all permissions."
        ).performClick()

        assertTrue(clicked)
    }

    @Test
    fun `BluetoothがONのときBluetooth警告が表示されない`() {
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onOverlayToggle = {},
                onLicensesClick = {},
                onDevicesClick = {},
            )
        }

        composeTestRule.onNodeWithText(
            "Bluetooth is off. Please enable Bluetooth."
        ).assertDoesNotExist()
    }

    @Test
    fun `BluetoothがOFFのときBluetooth警告が表示され、タップするとコールバックが呼ばれる`() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
                overlayEnabled = false,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = { clicked = true },
                onOverlayToggle = {},
                onLicensesClick = {},
                onDevicesClick = {},
            )
        }

        composeTestRule.onNodeWithText(
            "Bluetooth is off. Please enable Bluetooth."
        ).performClick()

        assertTrue(clicked)
    }

    @Test
    fun `Bluetoothがnullのときbluetooth警告が表示されタップしてもコールバックが呼ばれない`() {
        var clicked = false
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = null,
                overlayEnabled = false,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = { clicked = true },
                onOverlayToggle = {},
                onLicensesClick = {},
                onDevicesClick = {},
            )
        }

        composeTestRule.onNodeWithText(
            "This device does not support Bluetooth."
        ).assertIsDisplayed().performClick()

        assertTrue(!clicked)
    }

    @Test
    fun `オーバーレイがオフのときアイテムをタップするとtrueでonOverlayToggleが呼ばれる`() {
        var toggledValue: Boolean? = null
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = false,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onOverlayToggle = { toggledValue = it },
                onLicensesClick = {},
                onDevicesClick = {},
            )
        }

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
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onOverlayToggle = { toggledValue = it },
                onLicensesClick = {},
                onDevicesClick = {},
            )
        }

        composeTestRule.onNodeWithText("Show battery overlay").performClick()

        assertTrue(toggledValue == false)
    }
}
