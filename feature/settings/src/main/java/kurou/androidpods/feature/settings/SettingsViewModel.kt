package kurou.androidpods.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

data class SettingsUiState(
    val bluetoothAdapterState: Int? = null,
    val appleDevices: Map<String, AppleDevice> = emptyMap(),
    val overlayEnabled: Boolean = false,
    val updateAvailable: Boolean = false,
    val themeSettings: ThemeSettings = ThemeSettings(),
    val overlayPosition: OverlayPosition = OverlayPosition.BOTTOM,
    val isNotificationsDisabled: Boolean = false,
    val isDeviceScanChannelDisabled: Boolean = false,
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

    val uiState: StateFlow<SettingsUiState> =
        combine(
            combine(
                getBluetoothAdapterStateUseCase.observe(),
                getAppleDevicesUseCase.observe(),
                _overlayEnabled,
            ) { bluetoothAdapterState, appleDevices, overlayEnabled ->
                Triple(bluetoothAdapterState, appleDevices, overlayEnabled)
            },
            combine(
                _updateAvailable,
                themeSettingsUseCase.observe(),
                overlayPositionUseCase.observe(),
            ) { updateAvailable, themeSettings, overlayPosition ->
                Triple(updateAvailable, themeSettings, overlayPosition)
            },
            combine(
                _isNotificationsDisabled,
                _isDeviceScanChannelDisabled,
            ) { isNotificationsDisabled, isDeviceScanChannelDisabled ->
                Pair(isNotificationsDisabled, isDeviceScanChannelDisabled)
            },
        ) { (bluetoothAdapterState, appleDevices, overlayEnabled),
            (updateAvailable, themeSettings, overlayPosition),
            (isNotificationsDisabled, isDeviceScanChannelDisabled),
            ->
            SettingsUiState(
                bluetoothAdapterState = bluetoothAdapterState,
                appleDevices = appleDevices,
                overlayEnabled = overlayEnabled,
                updateAvailable = updateAvailable,
                themeSettings = themeSettings,
                overlayPosition = overlayPosition,
                isNotificationsDisabled = isNotificationsDisabled,
                isDeviceScanChannelDisabled = isDeviceScanChannelDisabled,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SettingsUiState(),
        )

    fun checkUpdate(currentVersion: String) {
        viewModelScope.launch {
            _updateAvailable.update { checkUpdateUseCase(currentVersion) }
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
