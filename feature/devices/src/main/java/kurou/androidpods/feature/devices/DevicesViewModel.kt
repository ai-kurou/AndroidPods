package kurou.androidpods.feature.devices

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kurou.androidpods.core.domain.CompatibleDevice
import kurou.androidpods.core.domain.GetCompatibleDevicesUseCase
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    getCompatibleDevicesUseCase: GetCompatibleDevicesUseCase,
) : ViewModel() {
    val devices: StateFlow<List<CompatibleDevice>> =
        MutableStateFlow(getCompatibleDevicesUseCase()).asStateFlow()
}
