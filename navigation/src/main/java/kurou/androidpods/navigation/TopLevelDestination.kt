package kurou.androidpods.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    @StringRes val labelResId: Int,
) {
    DEVICES(
        route = Route.DEVICES,
        icon = Icons.Default.Search,
        labelResId = R.string.nav_label_devices,
    ),
}
