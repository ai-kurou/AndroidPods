package kurou.androidpods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kurou.androidpods.core.domain.FirstLaunchUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firstLaunchUseCase: FirstLaunchUseCase,
) : ViewModel() {

    val isFirstLaunch: StateFlow<Boolean?> = firstLaunchUseCase.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun markAsLaunched() {
        viewModelScope.launch {
            firstLaunchUseCase.markAsLaunched()
        }
    }
}
