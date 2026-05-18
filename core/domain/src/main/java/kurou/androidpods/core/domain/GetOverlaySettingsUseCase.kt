package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOverlaySettingsUseCase @Inject constructor(
    private val repository: OverlaySettingsRepository,
) {
    fun observe(): Flow<Boolean> = repository.observe()
    fun hasPermission(): Boolean = repository.hasPermission()
    suspend fun setEnabled(enabled: Boolean) = repository.setEnabled(enabled)
    fun refresh() = repository.refresh()
}
