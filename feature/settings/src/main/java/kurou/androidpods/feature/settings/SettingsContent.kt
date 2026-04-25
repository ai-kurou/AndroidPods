@file:Suppress("TooManyFunctions")

package kurou.androidpods.feature.settings

import android.bluetooth.BluetoothAdapter
import android.os.Build
import androidx.annotation.StringRes
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
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurou.androidpods.core.domain.OverlayPosition
import kurou.androidpods.core.domain.ThemeMode
import kurou.androidpods.core.domain.ThemeSettings

@Composable
internal fun SettingsContent(
    permissionStates: Map<String, Boolean>,
    bluetoothAdapterState: Int?,
    overlayEnabled: Boolean,
    overlayPosition: OverlayPosition,
    updateAvailable: Boolean,
    isNotificationsDisabled: Boolean,
    isDeviceScanChannelDisabled: Boolean,
    isServiceRestarting: Boolean,
    columns: Int,
    themeSettings: ThemeSettings,
    onPermissionWarningClick: () -> Unit,
    onBluetoothWarningClick: () -> Unit,
    onNotificationWarningClick: () -> Unit,
    onDeviceScanChannelWarningClick: () -> Unit,
    onOverlayToggle: (Boolean) -> Unit,
    onOverlayPositionClick: () -> Unit,
    onRestartServiceClick: () -> Unit,
    onThemeModeClick: () -> Unit,
    onDynamicColorToggle: (Boolean) -> Unit,
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
        modifier = modifier.fillMaxSize().padding(16.dp).testTag("SettingsGrid"),
    ) {
        bannerItems(
            state = BannerState(
                hasNotGranted = hasNotGranted,
                isBluetoothUnavailable = isBluetoothUnavailable,
                isBluetoothOff = isBluetoothOff,
                updateAvailable = updateAvailable,
                isNotificationsDisabled = isNotificationsDisabled,
                isDeviceScanChannelDisabled = isDeviceScanChannelDisabled,
            ),
            onPermissionWarningClick = onPermissionWarningClick,
            onBluetoothWarningClick = onBluetoothWarningClick,
            onNotificationWarningClick = onNotificationWarningClick,
            onDeviceScanChannelWarningClick = onDeviceScanChannelWarningClick,
            onUpdateClick = onUpdateClick,
        )
        overlaySectionItems(
            overlayEnabled = overlayEnabled,
            overlayPosition = overlayPosition,
            onOverlayToggle = onOverlayToggle,
            onOverlayPositionClick = onOverlayPositionClick,
        )
        scanServiceSectionItems(
            isServiceRestarting = isServiceRestarting,
            onRestartServiceClick = onRestartServiceClick,
        )
        appearanceSectionItems(
            themeSettings = themeSettings,
            onThemeModeClick = onThemeModeClick,
            onDynamicColorToggle = onDynamicColorToggle,
        )
        infoSectionItems(
            onDevicesClick = onDevicesClick,
            onLicensesClick = onLicensesClick,
            onGithubClick = onGithubClick,
        )
    }
}

private data class BannerState(
    val hasNotGranted: Boolean,
    val isBluetoothUnavailable: Boolean,
    val isBluetoothOff: Boolean,
    val updateAvailable: Boolean,
    val isNotificationsDisabled: Boolean,
    val isDeviceScanChannelDisabled: Boolean,
)

private fun LazyGridScope.bannerItems(
    state: BannerState,
    onPermissionWarningClick: () -> Unit,
    onBluetoothWarningClick: () -> Unit,
    onNotificationWarningClick: () -> Unit,
    onDeviceScanChannelWarningClick: () -> Unit,
    onUpdateClick: () -> Unit,
) {
    if (state.hasNotGranted) {
        item(key = R.string.permission_warning, span = { GridItemSpan(maxLineSpan) }) {
            WarningBanner(
                text = stringResource(R.string.permission_warning),
                onClick = onPermissionWarningClick,
                modifier = Modifier.animateItem(),
            )
        }
    }
    if (state.isNotificationsDisabled) {
        item(key = R.string.notification_disabled_warning, span = { GridItemSpan(maxLineSpan) }) {
            WarningBanner(
                text = stringResource(R.string.notification_disabled_warning),
                onClick = onNotificationWarningClick,
                modifier = Modifier.animateItem(),
            )
        }
    }
    if (state.isDeviceScanChannelDisabled) {
        item(key = R.string.device_scan_channel_disabled_warning, span = { GridItemSpan(maxLineSpan) }) {
            WarningBanner(
                text = stringResource(R.string.device_scan_channel_disabled_warning),
                onClick = onDeviceScanChannelWarningClick,
                modifier = Modifier.animateItem(),
            )
        }
    }
    if (state.isBluetoothUnavailable || state.isBluetoothOff) {
        item(key = R.string.bluetooth_warning, span = { GridItemSpan(maxLineSpan) }) {
            BluetoothWarningBanner(
                isBluetoothUnavailable = state.isBluetoothUnavailable,
                isBluetoothOff = state.isBluetoothOff,
                onBluetoothWarningClick = onBluetoothWarningClick,
                modifier = Modifier.animateItem(),
            )
        }
    }
    if (state.updateAvailable) {
        item(key = R.string.update_available, span = { GridItemSpan(maxLineSpan) }) {
            UpdateAvailableBanner(
                onClick = onUpdateClick,
                modifier = Modifier.animateItem(),
            )
        }
    }
}

