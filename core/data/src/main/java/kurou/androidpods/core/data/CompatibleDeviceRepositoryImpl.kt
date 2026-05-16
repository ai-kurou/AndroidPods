package kurou.androidpods.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kurou.androidpods.core.domain.CompatibleDevice
import kurou.androidpods.core.domain.CompatibleDeviceRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CompatibleDeviceRepositoryImpl @Inject constructor() : CompatibleDeviceRepository {
    override fun getCompatibleDevices(): Flow<List<CompatibleDevice>> =
        flowOf(
            APPLE_DEVICE_MODELS.map { (code, name) ->
                CompatibleDevice(name = name, modelCode = code)
            },
        )
}
