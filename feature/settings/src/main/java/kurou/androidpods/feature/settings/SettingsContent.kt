package kurou.androidpods.feature.settings

import android.bluetooth.BluetoothAdapter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun SettingsContent(
    permissionStates: Map<String, Boolean>,
    bluetoothAdapterState: Int?,
    overlayEnabled: Boolean,
    updateAvailable: Boolean,
    columns: Int,
    onPermissionWarningClick: () -> Unit,
    onBluetoothWarningClick: () -> Unit,
    onOverlayToggle: (Boolean) -> Unit,
    onRestartServiceClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onDevicesClick: () -> Unit,
    onGithubClick: () -> Unit,
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
                val backgroundColor = if (isBluetoothUnavailable)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.errorContainer
                val contentColor = if (isBluetoothUnavailable)
                    MaterialTheme.colorScheme.onError
                else
                    MaterialTheme.colorScheme.onErrorContainer
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
                    if (isBluetoothOff)
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = contentColor,
                        )
                }
            }
        }
        if (updateAvailable) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable(onClick = onUpdateClick)
                        .padding(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.update_available),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
        item(span = { GridItemSpan(1) }) {
            SettingsItem(
                label = stringResource(R.string.overlay_setting_label),
                icon = painterResource(R.drawable.ic_overlay_setting_label),
                onClick = { onOverlayToggle(!overlayEnabled) },
            ) {
                Switch(
                    checked = overlayEnabled,
                    onCheckedChange = onOverlayToggle,
                )
            }
        }
        item(span = { GridItemSpan(1) }) {
            SettingsItem(
                label = stringResource(R.string.restart_service),
                icon = painterResource(R.drawable.ic_restart_service),
                onClick = onRestartServiceClick,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                )
            }
        }
        item(span = { GridItemSpan(1) }) {
            SettingsItem(
                label = stringResource(R.string.compatible_devices),
                icon = painterResource(R.drawable.ic_compatible_devices),
                onClick = onDevicesClick,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                )
            }
        }
        item(span = { GridItemSpan(1) }) {
            SettingsItem(
                label = stringResource(R.string.open_source_licenses),
                icon = painterResource(R.drawable.ic_open_source_licenses),
                onClick = onLicensesClick,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                )
            }
        }
        item(span = { GridItemSpan(1) }) {
            SettingsItem(
                label = stringResource(R.string.github_repository),
                icon = painterResource(R.drawable.ic_github_repository),
                onClick = onGithubClick,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    label: String,
    icon: Painter,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
        )
        // M3 の minimumInteractiveComponentSize (48.dp) に揃えることで
        // Switch と Icon の高さを構造的に一致させる
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            trailing()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreviewNoWarning() {
    SettingsContent(
        permissionStates = mapOf(
            android.Manifest.permission.BLUETOOTH_CONNECT to true,
            android.Manifest.permission.BLUETOOTH_SCAN to true,
        ),
        bluetoothAdapterState = BluetoothAdapter.STATE_ON,
        overlayEnabled = true,
        updateAvailable = false,
        columns = 1,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onOverlayToggle = {},
        onRestartServiceClick = {},
        onUpdateClick = {},
        onLicensesClick = {},
        onDevicesClick = {},
        onGithubClick = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreviewBluetoothUnavailable() {
    SettingsContent(
        permissionStates = emptyMap(),
        bluetoothAdapterState = null,
        overlayEnabled = false,
        updateAvailable = false,
        columns = 1,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onOverlayToggle = {},
        onRestartServiceClick = {},
        onUpdateClick = {},
        onLicensesClick = {},
        onDevicesClick = {},
        onGithubClick = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreviewAllWarnings() {
    SettingsContent(
        permissionStates = mapOf(
            android.Manifest.permission.BLUETOOTH_CONNECT to false,
            android.Manifest.permission.BLUETOOTH_SCAN to false,
        ),
        bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
        overlayEnabled = false,
        updateAvailable = true,
        columns = 1,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onOverlayToggle = {},
        onRestartServiceClick = {},
        onUpdateClick = {},
        onLicensesClick = {},
        onDevicesClick = {},
        onGithubClick = {},
    )
}

@Preview(showBackground = true, widthDp = 700)
@Composable
private fun SettingsContentPreviewTwoColumns() {
    SettingsContent(
        permissionStates = mapOf(
            android.Manifest.permission.BLUETOOTH_CONNECT to false,
            android.Manifest.permission.BLUETOOTH_SCAN to false,
        ),
        bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
        overlayEnabled = false,
        updateAvailable = true,
        columns = 2,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onOverlayToggle = {},
        onRestartServiceClick = {},
        onUpdateClick = {},
        onLicensesClick = {},
        onDevicesClick = {},
        onGithubClick = {},
    )
}

@Preview(showBackground = true, widthDp = 900)
@Composable
private fun SettingsContentPreviewThreeColumns() {
    SettingsContent(
        permissionStates = mapOf(
            android.Manifest.permission.BLUETOOTH_CONNECT to false,
            android.Manifest.permission.BLUETOOTH_SCAN to false,
        ),
        bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
        overlayEnabled = false,
        updateAvailable = true,
        columns = 3,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onOverlayToggle = {},
        onRestartServiceClick = {},
        onUpdateClick = {},
        onLicensesClick = {},
        onDevicesClick = {},
        onGithubClick = {},
    )
}
