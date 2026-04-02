package kurou.androidpods.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    @StringRes val labelResId: Int,
) {
    SETTINGS(
        route = Route.SETTINGS,
        icon = Icons.Default.Settings,
        labelResId = R.string.nav_label_settings,
    ),
}
