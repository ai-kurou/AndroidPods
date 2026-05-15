package kurou.androidpods.core.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WidgetBatteryUseCase @Inject constructor(
    private val repository: WidgetBatteryRepository,
) {
    fun observe(): Flow<WidgetBatteryState?> = repository.observe()

    suspend fun save(device: AppleDevice) = repository.save(device)
}
