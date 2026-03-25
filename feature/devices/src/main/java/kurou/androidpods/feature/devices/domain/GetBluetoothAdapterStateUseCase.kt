package kurou.androidpods.feature.devices.domain

import kurou.androidpods.feature.devices.data.BluetoothAdapterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBluetoothAdapterStateUseCase @Inject constructor(
    private val repository: BluetoothAdapterRepository,
) {
    fun observe(): Flow<Int?> = repository.observeAdapterState()

    fun current(): Int? = repository.getCurrentState()
}
