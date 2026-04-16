package kurou.androidpods.feature.settings

import android.Manifest
import android.bluetooth.BluetoothAdapter
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-port-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsContentScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `通常状態_警告なし`() {
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = mapOf(
                    Manifest.permission.BLUETOOTH_CONNECT to true,
                    Manifest.permission.BLUETOOTH_SCAN to true,
                ),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = true,
                updateAvailable = false,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onOverlayToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun `Bluetooth非対応の警告が表示される`() {
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = null,
                overlayEnabled = false,
                updateAvailable = false,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onOverlayToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun `全警告が表示される`() {
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = mapOf(
                    Manifest.permission.BLUETOOTH_CONNECT to false,
                    Manifest.permission.BLUETOOTH_SCAN to false,
                ),
                bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
                overlayEnabled = false,
                updateAvailable = true,
                columns = 1,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onOverlayToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    @Config(qualifiers = "w640dp-h360dp-land-xxhdpi")
    fun `通常状態_警告なし_横向き`() {
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = mapOf(
                    Manifest.permission.BLUETOOTH_CONNECT to true,
                    Manifest.permission.BLUETOOTH_SCAN to true,
                ),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                overlayEnabled = true,
                updateAvailable = false,
                columns = 2,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onOverlayToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    @Config(qualifiers = "w640dp-h360dp-land-xxhdpi")
    fun `Bluetooth非対応の警告が表示される_横向き`() {
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = null,
                overlayEnabled = false,
                updateAvailable = false,
                columns = 2,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onOverlayToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    @Config(qualifiers = "w640dp-h360dp-land-xxhdpi")
    fun `全警告が表示される_横向き`() {
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = mapOf(
                    Manifest.permission.BLUETOOTH_CONNECT to false,
                    Manifest.permission.BLUETOOTH_SCAN to false,
                ),
                bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
                overlayEnabled = false,
                updateAvailable = true,
                columns = 2,
                onPermissionWarningClick = {},
                onBluetoothWarningClick = {},
                onOverlayToggle = {},
                onUpdateClick = {},
                onLicensesClick = {},
                onDevicesClick = {},
                onGithubClick = {},
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
