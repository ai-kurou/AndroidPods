package kurou.androidpods.core.domain

import javax.inject.Inject

class GetOverlaySettingsUseCase @Inject constructor(
    private val repository: OverlaySettingsRepository,
) {
    fun isEnabled(): Boolean = repository.isEnabled()
}
