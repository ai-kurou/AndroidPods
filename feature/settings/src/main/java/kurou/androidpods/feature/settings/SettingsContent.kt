package kurou.androidpods.feature.settings

import android.bluetooth.BluetoothAdapter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
    Column(modifier = modifier.padding(16.dp)) {
        val hasNotGranted = permissionStates.values.any { !it }
        if (hasNotGranted) {
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
        val isBluetoothUnavailable = bluetoothAdapterState == null
        val isBluetoothOff = bluetoothAdapterState != null && bluetoothAdapterState != BluetoothAdapter.STATE_ON
        if (isBluetoothUnavailable || isBluetoothOff) {
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
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
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
        bluetoothAdapterState = null,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
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
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
    )
}
