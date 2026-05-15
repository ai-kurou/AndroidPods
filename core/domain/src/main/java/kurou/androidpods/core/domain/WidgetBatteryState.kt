package kurou.androidpods.core.domain

data class WidgetBatteryState(
    val deviceName: String,
    val modelCode: Int,
    val leftBattery: Int?,
    val rightBattery: Int?,
    val caseBattery: Int?,
    val leftCharging: Boolean,
    val rightCharging: Boolean,
    val caseCharging: Boolean,
    val isSingle: Boolean,
    val recordedAt: Long,
)
