package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OverlayPositionUseCase @Inject constructor(
    private val repository: OverlayPositionRepository,
) {
    fun observe(): Flow<OverlayPosition> = repository.observe()

    suspend fun update(position: OverlayPosition) = repository.update(position)
}
