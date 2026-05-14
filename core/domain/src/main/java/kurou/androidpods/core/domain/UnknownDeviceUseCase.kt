package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UnknownDeviceUseCase @Inject constructor(
    private val repository: UnknownDeviceRepository,
) {
    fun observe(): Flow<Set<String>> = repository.observeUnknownModelCodes()

    suspend fun save(modelCode: Int) = repository.saveUnknownModelCode(modelCode)

    suspend fun report(modelCode: Int, deviceName: String) =
        repository.reportWithDeviceName(modelCode, deviceName)
}
