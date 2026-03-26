package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FirstLaunchUseCase @Inject constructor(
    private val repository: FirstLaunchRepository,
) {
    fun observe(): Flow<Boolean> = repository.observeIsFirstLaunch()

    suspend fun markAsLaunched() = repository.markAsLaunched()
}
