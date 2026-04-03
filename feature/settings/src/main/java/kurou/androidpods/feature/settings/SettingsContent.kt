package kurou.androidpods.feature.settings

import android.bluetooth.BluetoothAdapter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun SettingsContent(
    permissionStates: Map<String, Boolean>,
    bluetoothAdapterState: Int?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {
        val hasNotGranted = permissionStates.values.any { !it }
        if (hasNotGranted) {
            Text(
                text = stringResource(R.string.permission_warning),
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(12.dp),
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
        val adapterStateText = when (bluetoothAdapterState) {
            BluetoothAdapter.STATE_ON -> stringResource(R.string.bluetooth_state_on)
            BluetoothAdapter.STATE_OFF -> stringResource(R.string.bluetooth_state_off)
            BluetoothAdapter.STATE_TURNING_ON -> stringResource(R.string.bluetooth_state_turning_on)
            BluetoothAdapter.STATE_TURNING_OFF -> stringResource(R.string.bluetooth_state_turning_off)
            else -> stringResource(R.string.bluetooth_state_not_available)
        }
        Text(text = "${stringResource(R.string.bluetooth_adapter_state)}: $adapterStateText")
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
        }
    }
}

@Preview(showBackground = true, name = "全許可済み")
@Composable
private fun SettingsContentPreviewAllGranted() {
    SettingsContent(
        permissionStates = mapOf(
            android.Manifest.permission.BLUETOOTH_CONNECT to true,
            android.Manifest.permission.BLUETOOTH_SCAN to true,
        ),
        bluetoothAdapterState = BluetoothAdapter.STATE_ON,
    )
}

@Preview(showBackground = true, name = "未許可あり")
@Composable
private fun SettingsContentPreviewNotGranted() {
    SettingsContent(
        permissionStates = mapOf(
            android.Manifest.permission.BLUETOOTH_CONNECT to true,
            android.Manifest.permission.BLUETOOTH_SCAN to false,
        ),
        bluetoothAdapterState = BluetoothAdapter.STATE_ON,
    )
}

@Preview(showBackground = true, name = "API 30以下")
@Composable
private fun SettingsContentPreviewApi30() {
    SettingsContent(
        permissionStates = mapOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION to false,
        ),
        bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
    )
}
