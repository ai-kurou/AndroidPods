package kurou.androidpods.feature.settings

import android.bluetooth.BluetoothAdapter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun SettingsContent(
    permissionStates: Map<String, Boolean>,
    bluetoothAdapterState: Int?,
    onPermissionWarningClick: () -> Unit,
    onBluetoothWarningClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasNotGranted = permissionStates.values.any { !it }
    val isBluetoothUnavailable = bluetoothAdapterState == null
    val isBluetoothOff = bluetoothAdapterState != null && bluetoothAdapterState != BluetoothAdapter.STATE_ON

    LazyColumn(modifier = modifier.fillMaxSize().padding(16.dp)) {
        if (hasNotGranted) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .clickable(onClick = onPermissionWarningClick)
                        .padding(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.permission_warning),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        if (isBluetoothUnavailable || isBluetoothOff) {
            item {
                val backgroundColor = if (isBluetoothUnavailable) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
                val contentColor = if (isBluetoothUnavailable) {
                    MaterialTheme.colorScheme.onError
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor)
                        .then(
                            if (isBluetoothOff) Modifier.clickable(onClick = onBluetoothWarningClick)
                            else Modifier
                        )
                        .padding(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(
                            if (isBluetoothUnavailable) R.string.bluetooth_not_supported
                            else R.string.bluetooth_warning
                        ),
                        color = contentColor,
                        modifier = Modifier.weight(1f),
                    )
                    if (isBluetoothOff) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = contentColor,
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Preview(showBackground = true, name = "警告なし")
@Composable
private fun SettingsContentPreviewNoWarning() {
    SettingsContent(
        permissionStates = mapOf(
            android.Manifest.permission.BLUETOOTH_CONNECT to true,
            android.Manifest.permission.BLUETOOTH_SCAN to true,
        ),
        bluetoothAdapterState = BluetoothAdapter.STATE_ON,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
    )
}

@Preview(showBackground = true, name = "権限未許可")
@Composable
private fun SettingsContentPreviewPermissionNotGranted() {
    SettingsContent(
        permissionStates = mapOf(
            android.Manifest.permission.BLUETOOTH_CONNECT to true,
            android.Manifest.permission.BLUETOOTH_SCAN to false,
        ),
        bluetoothAdapterState = BluetoothAdapter.STATE_ON,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
    )
}

@Preview(showBackground = true, name = "Bluetoothオフ")
@Composable
private fun SettingsContentPreviewBluetoothOff() {
    SettingsContent(
        permissionStates = mapOf(
            android.Manifest.permission.BLUETOOTH_CONNECT to true,
            android.Manifest.permission.BLUETOOTH_SCAN to true,
        ),
        bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
    )
}

@Preview(showBackground = true, name = "Bluetooth非対応")
@Composable
private fun SettingsContentPreviewBluetoothUnavailable() {
    SettingsContent(
        permissionStates = emptyMap(),
        bluetoothAdapterState = null,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
    )
}

@Preview(showBackground = true, name = "権限未許可 + Bluetoothオフ")
@Composable
private fun SettingsContentPreviewAllWarnings() {
    SettingsContent(
        permissionStates = mapOf(
            android.Manifest.permission.BLUETOOTH_CONNECT to false,
            android.Manifest.permission.BLUETOOTH_SCAN to false,
        ),
        bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
    )
}
