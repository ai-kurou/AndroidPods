package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow

interface CompatibleDeviceRepository {
    fun getCompatibleDevices(): Flow<List<CompatibleDevice>>
}
