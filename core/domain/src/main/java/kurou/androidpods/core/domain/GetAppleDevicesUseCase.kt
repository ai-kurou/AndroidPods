package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetAppleDevicesUseCase @Inject constructor(
    private val repository: AppleDeviceRepository,
    private val rssiThresholdRepository: RssiThresholdRepository,
) {
    fun observe(): Flow<Map<String, AppleDevice>> =
        combine(repository.observeDevices(), rssiThresholdRepository.observe()) { devices, threshold ->
            if (threshold == RssiThreshold.ALL) {
                devices
            } else {
                devices.filter { (_, device) -> device.rssi >= threshold.minRssi }
            }
        }

    fun startScan() = repository.startScan()

    fun stopScan() = repository.stopScan()
}
