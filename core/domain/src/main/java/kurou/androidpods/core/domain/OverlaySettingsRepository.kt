package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow

interface OverlaySettingsRepository {
    fun isEnabled(): Boolean
    fun observe(): Flow<Boolean>
    fun refresh()
}
