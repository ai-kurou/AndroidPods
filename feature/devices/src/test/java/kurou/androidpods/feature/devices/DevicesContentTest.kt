package kurou.androidpods.feature.devices

import android.Manifest
import android.bluetooth.BluetoothAdapter
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.androidpods.core.domain.AppleDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DevicesContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `BluetoothがONのとき「On」が表示される`() {
        composeTestRule.setContent {
            DevicesContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                appleDevices = emptyList(),
            )
        }

        composeTestRule.onNodeWithText("Bluetooth Adapter: On").assertIsDisplayed()
    }

    @Test
    fun `BluetoothがOFFのとき「Off」が表示される`() {
        composeTestRule.setContent {
            DevicesContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
                appleDevices = emptyList(),
            )
        }

        composeTestRule.onNodeWithText("Bluetooth Adapter: Off").assertIsDisplayed()
    }

    @Test
    fun `Bluetoothがnullのとき「Not Available」が表示される`() {
        composeTestRule.setContent {
            DevicesContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = null,
                appleDevices = emptyList(),
            )
        }

        composeTestRule.onNodeWithText("Bluetooth Adapter: Not Available").assertIsDisplayed()
    }

    @Test
    fun `パーミッションの許可状態が表示される`() {
        composeTestRule.setContent {
            DevicesContent(
                permissionStates = mapOf(
                    Manifest.permission.BLUETOOTH_CONNECT to true,
                    Manifest.permission.BLUETOOTH_SCAN to false,
                ),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                appleDevices = emptyList(),
            )
        }

        composeTestRule.onNodeWithText("BLUETOOTH_CONNECT: Granted").assertIsDisplayed()
        composeTestRule.onNodeWithText("BLUETOOTH_SCAN: Not Granted").assertIsDisplayed()
    }

    @Test
    fun `Appleデバイスが表示される`() {
        composeTestRule.setContent {
            DevicesContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                appleDevices = listOf(
                    AppleDevice("AA:BB:CC:DD:EE:FF", "AirPods Pro (2nd Gen)", 0x1420, -45, 8, 9, 7),
                ),
            )
        }

        composeTestRule.onNodeWithText("Apple Devices").assertIsDisplayed()
        composeTestRule.onNodeWithText("AirPods Pro (2nd Gen) (AA:BB:CC:DD:EE:FF)").assertIsDisplayed()
        composeTestRule.onNodeWithText("RSSI: -45 dBm / L: 80% R: 90% Case: 70%").assertIsDisplayed()
    }

    @Test
    fun `Appleデバイスが空のとき案内メッセージが表示される`() {
        composeTestRule.setContent {
            DevicesContent(
                permissionStates = emptyMap(),
                bluetoothAdapterState = BluetoothAdapter.STATE_ON,
                appleDevices = emptyList(),
            )
        }

        composeTestRule.onNodeWithText("Apple Devices").assertIsDisplayed()
        composeTestRule.onNodeWithText("No Apple devices found").assertIsDisplayed()
    }
}
