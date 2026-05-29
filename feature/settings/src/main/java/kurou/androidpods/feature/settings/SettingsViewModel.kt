package kurou.androidpods.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.CheckUpdateUseCase
import kurou.androidpods.core.domain.GetAppleDevicesUseCase
import kurou.androidpods.core.domain.GetBluetoothAdapterStateUseCase
import kurou.androidpods.core.domain.GetOverlaySettingsUseCase
import kurou.androidpods.core.domain.OverlayPosition
import kurou.androidpods.core.domain.OverlayPositionUseCase
import kurou.androidpods.core.domain.RssiThreshold
import kurou.androidpods.core.domain.RssiThresholdUseCase
import kurou.androidpods.core.domain.ThemeSettings
import kurou.androidpods.core.domain.ThemeSettingsUseCase
import kurou.androidpods.core.domain.UnknownDeviceUseCase
import javax.inject.Inject

sealed interface ServiceEvent {
    data object StopScan : ServiceEvent
    data object StartScan : ServiceEvent
    data object ShowRestartSnackbar : ServiceEvent
    data object OpenOverlayPermissionSettings : ServiceEvent
}

private data class ScanState(
    val bluetoothAdapterState: Int?,
    val appleDevices: Map<String, AppleDevice>,
    val unknownModelCodes: Set<String>,
)

private data class OverlayState(
    val overlayEnabled: Boolean,
    val overlayPosition: OverlayPosition,
)

private data class WarningState(
    val updateAvailable: Boolean,
    val isNotificationsDisabled: Boolean,
    val isDeviceScanChannelDisabled: Boolean,
    val isBatteryOptimizationExempt: Boolean,
    val permissionStates: Map<String, Boolean>,
)

private data class AppSettingsState(
    val themeSettings: ThemeSettings,
    val rssiThreshold: RssiThreshold,
)

private data class UiControlState(
    val isServiceRestarting: Boolean = false,
    val showPermissionRequiredDialog: Boolean = false,
    val showThemeModeDialog: Boolean = false,
    val showOverlayPositionDialog: Boolean = false,
    val showUnknownDeviceSheet: Boolean = false,
    val showRssiThresholdDialog: Boolean = false,
)

data class SettingsUiState(
    val bluetoothAdapterState: Int? = null,
    val appleDevices: Map<String, AppleDevice> = emptyMap(),
    val overlayEnabled: Boolean = false,
    val updateAvailable: Boolean = false,
    val themeSettings: ThemeSettings = ThemeSettings(),
    val overlayPosition: OverlayPosition = OverlayPosition.TOP,
    val isNotificationsDisabled: Boolean = false,
    val isDeviceScanChannelDisabled: Boolean = false,
    val isBatteryOptimizationExempt: Boolean = false,
    val unknownModelCodes: Set<String> = emptySet(),
    val permissionStates: Map<String, Boolean> = emptyMap(),
    val rssiThreshold: RssiThreshold = RssiThreshold.VERY_NEAR,
    val isServiceRestarting: Boolean = false,
    val showPermissionRequiredDialog: Boolean = false,
    val showThemeModeDialog: Boolean = false,
    val showOverlayPositionDialog: Boolean = false,
    val showUnknownDeviceSheet: Boolean = false,
    val showRssiThresholdDialog: Boolean = false,
) {
    val hasUnknownDevices: Boolean get() = unknownModelCodes.isNotEmpty()
}

