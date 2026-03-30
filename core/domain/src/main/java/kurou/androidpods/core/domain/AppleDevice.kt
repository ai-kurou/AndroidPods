package kurou.androidpods.core.domain

data class AppleDevice(
    val address: String,
    val modelName: String,
    val modelCode: Int,
    val rssi: Int,
    val leftBattery: Int?,
    val rightBattery: Int?,
    val caseBattery: Int?,
    val isSingle: Boolean = false,
    val leftCharging: Boolean = false,
    val rightCharging: Boolean = false,
    val caseCharging: Boolean = false,
    val lidOpen: Boolean = false,
    val lidOpenCounter: Int = 0,
    val deviceColor: Int = 0,
    val colorName: String = "",
    val images: DeviceImages? = null,
)
