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
)

private data class InternalState(
    val overlayEnabled: Boolean,
    val updateAvailable: Boolean,
    val isNotificationsDisabled: Boolean,
    val isDeviceScanChannelDisabled: Boolean,
    val isBatteryOptimizationExempt: Boolean,
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
    private val _overlayEnabled = MutableStateFlow(getOverlaySettingsUseCase.isEnabled())
    private val _updateAvailable = MutableStateFlow(false)
    private val _isNotificationsDisabled = MutableStateFlow(false)
    private val _isDeviceScanChannelDisabled = MutableStateFlow(false)
    private val _isBatteryOptimizationExempt = MutableStateFlow(false)
    private val _permissionStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val _isServiceRestarting = MutableStateFlow(false)
    private val _serviceEvents = MutableSharedFlow<ServiceEvent>()
    val serviceEvents: SharedFlow<ServiceEvent> = _serviceEvents.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> =
        combine(
            combine(
                getBluetoothAdapterStateUseCase.observe(),
                getAppleDevicesUseCase.observe(),
                themeSettingsUseCase.observe(),
                overlayPositionUseCase.observe(),
            ) { bluetoothAdapterState, appleDevices, themeSettings, overlayPosition ->
                UseCaseState(bluetoothAdapterState, appleDevices, themeSettings, overlayPosition)
            },
            combine(
                _overlayEnabled,
                _updateAvailable,
                _isNotificationsDisabled,
                _isDeviceScanChannelDisabled,
                _isBatteryOptimizationExempt,
            ) { overlayEnabled,
                updateAvailable,
                isNotificationsDisabled,
                isDeviceScanChannelDisabled,
                isBatteryOptimizationExempt,
                ->
                InternalState(
                    overlayEnabled,
                    updateAvailable,
                    isNotificationsDisabled,
                    isDeviceScanChannelDisabled,
                    isBatteryOptimizationExempt,
                )
            },
            _permissionStates,
            _isServiceRestarting,
        ) { useCaseState, internalState, permissionStates, isServiceRestarting ->
            SettingsUiState(
                bluetoothAdapterState = useCaseState.bluetoothAdapterState,
                appleDevices = useCaseState.appleDevices,
                themeSettings = useCaseState.themeSettings,
                overlayPosition = useCaseState.overlayPosition,
                overlayEnabled = internalState.overlayEnabled,
                updateAvailable = internalState.updateAvailable,
                isNotificationsDisabled = internalState.isNotificationsDisabled,
                isDeviceScanChannelDisabled = internalState.isDeviceScanChannelDisabled,
                isBatteryOptimizationExempt = internalState.isBatteryOptimizationExempt,
                permissionStates = permissionStates,
                isServiceRestarting = isServiceRestarting,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SettingsUiState(),
        )

    fun checkUpdate(currentVersion: String) {
        viewModelScope.launch {
            _updateAvailable.update { checkUpdateUseCase(currentVersion).getOrDefault(false) }
        }
    }

    fun refreshOverlayState() {
        _overlayEnabled.update { getOverlaySettingsUseCase.isEnabled() }
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
            delay(5_000)
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
}
