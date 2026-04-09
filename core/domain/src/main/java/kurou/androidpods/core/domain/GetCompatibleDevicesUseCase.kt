package kurou.androidpods.core.domain

import javax.inject.Inject

class GetCompatibleDevicesUseCase @Inject constructor(
    private val repository: CompatibleDeviceRepository,
) {
    operator fun invoke(): List<CompatibleDevice> = repository.getCompatibleDevices()
}
