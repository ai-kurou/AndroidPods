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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
    overlayEnabled: Boolean,
    columns: Int,
    onPermissionWarningClick: () -> Unit,
    onBluetoothWarningClick: () -> Unit,
    onOverlayToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasNotGranted = permissionStates.values.any { !it }
    val isBluetoothUnavailable = bluetoothAdapterState == null
    val isBluetoothOff = bluetoothAdapterState != null && bluetoothAdapterState != BluetoothAdapter.STATE_ON

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize().padding(16.dp),
    ) {
        if (hasNotGranted) {
            item(span = { GridItemSpan(maxLineSpan) }) {
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
                    Spacer(modifier = Modifier.width(8.dp))
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
            }
        }
        if (isBluetoothUnavailable || isBluetoothOff) {
            item(span = { GridItemSpan(maxLineSpan) }) {
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
                    Spacer(modifier = Modifier.width(8.dp))
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
            }
        }
        item(span = { GridItemSpan(1) }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.overlay_setting_label),
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = overlayEnabled,
                    onCheckedChange = onOverlayToggle,
                )
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
        overlayEnabled = true,
        columns = 1,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onOverlayToggle = {},
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
        overlayEnabled = false,
        columns = 1,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onOverlayToggle = {},
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
        overlayEnabled = false,
        columns = 1,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onOverlayToggle = {},
    )
}

@Preview(showBackground = true, name = "Bluetooth非対応")
@Composable
private fun SettingsContentPreviewBluetoothUnavailable() {
    SettingsContent(
        permissionStates = emptyMap(),
        bluetoothAdapterState = null,
        overlayEnabled = false,
        columns = 1,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onOverlayToggle = {},
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
        overlayEnabled = false,
        columns = 1,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onOverlayToggle = {},
    )
}

@Preview(showBackground = true, widthDp = 700, name = "2列 (Medium)")
@Composable
private fun SettingsContentPreviewMedium() {
    SettingsContent(
        permissionStates = mapOf(
            android.Manifest.permission.BLUETOOTH_CONNECT to false,
            android.Manifest.permission.BLUETOOTH_SCAN to false,
        ),
        bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
        overlayEnabled = false,
        columns = 2,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onOverlayToggle = {},
    )
}

@Preview(showBackground = true, widthDp = 900, name = "3列 (Expanded)")
@Composable
private fun SettingsContentPreviewExpanded() {
    SettingsContent(
        permissionStates = mapOf(
            android.Manifest.permission.BLUETOOTH_CONNECT to false,
            android.Manifest.permission.BLUETOOTH_SCAN to false,
        ),
        bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
        overlayEnabled = false,
        columns = 3,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onOverlayToggle = {},
    )
}
