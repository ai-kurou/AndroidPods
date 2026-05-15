package kurou.androidpods.core.domain

data class WidgetBatteryState(
    val deviceName: String,
    val leftBattery: Int?,
    val rightBattery: Int?,
    val caseBattery: Int?,
    val isSingle: Boolean,
    val recordedAt: Long,
)
