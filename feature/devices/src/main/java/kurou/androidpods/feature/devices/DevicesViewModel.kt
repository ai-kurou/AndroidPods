package kurou.androidpods.feature.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kurou.androidpods.feature.devices.domain.GetBluetoothAdapterStateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val getBluetoothAdapterStateUseCase: GetBluetoothAdapterStateUseCase,
) : ViewModel() {

    private val _bluetoothAdapterState = MutableStateFlow(getBluetoothAdapterStateUseCase.current())
    val bluetoothAdapterState: StateFlow<Int?> = _bluetoothAdapterState.asStateFlow()

    init {
        viewModelScope.launch {
            getBluetoothAdapterStateUseCase.observe().collect { state ->
                _bluetoothAdapterState.value = state
            }
        }
    }

    fun refreshBluetoothState() {
        _bluetoothAdapterState.value = getBluetoothAdapterStateUseCase.current()
    }
}
