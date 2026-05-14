package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow

interface UnknownDeviceRepository {
    fun observeUnknownModelCodes(): Flow<Set<String>>
    suspend fun saveUnknownModelCode(modelCode: Int)
    suspend fun reportWithDeviceName(modelCode: Int, deviceName: String)
}
