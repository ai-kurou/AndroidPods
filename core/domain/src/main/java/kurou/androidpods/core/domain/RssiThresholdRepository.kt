package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow

interface RssiThresholdRepository {
    fun observe(): Flow<RssiThreshold>

    suspend fun update(threshold: RssiThreshold)
}
