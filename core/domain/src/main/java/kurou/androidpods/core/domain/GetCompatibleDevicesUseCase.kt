package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCompatibleDevicesUseCase @Inject constructor(
    private val repository: CompatibleDeviceRepository,
) {
    operator fun invoke(): Flow<List<CompatibleDevice>> = repository.getCompatibleDevices()
}
