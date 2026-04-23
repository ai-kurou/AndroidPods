package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow

interface AppleDeviceRepository {
    fun observeDevices(): Flow<Map<String, AppleDevice>>

    fun startScan()

    fun stopScan()
}
