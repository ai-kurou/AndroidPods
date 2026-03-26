package kurou.androidpods.core.domain

data class AppleDevice(
    val address: String,
    val modelName: String,
    val modelCode: Int,
    val rssi: Int,
    val leftBattery: Int?,
    val rightBattery: Int?,
    val caseBattery: Int?,
)
