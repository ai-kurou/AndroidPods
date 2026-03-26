package kurou.androidpods.feature.devices

import android.bluetooth.BluetoothAdapter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurou.androidpods.core.domain.AppleDevice

@Composable
internal fun DevicesContent(
    permissionStates: Map<String, Boolean>,
    bluetoothAdapterState: Int?,
    appleDevices: List<AppleDevice>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {
        permissionStates.forEach { (permission, granted) ->
            val shortName = permission.substringAfterLast(".")
            val status = if (granted) stringResource(R.string.permission_granted) else stringResource(R.string.permission_not_granted)
            Text(text = "$shortName: $status")
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        val adapterStateText = when (bluetoothAdapterState) {
            BluetoothAdapter.STATE_ON -> stringResource(R.string.bluetooth_state_on)
            BluetoothAdapter.STATE_OFF -> stringResource(R.string.bluetooth_state_off)
            BluetoothAdapter.STATE_TURNING_ON -> stringResource(R.string.bluetooth_state_turning_on)
            BluetoothAdapter.STATE_TURNING_OFF -> stringResource(R.string.bluetooth_state_turning_off)
            else -> stringResource(R.string.bluetooth_state_not_available)
        }
        Text(text = "${stringResource(R.string.bluetooth_adapter_state)}: $adapterStateText")
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(text = stringResource(R.string.apple_devices_title))
        if (appleDevices.isEmpty()) {
            Text(text = stringResource(R.string.no_apple_devices))
        } else {
            appleDevices.forEach { device ->
                Text(text = "${device.modelName} (${device.address})")
                val batteryInfo = if (device.isSingle) {
                    "Battery: ${batteryText(device.leftBattery)}"
                } else {
                    "L: ${batteryText(device.leftBattery)} " +
                        "R: ${batteryText(device.rightBattery)} " +
                        "Case: ${batteryText(device.caseBattery)}"
                }
                Text(text = "RSSI: ${device.rssi} dBm / $batteryInfo")
            }
        }
    }
}

private fun batteryText(level: Int?): String = when {
    level == null -> "--"
    level >= 10 -> "100%"
    else -> "${level * 10 + 5}%"
}

@Preview(showBackground = true, name = "API 31+ (Android 12+)")
@Composable
private fun DevicesContentPreviewApi31() {
    DevicesContent(
        permissionStates = mapOf(
            android.Manifest.permission.BLUETOOTH_CONNECT to true,
            android.Manifest.permission.BLUETOOTH_SCAN to true,
        ),
        bluetoothAdapterState = BluetoothAdapter.STATE_ON,
        appleDevices = listOf(
            AppleDevice(
                address = "AA:BB:CC:DD:EE:FF",
                modelName = "AirPods Pro (2nd Gen)",
                modelCode = 0x1420,
                rssi = -45,
                leftBattery = 8,
                rightBattery = 9,
                caseBattery = 7,
                isSingle = false
            ),
            AppleDevice(
                address = "FF:EE:DD:CC:BB:AA",
                modelName = "Beats Flex",
                modelCode = 0x1020,
                rssi = -40,
                leftBattery = 6,
                rightBattery = null,
                caseBattery = null,
                isSingle = true
            )
        ),
    )
}

@Preview(showBackground = true, name = "API 30以下")
@Composable
private fun DevicesContentPreviewApi30() {
    DevicesContent(
        permissionStates = mapOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION to false,
        ),
        bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
        appleDevices = emptyList(),
    )
}
