package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow

interface OverlaySettingsRepository {
    fun isEnabled(): Boolean
    fun hasPermission(): Boolean
    fun observe(): Flow<Boolean>
    suspend fun setEnabled(enabled: Boolean)
    fun refresh()
}
