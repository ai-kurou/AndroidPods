package kurou.androidpods.feature.widget

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kurou.androidpods.core.domain.WidgetBatteryUseCase

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun widgetBatteryUseCase(): WidgetBatteryUseCase
}
