package kurou.androidpods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kurou.androidpods.core.domain.FirstLaunchUseCase
import kurou.androidpods.core.domain.ThemeSettings
import kurou.androidpods.core.domain.ThemeSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val isFirstLaunch: Boolean? = null,
    val themeSettings: ThemeSettings = ThemeSettings(),
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firstLaunchUseCase: FirstLaunchUseCase,
    private val themeSettingsUseCase: ThemeSettingsUseCase,
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = combine(
        firstLaunchUseCase.observe(),
        themeSettingsUseCase.observe(),
    ) { isFirstLaunch, themeSettings ->
        MainUiState(isFirstLaunch, themeSettings)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState(),
    )

    fun markAsLaunched() {
        viewModelScope.launch {
            firstLaunchUseCase.markAsLaunched()
        }
    }
}