private fun LazyGridScope.overlaySectionItems(
    overlayEnabled: Boolean,
    overlayPosition: OverlayPosition,
    onOverlayToggle: (Boolean) -> Unit,
    onOverlayPositionClick: () -> Unit,
) {
    sectionLabel(R.string.overlay_section_label)
    item(key = R.string.overlay_setting_label, span = { GridItemSpan(1) }) {
        SettingsItem(
            label = stringResource(R.string.overlay_setting_label),
            icon = painterResource(R.drawable.ic_overlay_setting_label),
            onClick = { onOverlayToggle(!overlayEnabled) },
            modifier = Modifier.animateItem(),
        ) {
            Switch(
                checked = overlayEnabled,
                onCheckedChange = onOverlayToggle,
            )
        }
    }
    item(key = R.string.overlay_position_label, span = { GridItemSpan(1) }) {
        SettingsItem(
            label = stringResource(R.string.overlay_position_label),
            icon = painterResource(R.drawable.ic_overlay_position),
            onClick = onOverlayPositionClick,
            modifier = Modifier.animateItem(),
        ) {
            Text(
                text = stringResource(overlayPosition.toStringRes()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun LazyGridScope.scanServiceSectionItems(
    isServiceRestarting: Boolean,
    onRestartServiceClick: () -> Unit,
) {
    sectionLabel(R.string.scan_service_section_label)
    item(key = R.string.restart_service, span = { GridItemSpan(1) }) {
        SettingsItem(
            label = stringResource(R.string.restart_service),
            icon = painterResource(R.drawable.ic_restart_service),
            onClick = onRestartServiceClick,
            enabled = !isServiceRestarting,
            modifier = Modifier.animateItem(),
        ) {
            if (isServiceRestarting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                )
            }
        }
    }
}

private fun LazyGridScope.appearanceSectionItems(
    themeSettings: ThemeSettings,
    onThemeModeClick: () -> Unit,
    onDynamicColorToggle: (Boolean) -> Unit,
) {
    sectionLabel(R.string.appearance_section_label)
    item(key = R.string.theme_mode_label, span = { GridItemSpan(1) }) {
        SettingsItem(
            label = stringResource(R.string.theme_mode_label),
            icon = painterResource(R.drawable.ic_theme),
            onClick = onThemeModeClick,
            modifier = Modifier.animateItem(),
        ) {
            Text(
                text = stringResource(themeSettings.themeMode.toStringRes()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        item(key = R.string.dynamic_color_label, span = { GridItemSpan(1) }) {
            SettingsItem(
                label = stringResource(R.string.dynamic_color_label),
                icon = painterResource(R.drawable.ic_dynamic_color),
                onClick = { onDynamicColorToggle(!themeSettings.useDynamicColor) },
                modifier = Modifier.animateItem(),
            ) {
                Switch(
                    checked = themeSettings.useDynamicColor,
                    onCheckedChange = onDynamicColorToggle,
                )
            }
        }
    }
}

private fun LazyGridScope.infoSectionItems(
    onDevicesClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onGithubClick: () -> Unit,
) {
    sectionLabel(R.string.info_section_label)
    item(key = R.string.compatible_devices, span = { GridItemSpan(1) }) {
        SettingsItem(
            label = stringResource(R.string.compatible_devices),
            icon = painterResource(R.drawable.ic_compatible_devices),
            onClick = onDevicesClick,
            modifier = Modifier.animateItem(),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
            )
        }
    }
    item(key = R.string.open_source_licenses, span = { GridItemSpan(1) }) {
        SettingsItem(
            label = stringResource(R.string.open_source_licenses),
            icon = painterResource(R.drawable.ic_open_source_licenses),
            onClick = onLicensesClick,
            modifier = Modifier.animateItem(),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
            )
        }
    }
    item(key = R.string.github_repository, span = { GridItemSpan(1) }) {
        SettingsItem(
            label = stringResource(R.string.github_repository),
            icon = painterResource(R.drawable.ic_github_repository),
            onClick = onGithubClick,
            modifier = Modifier.animateItem(),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
            )
        }
    }
}

private fun LazyGridScope.sectionLabel(@StringRes labelRes: Int) {
    item(key = labelRes, span = { GridItemSpan(maxLineSpan) }) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .animateItem()
                .padding(horizontal = 4.dp, vertical = 4.dp),
        )
    }
}

@Composable
internal fun WarningBanner(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.errorContainer)
                .clickable(onClick = onClick)
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
            text = text,
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

@Composable
private fun BluetoothWarningBanner(
    isBluetoothUnavailable: Boolean,
    isBluetoothOff: Boolean,
    onBluetoothWarningClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        if (isBluetoothUnavailable) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.errorContainer
        }
    val contentColor =
        if (isBluetoothUnavailable) {
            MaterialTheme.colorScheme.onError
        } else {
            MaterialTheme.colorScheme.onErrorContainer
        }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .then(
                    if (isBluetoothOff) {
                        Modifier.clickable(onClick = onBluetoothWarningClick)
                    } else {
                        Modifier
                    },
                ).padding(12.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text =
                stringResource(
                    if (isBluetoothUnavailable) {
                        R.string.bluetooth_not_supported
                    } else {
                        R.string.bluetooth_warning
                    },
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

@Composable
private fun UpdateAvailableBanner(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClick = onClick)
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

@Composable
private fun SettingsItem(
    label: String,
    icon: Painter,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    trailing: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(if (onClick != null && enabled) Modifier.clickable(onClick = onClick) else Modifier)
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

internal fun ThemeMode.toStringRes(): Int =
    when (this) {
        ThemeMode.SYSTEM -> R.string.theme_mode_system
        ThemeMode.LIGHT -> R.string.theme_mode_light
        ThemeMode.DARK -> R.string.theme_mode_dark
    }

internal fun OverlayPosition.toStringRes(): Int =
    when (this) {
        OverlayPosition.TOP -> R.string.overlay_position_top
        OverlayPosition.BOTTOM -> R.string.overlay_position_bottom
    }

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun SettingsContentPreviewNoWarning() {
    SettingsContent(
        permissionStates =
            mapOf(
                android.Manifest.permission.BLUETOOTH_CONNECT to true,
                android.Manifest.permission.BLUETOOTH_SCAN to true,
            ),
        bluetoothAdapterState = BluetoothAdapter.STATE_ON,
        overlayEnabled = true,
        overlayPosition = OverlayPosition.BOTTOM,
        updateAvailable = false,
        isNotificationsDisabled = false,
        isDeviceScanChannelDisabled = false,
        isServiceRestarting = false,
        columns = 1,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onNotificationWarningClick = {},
        onDeviceScanChannelWarningClick = {},
        onOverlayToggle = {},
        onOverlayPositionClick = {},
        onRestartServiceClick = {},
        themeSettings = ThemeSettings(),
        onThemeModeClick = {},
        onDynamicColorToggle = {},
        onUpdateClick = {},
        onLicensesClick = {},
        onDevicesClick = {},
        onGithubClick = {},
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun SettingsContentPreviewBluetoothUnavailable() {
    SettingsContent(
        permissionStates = emptyMap(),
        bluetoothAdapterState = null,
        overlayEnabled = false,
        overlayPosition = OverlayPosition.BOTTOM,
        updateAvailable = false,
        isNotificationsDisabled = false,
        isDeviceScanChannelDisabled = false,
        isServiceRestarting = false,
        columns = 1,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onNotificationWarningClick = {},
        onDeviceScanChannelWarningClick = {},
        onOverlayToggle = {},
        onOverlayPositionClick = {},
        onRestartServiceClick = {},
        themeSettings = ThemeSettings(),
        onThemeModeClick = {},
        onDynamicColorToggle = {},
        onUpdateClick = {},
        onLicensesClick = {},
        onDevicesClick = {},
        onGithubClick = {},
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun SettingsContentPreviewAllWarnings() {
    SettingsContent(
        permissionStates =
            mapOf(
                android.Manifest.permission.BLUETOOTH_CONNECT to false,
                android.Manifest.permission.BLUETOOTH_SCAN to false,
            ),
        bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
        overlayEnabled = false,
        overlayPosition = OverlayPosition.TOP,
        updateAvailable = true,
        isNotificationsDisabled = true,
        isDeviceScanChannelDisabled = true,
        isServiceRestarting = false,
        columns = 1,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onNotificationWarningClick = {},
        onDeviceScanChannelWarningClick = {},
        onOverlayToggle = {},
        onOverlayPositionClick = {},
        onRestartServiceClick = {},
        themeSettings = ThemeSettings(),
        onThemeModeClick = {},
        onDynamicColorToggle = {},
        onUpdateClick = {},
        onLicensesClick = {},
        onDevicesClick = {},
        onGithubClick = {},
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun SettingsContentPreviewServiceRestarting() {
    SettingsContent(
        permissionStates =
            mapOf(
                android.Manifest.permission.BLUETOOTH_CONNECT to true,
                android.Manifest.permission.BLUETOOTH_SCAN to true,
            ),
        bluetoothAdapterState = BluetoothAdapter.STATE_ON,
        overlayEnabled = true,
        overlayPosition = OverlayPosition.BOTTOM,
        updateAvailable = false,
        isNotificationsDisabled = false,
        isDeviceScanChannelDisabled = false,
        isServiceRestarting = true,
        columns = 1,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onNotificationWarningClick = {},
        onDeviceScanChannelWarningClick = {},
        onOverlayToggle = {},
        onOverlayPositionClick = {},
        onRestartServiceClick = {},
        themeSettings = ThemeSettings(),
        onThemeModeClick = {},
        onDynamicColorToggle = {},
        onUpdateClick = {},
        onLicensesClick = {},
        onDevicesClick = {},
        onGithubClick = {},
    )
}

@Preview(showBackground = true, widthDp = 700, heightDp = 400)
@Composable
private fun SettingsContentPreviewTwoColumns() {
    SettingsContent(
        permissionStates =
            mapOf(
                android.Manifest.permission.BLUETOOTH_CONNECT to false,
                android.Manifest.permission.BLUETOOTH_SCAN to false,
            ),
        bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
        overlayEnabled = false,
        overlayPosition = OverlayPosition.BOTTOM,
        updateAvailable = true,
        isNotificationsDisabled = true,
        isDeviceScanChannelDisabled = false,
        isServiceRestarting = false,
        columns = 2,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onNotificationWarningClick = {},
        onDeviceScanChannelWarningClick = {},
        onOverlayToggle = {},
        onOverlayPositionClick = {},
        onRestartServiceClick = {},
        themeSettings = ThemeSettings(),
        onThemeModeClick = {},
        onDynamicColorToggle = {},
        onUpdateClick = {},
        onLicensesClick = {},
        onDevicesClick = {},
        onGithubClick = {},
    )
}

@Preview(showBackground = true, widthDp = 900, heightDp = 400)
@Composable
private fun SettingsContentPreviewThreeColumns() {
    SettingsContent(
        permissionStates =
            mapOf(
                android.Manifest.permission.BLUETOOTH_CONNECT to false,
                android.Manifest.permission.BLUETOOTH_SCAN to false,
            ),
        bluetoothAdapterState = BluetoothAdapter.STATE_OFF,
        overlayEnabled = false,
        overlayPosition = OverlayPosition.BOTTOM,
        updateAvailable = true,
        isNotificationsDisabled = true,
        isDeviceScanChannelDisabled = false,
        isServiceRestarting = false,
        columns = 3,
        onPermissionWarningClick = {},
        onBluetoothWarningClick = {},
        onNotificationWarningClick = {},
        onDeviceScanChannelWarningClick = {},
        onOverlayToggle = {},
        onOverlayPositionClick = {},
        onRestartServiceClick = {},
        themeSettings = ThemeSettings(),
        onThemeModeClick = {},
        onDynamicColorToggle = {},
        onUpdateClick = {},
        onLicensesClick = {},
        onDevicesClick = {},
        onGithubClick = {},
    )
}
