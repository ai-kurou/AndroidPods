package kurou.androidpods.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class OnboardingUiState(
    val showPermissionDeniedDialog: Boolean = false,
    val showBluetoothUnavailableDialog: Boolean = false,
    val showBluetoothDeniedDialog: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {
    private val _showPermissionDeniedDialog = MutableStateFlow(false)
    private val _showBluetoothUnavailableDialog = MutableStateFlow(false)
    private val _showBluetoothDeniedDialog = MutableStateFlow(false)

    val uiState: StateFlow<OnboardingUiState> =
        combine(
            _showPermissionDeniedDialog,
            _showBluetoothUnavailableDialog,
            _showBluetoothDeniedDialog,
        ) { permDenied, btUnavailable, btDenied ->
            OnboardingUiState(permDenied, btUnavailable, btDenied)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, OnboardingUiState())

    fun showPermissionDenied() { _showPermissionDeniedDialog.update { true } }
    fun dismissPermissionDenied() { _showPermissionDeniedDialog.update { false } }
    fun showBluetoothUnavailable() { _showBluetoothUnavailableDialog.update { true } }
    fun dismissBluetoothUnavailable() { _showBluetoothUnavailableDialog.update { false } }
    fun showBluetoothDenied() { _showBluetoothDeniedDialog.update { true } }
    fun dismissBluetoothDenied() { _showBluetoothDeniedDialog.update { false } }
}
