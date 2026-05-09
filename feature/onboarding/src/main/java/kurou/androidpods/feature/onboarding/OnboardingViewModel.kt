package kurou.androidpods.feature.onboarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class OnboardingUiState(
    val showPermissionDeniedDialog: Boolean = false,
    val showBluetoothUnavailableDialog: Boolean = false,
    val showBluetoothDeniedDialog: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun showPermissionDenied() { _uiState.update { it.copy(showPermissionDeniedDialog = true) } }
    fun dismissPermissionDenied() { _uiState.update { it.copy(showPermissionDeniedDialog = false) } }
    fun showBluetoothUnavailable() { _uiState.update { it.copy(showBluetoothUnavailableDialog = true) } }
    fun dismissBluetoothUnavailable() { _uiState.update { it.copy(showBluetoothUnavailableDialog = false) } }
    fun showBluetoothDenied() { _uiState.update { it.copy(showBluetoothDeniedDialog = true) } }
    fun dismissBluetoothDenied() { _uiState.update { it.copy(showBluetoothDeniedDialog = false) } }
}
