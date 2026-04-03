package kurou.androidpods.feature.settings

import android.Manifest
import android.bluetooth.BluetoothAdapter
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `BluetoothがONのとき「On」が表示される`() {
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
            )
        }

        composeTestRule.onNodeWithText("Bluetooth Adapter: On").assertIsDisplayed()
    }

    @Test
    fun `BluetoothがOFFのとき「Off」が表示される`() {
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
            )
        }

        composeTestRule.onNodeWithText("Bluetooth Adapter: Off").assertIsDisplayed()
    }

    @Test
    fun `Bluetoothがnullのとき「Not Available」が表示される`() {
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = null,
            )
        }

        composeTestRule.onNodeWithText("Bluetooth Adapter: Not Available").assertIsDisplayed()
    }

    @Test
    fun `全て許可済みのとき警告が表示されない`() {
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = mapOf(
                    Manifest.permission.BLUETOOTH_CONNECT to true,
                    Manifest.permission.BLUETOOTH_SCAN to true,
                ),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
            )
        }

        composeTestRule.onNodeWithText(
            "Some required permissions are not granted. Please grant all permissions."
        ).assertDoesNotExist()
    }

    @Test
    fun `未許可の権限があるとき警告が表示される`() {
        composeTestRule.setContent {
            SettingsContent(
                permissionStates = mapOf(
                    Manifest.permission.BLUETOOTH_CONNECT to true,
                    Manifest.permission.BLUETOOTH_SCAN to false,
                ),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
            )
        }

        composeTestRule.onNodeWithText(
            "Some required permissions are not granted. Please grant all permissions."
        ).assertIsDisplayed()
    }
}
