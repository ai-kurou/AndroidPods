package kurou.androidpods.core.data

import kurou.androidpods.core.domain.CompatibleDevice
import kurou.androidpods.core.domain.CompatibleDeviceRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CompatibleDeviceRepositoryImpl @Inject constructor() : CompatibleDeviceRepository {
    override fun getCompatibleDevices(): List<CompatibleDevice> =
        APPLE_DEVICE_MODELS.map { (code, name) ->
            CompatibleDevice(name = name, images = appleDeviceImages(code))
        }
}
