package kurou.androidpods.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.CheckUpdateUseCase
import kurou.androidpods.core.domain.GetAppleDevicesUseCase
import kurou.androidpods.core.domain.GetBluetoothAdapterStateUseCase
import kurou.androidpods.core.domain.GetOverlaySettingsUseCase
import kurou.androidpods.core.domain.ThemeSettings
import kurou.androidpods.core.domain.ThemeSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val bluetoothAdapterState: Int? = null,
    val appleDevices: Map<String, AppleDevice> = emptyMap(),
    val overlayEnabled: Boolean = false,
    val updateAvailable: Boolean = false,
    val themeSettings: ThemeSettings = ThemeSettings(),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getBluetoothAdapterStateUseCase: GetBluetoothAdapterStateUseCase,
    private val getAppleDevicesUseCase: GetAppleDevicesUseCase,
    private val getOverlaySettingsUseCase: GetOverlaySettingsUseCase,
    private val checkUpdateUseCase: CheckUpdateUseCase,
    private val themeSettingsUseCase: ThemeSettingsUseCase,
) : ViewModel() {

    private val _overlayEnabled = MutableStateFlow(getOverlaySettingsUseCase.isEnabled())
    private val _updateAvailable = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        getBluetoothAdapterStateUseCase.observe(),
        getAppleDevicesUseCase.observe(),
        _overlayEnabled,
        _updateAvailable,
        themeSettingsUseCase.observe(),
    ) { bluetoothAdapterState, appleDevices, overlayEnabled, updateAvailable, themeSettings ->
        SettingsUiState(bluetoothAdapterState, appleDevices, overlayEnabled, updateAvailable, themeSettings)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState(),
    )

    fun checkUpdate(currentVersion: String) {
        viewModelScope.launch {
            _updateAvailable.value = checkUpdateUseCase(currentVersion)
        }
    }

    fun refreshOverlayState() {
        _overlayEnabled.value = getOverlaySettingsUseCase.isEnabled()
    }

    fun updateThemeSettings(settings: ThemeSettings) {
        viewModelScope.launch {
            themeSettingsUseCase.update(settings)
        }
    }

    fun startScan() {
        getAppleDevicesUseCase.startScan()
    }

    fun stopScan() {
        getAppleDevicesUseCase.stopScan()
    }
}
