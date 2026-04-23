package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow

interface BluetoothAdapterRepository {
    fun observeAdapterState(): Flow<Int?>

    fun getCurrentState(): Int?
}
