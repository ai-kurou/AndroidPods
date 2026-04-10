package kurou.androidpods.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.GetAppleDevicesUseCase
import kurou.androidpods.core.domain.GetBluetoothAdapterStateUseCase
import kurou.androidpods.core.domain.GetOverlaySettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SettingsUiState(
    val bluetoothAdapterState: Int? = null,
    val appleDevices: Map<String, AppleDevice> = emptyMap(),
    val overlayEnabled: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getBluetoothAdapterStateUseCase: GetBluetoothAdapterStateUseCase,
    private val getAppleDevicesUseCase: GetAppleDevicesUseCase,
    private val getOverlaySettingsUseCase: GetOverlaySettingsUseCase,
) : ViewModel() {

    private val _overlayEnabled = MutableStateFlow(getOverlaySettingsUseCase.isEnabled())

    val uiState: StateFlow<SettingsUiState> = combine(
        getBluetoothAdapterStateUseCase.observe(),
        getAppleDevicesUseCase.observe(),
        _overlayEnabled,
    ) { bluetoothAdapterState, appleDevices, overlayEnabled ->
        SettingsUiState(bluetoothAdapterState, appleDevices, overlayEnabled)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState(
            bluetoothAdapterState = getBluetoothAdapterStateUseCase.current(),
            overlayEnabled = getOverlaySettingsUseCase.isEnabled(),
        ),
    )

    fun refreshOverlayState() {
        _overlayEnabled.value = getOverlaySettingsUseCase.isEnabled()
    }

    fun startScan() {
        getAppleDevicesUseCase.startScan()
    }

    fun stopScan() {
        getAppleDevicesUseCase.stopScan()
    }
}
