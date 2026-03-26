package kurou.androidpods.feature.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.GetAppleDevicesUseCase
import kurou.androidpods.core.domain.GetBluetoothAdapterStateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val getBluetoothAdapterStateUseCase: GetBluetoothAdapterStateUseCase,
    private val getAppleDevicesUseCase: GetAppleDevicesUseCase,
) : ViewModel() {

    private val _bluetoothAdapterState = MutableStateFlow(getBluetoothAdapterStateUseCase.current())
    val bluetoothAdapterState: StateFlow<Int?> = _bluetoothAdapterState.asStateFlow()

    private val _appleDevices = MutableStateFlow<Map<String, AppleDevice>>(emptyMap())
    val appleDevices: StateFlow<Map<String, AppleDevice>> = _appleDevices.asStateFlow()

    init {
        viewModelScope.launch {
            getBluetoothAdapterStateUseCase.observe().collect { state ->
                _bluetoothAdapterState.value = state
            }
        }
        viewModelScope.launch {
            getAppleDevicesUseCase.observe().collect { devices ->
                _appleDevices.value = devices
            }
        }
    }

    fun refreshBluetoothState() {
        _bluetoothAdapterState.value = getBluetoothAdapterStateUseCase.current()
    }

    fun startScan() {
        getAppleDevicesUseCase.startScan()
    }

    fun stopScan() {
        getAppleDevicesUseCase.stopScan()
    }
}
