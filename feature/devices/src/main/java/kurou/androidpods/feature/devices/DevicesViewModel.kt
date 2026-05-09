package kurou.androidpods.feature.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kurou.androidpods.core.domain.CompatibleDevice
import kurou.androidpods.core.domain.GetCompatibleDevicesUseCase
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    getCompatibleDevicesUseCase: GetCompatibleDevicesUseCase,
) : ViewModel() {
    val devices: StateFlow<List<CompatibleDevice>> =
        getCompatibleDevicesUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )
}
