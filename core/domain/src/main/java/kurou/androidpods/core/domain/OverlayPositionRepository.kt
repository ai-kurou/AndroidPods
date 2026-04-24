package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow

interface OverlayPositionRepository {
    fun observe(): Flow<OverlayPosition>
    suspend fun update(position: OverlayPosition)
}
