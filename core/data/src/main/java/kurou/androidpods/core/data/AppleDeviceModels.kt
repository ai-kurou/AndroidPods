package kurou.androidpods.core.data

internal val APPLE_DEVICE_MODELS: Map<Int, String> = mapOf(
    // AirPods
    0x0220 to "AirPods (1st Gen)",
    0x0F20 to "AirPods (2nd Gen)",
    0x1320 to "AirPods (3rd Gen)",
    0x1920 to "AirPods (4th Gen)",
    0x1B20 to "AirPods (4th Gen, ANC)",
    0x0A20 to "AirPods Max",
    0x1F20 to "AirPods Max (USB-C)",
    0x0E20 to "AirPods Pro",
    0x1420 to "AirPods Pro (2nd Gen)",
    0x2420 to "AirPods Pro (2nd Gen, USB-C)",
    // Beats
    0x0520 to "BeatsX",
    0x0620 to "Beats Solo3",
    0x0920 to "Beats Studio3",
    0x1720 to "Beats Studio Pro",
    0x1120 to "Beats Studio Buds",
    0x1620 to "Beats Studio Buds+",
    0x1220 to "Beats Fit Pro",
    0x0320 to "Powerbeats3",
    0x0B20 to "Powerbeats Pro",
    0x0C20 to "Beats Solo Pro",
    0x1020 to "Beats Flex",
)

internal val SINGLE_BATTERY_MODELS: Set<Int> = setOf(
    0x0A20, // AirPods Max
    0x1F20, // AirPods Max (USB-C)
    0x0520, // BeatsX
    0x1020, // Beats Flex
    0x0620, // Beats Solo3
    0x0920, // Beats Studio3
    0x1720, // Beats Studio Pro
    0x0320, // Powerbeats3
    0x0C20, // Beats Solo Pro
)

internal fun appleModelName(modelCode: Int): String =
    APPLE_DEVICE_MODELS[modelCode]
        ?: "Unknown Apple Device (0x${modelCode.toString(16).padStart(4, '0')})"
