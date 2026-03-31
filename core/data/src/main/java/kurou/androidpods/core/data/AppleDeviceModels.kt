package kurou.androidpods.core.data

import kurou.androidpods.core.domain.DeviceImages

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
    0x2720 to "AirPods Pro (3rd Gen)",
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

private val APPLE_DEVICE_COLORS: Map<Int, String> = mapOf(
    0x00 to "White",
    0x01 to "Black",
    0x02 to "Red",
    0x03 to "Blue",
    0x04 to "Pink",
    0x05 to "Gray",
    0x06 to "Silver",
    0x07 to "Gold",
    0x08 to "Rose Gold",
    0x09 to "Space Gray",
    0x0A to "Dark Blue",
    0x0B to "Light Blue",
    0x0C to "Yellow",
)

internal fun appleDeviceColorName(colorCode: Int): String =
    APPLE_DEVICE_COLORS[colorCode] ?: "Unknown (0x${colorCode.toString(16).padStart(2, '0')})"

internal fun appleDeviceImages(modelCode: Int): DeviceImages? =
    APPLE_DEVICE_IMAGES[modelCode]

private val APPLE_DEVICE_IMAGES: Map<Int, DeviceImages> = mapOf(
    0x0220 to DeviceImages.Tws(
        left = R.mipmap.airpods_1st_gen_left,
        right = R.mipmap.airpods_1st_gen_right,
        case = R.mipmap.airpods_1st_gen_case,
    ),
    0x0F20 to DeviceImages.Tws(
        left = R.mipmap.airpods_2nd_gen_left,
        right = R.mipmap.airpods_2nd_gen_right,
        case = R.mipmap.airpods_2nd_gen_case,
    ),
    0x1320 to DeviceImages.Tws(
        left = R.mipmap.airpods_3rd_gen_left,
        right = R.mipmap.airpods_3rd_gen_right,
        case = R.mipmap.airpods_3rd_gen_case,
    ),
    0x1920 to DeviceImages.Tws(
        left = R.mipmap.airpods_4th_gen_left,
        right = R.mipmap.airpods_4th_gen_right,
        case = R.mipmap.airpods_4th_gen_case,
    ),
    0x1B20 to DeviceImages.Tws(
        left = R.mipmap.airpods_4th_gen_anc_left,
        right = R.mipmap.airpods_4th_gen_anc_right,
        case = R.mipmap.airpods_4th_gen_anc_case,
    ),
    0x0A20 to DeviceImages.Single(
        body = R.mipmap.airpods_max_1st_gen,
    ),
    0x1F20 to DeviceImages.Single(
        body = R.mipmap.airpods_max_1st_gen_usb_c,
    ),
    0x0E20 to DeviceImages.Tws(
        left = R.mipmap.airpods_pro_1st_gen_left,
        right = R.mipmap.airpods_pro_1st_gen_right,
        case = R.mipmap.airpods_pro_1st_gen_case,
    ),
)
