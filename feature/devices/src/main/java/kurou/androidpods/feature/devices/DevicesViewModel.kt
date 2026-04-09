package kurou.androidpods.feature.devices

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kurou.androidpods.core.domain.CompatibleDevice
import kurou.androidpods.core.domain.GetCompatibleDevicesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    getCompatibleDevices: GetCompatibleDevicesUseCase,
) : ViewModel() {
    val devices: StateFlow<List<CompatibleDevice>> =
        MutableStateFlow(getCompatibleDevices()).asStateFlow()
}
