package kurou.androidpods.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kurou.androidpods.core.domain.ThemeSettings
import kurou.androidpods.core.domain.ThemeSettingsUseCase
import javax.inject.Inject

sealed interface ServiceEvent {
    data object StopScan : ServiceEvent
    data object StartScan : ServiceEvent
    data object ShowRestartSnackbar : ServiceEvent
}

private data class UseCaseState(
    val bluetoothAdapterState: Int?,
    val appleDevices: Map<String, AppleDevice>,
    val themeSettings: ThemeSettings,
    val overlayPosition: OverlayPosition,
    val overlayEnabled: Boolean,
)

private data class InternalState(
    val updateAvailable: Boolean,
    val isNotificationsDisabled: Boolean,
    val isDeviceScanChannelDisabled: Boolean,
    val isBatteryOptimizationExempt: Boolean,
)

private data class DialogState(
    val showPermissionRequiredDialog: Boolean = false,
    val showThemeModeDialog: Boolean = false,
    val showOverlayPositionDialog: Boolean = false,
)

data class SettingsUiState(
    val bluetoothAdapterState: Int? = null,
    val appleDevices: Map<String, AppleDevice> = emptyMap(),
    val overlayEnabled: Boolean = false,
    val updateAvailable: Boolean = false,
    val themeSettings: ThemeSettings = ThemeSettings(),
    val overlayPosition: OverlayPosition = OverlayPosition.BOTTOM,
    val isNotificationsDisabled: Boolean = false,
    val isDeviceScanChannelDisabled: Boolean = false,
    val isBatteryOptimizationExempt: Boolean = false,
    val permissionStates: Map<String, Boolean> = emptyMap(),
    val isServiceRestarting: Boolean = false,
    val showPermissionRequiredDialog: Boolean = false,
    val showThemeModeDialog: Boolean = false,
    val showOverlayPositionDialog: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getBluetoothAdapterStateUseCase: GetBluetoothAdapterStateUseCase,
    private val getAppleDevicesUseCase: GetAppleDevicesUseCase,
    private val getOverlaySettingsUseCase: GetOverlaySettingsUseCase,
    private val checkUpdateUseCase: CheckUpdateUseCase,
    private val themeSettingsUseCase: ThemeSettingsUseCase,
    private val overlayPositionUseCase: OverlayPositionUseCase,
) : ViewModel() {
    private val _updateAvailable = MutableStateFlow(false)
    private val _isNotificationsDisabled = MutableStateFlow(false)
    private val _isDeviceScanChannelDisabled = MutableStateFlow(false)
    private val _isBatteryOptimizationExempt = MutableStateFlow(false)
    private val _permissionStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val _isServiceRestarting = MutableStateFlow(false)
    private val _dialogState = MutableStateFlow(DialogState())
    private val _serviceEvents = MutableSharedFlow<ServiceEvent>()
    val serviceEvents: SharedFlow<ServiceEvent> = _serviceEvents.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> =
        combine(
            combine(
                getBluetoothAdapterStateUseCase.observe(),
                getAppleDevicesUseCase.observe(),
                themeSettingsUseCase.observe(),
                overlayPositionUseCase.observe(),
                getOverlaySettingsUseCase.observe(),
            ) { bluetoothAdapterState, appleDevices, themeSettings, overlayPosition, overlayEnabled ->
                UseCaseState(bluetoothAdapterState, appleDevices, themeSettings, overlayPosition, overlayEnabled)
            },
            combine(
                _updateAvailable,
                _isNotificationsDisabled,
                _isDeviceScanChannelDisabled,
                _isBatteryOptimizationExempt,
            ) { updateAvailable,
                isNotificationsDisabled,
                isDeviceScanChannelDisabled,
                isBatteryOptimizationExempt,
                ->
                InternalState(
                    updateAvailable,
                    isNotificationsDisabled,
                    isDeviceScanChannelDisabled,
                    isBatteryOptimizationExempt,
                )
            },
            _permissionStates,
            _isServiceRestarting,
            _dialogState,
        ) { useCaseState, internalState, permissionStates, isServiceRestarting, dialogState ->
            SettingsUiState(
                bluetoothAdapterState = useCaseState.bluetoothAdapterState,
                appleDevices = useCaseState.appleDevices,
                themeSettings = useCaseState.themeSettings,
                overlayPosition = useCaseState.overlayPosition,
                overlayEnabled = useCaseState.overlayEnabled,
                updateAvailable = internalState.updateAvailable,
                isNotificationsDisabled = internalState.isNotificationsDisabled,
                isDeviceScanChannelDisabled = internalState.isDeviceScanChannelDisabled,
                isBatteryOptimizationExempt = internalState.isBatteryOptimizationExempt,
                permissionStates = permissionStates,
                isServiceRestarting = isServiceRestarting,
                showPermissionRequiredDialog = dialogState.showPermissionRequiredDialog,
                showThemeModeDialog = dialogState.showThemeModeDialog,
                showOverlayPositionDialog = dialogState.showOverlayPositionDialog,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SettingsUiState(),
        )

    fun showPermissionRequiredDialog() =
        _dialogState.update { it.copy(showPermissionRequiredDialog = true) }

    fun dismissPermissionRequiredDialog() =
        _dialogState.update { it.copy(showPermissionRequiredDialog = false) }

    fun showThemeModeDialog() =
        _dialogState.update { it.copy(showThemeModeDialog = true) }

    fun dismissThemeModeDialog() =
        _dialogState.update { it.copy(showThemeModeDialog = false) }

    fun showOverlayPositionDialog() =
        _dialogState.update { it.copy(showOverlayPositionDialog = true) }

    fun dismissOverlayPositionDialog() =
        _dialogState.update { it.copy(showOverlayPositionDialog = false) }

    fun checkUpdate(currentVersion: String) {
        viewModelScope.launch {
            _updateAvailable.update { checkUpdateUseCase(currentVersion).getOrDefault(false) }
        }
    }

    fun refreshOverlayState() {
        getOverlaySettingsUseCase.refresh()
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
            _isServiceRestarting.update { true }
            _serviceEvents.emit(ServiceEvent.StopScan)
            _serviceEvents.emit(ServiceEvent.StartScan)
            delay(RESTART_DELAY_MS)
            _isServiceRestarting.update { false }
            _serviceEvents.emit(ServiceEvent.ShowRestartSnackbar)
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
