package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RssiThresholdUseCase @Inject constructor(
    private val repository: RssiThresholdRepository,
) {
    fun observe(): Flow<RssiThreshold> = repository.observe()

    suspend fun update(threshold: RssiThreshold) = repository.update(threshold)
}
