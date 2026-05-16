package kurou.androidpods.core.designsystem

fun deviceImages(modelCode: Int): DeviceImages? = DEVICE_IMAGES[modelCode]

private val DEVICE_IMAGES: Map<Int, DeviceImages> =
    mapOf(
        0x0220 to
            DeviceImages.Tws(
                left = R.drawable.airpods_1st_gen_left,
                right = R.drawable.airpods_1st_gen_right,
                case = R.drawable.airpods_1st_gen_case,
            ),
        0x0F20 to
            DeviceImages.Tws(
                left = R.drawable.airpods_2nd_gen_left,
                right = R.drawable.airpods_2nd_gen_right,
                case = R.drawable.airpods_2nd_gen_case,
            ),
        0x1320 to
            DeviceImages.Tws(
                left = R.drawable.airpods_3rd_gen_left,
                right = R.drawable.airpods_3rd_gen_right,
                case = R.drawable.airpods_3rd_gen_case,
            ),
        0x1920 to
            DeviceImages.Tws(
                left = R.drawable.airpods_4th_gen_left,
                right = R.drawable.airpods_4th_gen_right,
                case = R.drawable.airpods_4th_gen_case,
            ),
        0x1B20 to
            DeviceImages.Tws(
                left = R.drawable.airpods_4th_gen_anc_left,
                right = R.drawable.airpods_4th_gen_anc_right,
                case = R.drawable.airpods_4th_gen_anc_case,
            ),
        0x0A20 to
            DeviceImages.Single(
                body = R.drawable.airpods_max_1st_gen,
            ),
        0x1F20 to
            DeviceImages.Single(
                body = R.drawable.airpods_max_1st_gen_usb_c,
            ),
        0x2D20 to
            DeviceImages.Single(
                body = R.drawable.airpods_max_2nd_gen,
            ),
        0x0E20 to
            DeviceImages.Tws(
                left = R.drawable.airpods_pro_1st_gen_left,
                right = R.drawable.airpods_pro_1st_gen_right,
                case = R.drawable.airpods_pro_1st_gen_case,
            ),
        0x1420 to
            DeviceImages.Tws(
                left = R.drawable.airpods_pro_2nd_gen_left,
                right = R.drawable.airpods_pro_2nd_gen_right,
                case = R.drawable.airpods_pro_2nd_gen_case,
            ),
        0x2420 to
            DeviceImages.Tws(
                left = R.drawable.airpods_pro_2nd_gen_usb_c_left,
                right = R.drawable.airpods_pro_2nd_gen_usb_c_right,
                case = R.drawable.airpods_pro_2nd_gen_usb_c_case,
            ),
        0x2720 to
            DeviceImages.Tws(
                left = R.drawable.airpods_pro_3rd_gen_left,
                right = R.drawable.airpods_pro_3rd_gen_right,
                case = R.drawable.airpods_pro_3rd_gen_case,
            ),
        // Beats - Single
        0x0520 to
            DeviceImages.Single(
                body = R.drawable.beats_x,
            ),
        0x0620 to
            DeviceImages.Single(
                body = R.drawable.beats_solo_3,
            ),
        0x0920 to
            DeviceImages.Single(
                body = R.drawable.beats_studio_3,
            ),
        0x1720 to
            DeviceImages.Single(
                body = R.drawable.beats_studio_pro,
            ),
        0x0320 to
            DeviceImages.Single(
                body = R.drawable.power_beats_3,
            ),
        0x0D20 to
            DeviceImages.Single(
                body = R.drawable.power_beats_4,
            ),
        0x0C20 to
            DeviceImages.Single(
                body = R.drawable.beats_solo_pro,
            ),
        0x1020 to
            DeviceImages.Single(
                body = R.drawable.beats_flex,
            ),
        // Beats - Tws
        0x1120 to
            DeviceImages.Tws(
                left = R.drawable.beats_studio_buds_left,
                right = R.drawable.beats_studio_buds_right,
                case = R.drawable.beats_studio_buds_case,
            ),
        0x1620 to
            DeviceImages.Tws(
                left = R.drawable.beats_studio_buds_plus_left,
                right = R.drawable.beats_studio_buds_plus_right,
                case = R.drawable.beats_studio_buds_plus_case,
            ),
        0x1220 to
            DeviceImages.Tws(
                left = R.drawable.beats_fit_pro_left,
                right = R.drawable.beats_fit_pro_right,
                case = R.drawable.beats_fit_pro_case,
            ),
        0x0B20 to
            DeviceImages.Tws(
                left = R.drawable.power_beats_pro_left,
                right = R.drawable.power_beats_pro_right,
                case = R.drawable.power_beats_pro_case,
            ),
    )
