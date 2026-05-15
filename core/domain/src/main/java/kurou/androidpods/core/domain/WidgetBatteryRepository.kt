package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow

interface WidgetBatteryRepository {
    fun observe(): Flow<WidgetBatteryState?>
    suspend fun save(device: AppleDevice)
}
