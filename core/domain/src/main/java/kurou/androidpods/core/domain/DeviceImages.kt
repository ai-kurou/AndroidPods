package kurou.androidpods.core.domain

import androidx.annotation.DrawableRes

sealed interface DeviceImages {
    data class Tws(
        @DrawableRes val left: Int,
        @DrawableRes val right: Int,
        @DrawableRes val case: Int,
    ) : DeviceImages

    data class Single(
        @DrawableRes val body: Int,
    ) : DeviceImages
}
