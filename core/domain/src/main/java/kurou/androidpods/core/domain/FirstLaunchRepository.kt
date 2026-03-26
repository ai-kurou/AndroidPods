package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow

interface FirstLaunchRepository {
    fun observeIsFirstLaunch(): Flow<Boolean>
    suspend fun markAsLaunched()
}
