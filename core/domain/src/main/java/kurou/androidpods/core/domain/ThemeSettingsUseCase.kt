package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ThemeSettingsUseCase @Inject constructor(
    private val repository: ThemeSettingsRepository,
) {
    fun observe(): Flow<ThemeSettings> = repository.observe()
    suspend fun update(settings: ThemeSettings) = repository.update(settings)
}
