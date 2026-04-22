package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow

interface ThemeSettingsRepository {
    fun observe(): Flow<ThemeSettings>
    suspend fun update(settings: ThemeSettings)
}
