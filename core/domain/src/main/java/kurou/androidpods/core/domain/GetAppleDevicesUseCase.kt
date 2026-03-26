package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppleDevicesUseCase @Inject constructor(
    private val repository: AppleDeviceRepository,
) {
    fun observe(): Flow<Map<String, AppleDevice>> = repository.observeDevices()
    fun startScan() = repository.startScan()
    fun stopScan() = repository.stopScan()
}