@HiltViewModel
@Suppress("TooManyFunctions", "LongParameterList")
class SettingsViewModel @Inject constructor(
    private val getBluetoothAdapterStateUseCase: GetBluetoothAdapterStateUseCase,
    private val getAppleDevicesUseCase: GetAppleDevicesUseCase,
    private val getOverlaySettingsUseCase: GetOverlaySettingsUseCase,
    private val checkUpdateUseCase: CheckUpdateUseCase,
    private val themeSettingsUseCase: ThemeSettingsUseCase,
    private val overlayPositionUseCase: OverlayPositionUseCase,
    private val unknownDeviceUseCase: UnknownDeviceUseCase,
    private val rssiThresholdUseCase: RssiThresholdUseCase,
) : ViewModel() {
    private val _updateAvailable = MutableStateFlow(false)
    private val _isNotificationsDisabled = MutableStateFlow(false)
    private val _isDeviceScanChannelDisabled = MutableStateFlow(false)
    private val _isBatteryOptimizationExempt = MutableStateFlow(false)
    private val _permissionStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val _uiControlState = MutableStateFlow(UiControlState())
    private val _serviceEvents = MutableSharedFlow<ServiceEvent>()
    val serviceEvents: SharedFlow<ServiceEvent> = _serviceEvents.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> =
        combine(
            combine(
                getBluetoothAdapterStateUseCase.observe(),
                getAppleDevicesUseCase.observe(),
                unknownDeviceUseCase.observe(),
            ) { bluetoothAdapterState, appleDevices, unknownModelCodes ->
                ScanState(bluetoothAdapterState, appleDevices, unknownModelCodes)
            },
            combine(
                getOverlaySettingsUseCase.observe(),
                overlayPositionUseCase.observe(),
            ) { overlayEnabled, overlayPosition ->
                OverlayState(overlayEnabled, overlayPosition)
            },
            combine(
                _updateAvailable,
                _isNotificationsDisabled,
                _isDeviceScanChannelDisabled,
                _isBatteryOptimizationExempt,
                _permissionStates,
            ) { updateAvailable, isNotificationsDisabled, isDeviceScanChannelDisabled,
                isBatteryOptimizationExempt, permissionStates,
                ->
                WarningState(
                    updateAvailable, isNotificationsDisabled, isDeviceScanChannelDisabled,
                    isBatteryOptimizationExempt, permissionStates,
                )
            },
            combine(
                themeSettingsUseCase.observe(),
                rssiThresholdUseCase.observe(),
            ) { themeSettings, rssiThreshold ->
                AppSettingsState(themeSettings, rssiThreshold)
            },
            _uiControlState,
        ) { scan, overlay, warning, appSettings, uiControl ->
            SettingsUiState(
                bluetoothAdapterState = scan.bluetoothAdapterState,
                appleDevices = scan.appleDevices,
                unknownModelCodes = scan.unknownModelCodes,
                overlayEnabled = overlay.overlayEnabled,
                overlayPosition = overlay.overlayPosition,
                themeSettings = appSettings.themeSettings,
                rssiThreshold = appSettings.rssiThreshold,
                updateAvailable = warning.updateAvailable,
                isNotificationsDisabled = warning.isNotificationsDisabled,
                isDeviceScanChannelDisabled = warning.isDeviceScanChannelDisabled,
                isBatteryOptimizationExempt = warning.isBatteryOptimizationExempt,
                permissionStates = warning.permissionStates,
                isServiceRestarting = uiControl.isServiceRestarting,
                showPermissionRequiredDialog = uiControl.showPermissionRequiredDialog,
                showThemeModeDialog = uiControl.showThemeModeDialog,
                showOverlayPositionDialog = uiControl.showOverlayPositionDialog,
                showUnknownDeviceSheet = uiControl.showUnknownDeviceSheet,
                showRssiThresholdDialog = uiControl.showRssiThresholdDialog,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SettingsUiState(),
        )

    fun showPermissionRequiredDialog() =
        _uiControlState.update { it.copy(showPermissionRequiredDialog = true) }

    fun dismissPermissionRequiredDialog() =
        _uiControlState.update { it.copy(showPermissionRequiredDialog = false) }

    fun showThemeModeDialog() =
        _uiControlState.update { it.copy(showThemeModeDialog = true) }

    fun dismissThemeModeDialog() =
        _uiControlState.update { it.copy(showThemeModeDialog = false) }

    fun showOverlayPositionDialog() =
        _uiControlState.update { it.copy(showOverlayPositionDialog = true) }

    fun dismissOverlayPositionDialog() =
        _uiControlState.update { it.copy(showOverlayPositionDialog = false) }

    fun showUnknownDeviceSheet() =
        _uiControlState.update { it.copy(showUnknownDeviceSheet = true) }

    fun dismissUnknownDeviceSheet() =
        _uiControlState.update { it.copy(showUnknownDeviceSheet = false) }

    fun reportUnknownDevice(modelCode: String, deviceName: String) {
        viewModelScope.launch {
            try {
                val code = modelCode.removePrefix("0x").toInt(16)
                unknownDeviceUseCase.report(code, deviceName)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            } finally {
                dismissUnknownDeviceSheet()
            }
        }
    }

    fun checkUpdate(currentVersion: String) {
        viewModelScope.launch {
            _updateAvailable.update { checkUpdateUseCase(currentVersion).getOrDefault(false) }
        }
    }

    fun refreshOverlayState() {
        getOverlaySettingsUseCase.refresh()
    }

    fun setOverlayEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && !getOverlaySettingsUseCase.hasPermission()) {
                getOverlaySettingsUseCase.setEnabled(true)
                _serviceEvents.emit(ServiceEvent.OpenOverlayPermissionSettings)
            } else {
                getOverlaySettingsUseCase.setEnabled(enabled)
            }
        }
    }

    fun refreshNotificationState(isDisabled: Boolean) {
        _isNotificationsDisabled.update { isDisabled }
    }

    fun refreshDeviceScanChannelState(isDisabled: Boolean) {
        _isDeviceScanChannelDisabled.update { isDisabled }
    }

    fun refreshBatteryOptimizationState(isExempt: Boolean) {
        _isBatteryOptimizationExempt.update { isExempt }
    }

    fun refreshPermissionStates(states: Map<String, Boolean>) {
        _permissionStates.update { states }
    }

    fun restartService() {
        viewModelScope.launch {
            _uiControlState.update { it.copy(isServiceRestarting = true) }
            _serviceEvents.emit(ServiceEvent.StopScan)
            _serviceEvents.emit(ServiceEvent.StartScan)
            delay(RESTART_DELAY_MS)
            _uiControlState.update { it.copy(isServiceRestarting = false) }
            _serviceEvents.emit(ServiceEvent.ShowRestartSnackbar)
        }
    }

    fun showRssiThresholdDialog() =
        _uiControlState.update { it.copy(showRssiThresholdDialog = true) }

    fun dismissRssiThresholdDialog() =
        _uiControlState.update { it.copy(showRssiThresholdDialog = false) }

    fun updateRssiThreshold(threshold: RssiThreshold) {
        viewModelScope.launch {
            rssiThresholdUseCase.update(threshold)
        }
    }

    fun updateThemeSettings(settings: ThemeSettings) {
        viewModelScope.launch {
            themeSettingsUseCase.update(settings)
        }
    }

    fun updateOverlayPosition(position: OverlayPosition) {
        viewModelScope.launch {
            overlayPositionUseCase.update(position)
        }
    }

    fun startScan() {
        getAppleDevicesUseCase.startScan()
    }

    fun stopScan() {
        getAppleDevicesUseCase.stopScan()
    }

    companion object {
        private const val RESTART_DELAY_MS = 5_000L
    }
}
