package kurou.androidpods.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kurou.androidpods.feature.settings.SettingsScreen

@Composable
fun AppScaffold(
    onStartScanService: () -> Unit,
    onStopScanService: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                item(
                    selected = destination == TopLevelDestination.SETTINGS,
                    onClick = {},
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = stringResource(destination.labelResId),
                        )
                    },
                    label = { Text(stringResource(destination.labelResId)) },
                )
            }
        },
    ) {
        SettingsScreen(
            onStartScanService = onStartScanService,
            onStopScanService = onStopScanService,
        )
    }
}

@Composable
private fun AppScaffoldPreviewContent(layoutType: NavigationSuiteType) {
    // AppScaffoldにしてしまうと実際のSettingsScreenが表示されてしまう(SettingsScreenに何が表示されるかはAppScaffoldの責務ではない)
    // ここではNavigationBar、NavigationRail、NavigationDrawerが表示されるだけを確認する。
    NavigationSuiteScaffold(
        layoutType = layoutType,
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                item(
                    selected = destination == TopLevelDestination.SETTINGS,
                    onClick = {},
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = stringResource(destination.labelResId),
                        )
                    },
                    label = { Text(stringResource(destination.labelResId)) },
                )
            }
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("Content Area")
        }
    }
}

@Preview(showBackground = true, name = "NavigationBar")
@Composable
private fun AppScaffoldPreviewNavigationBar() {
    AppScaffoldPreviewContent(NavigationSuiteType.NavigationBar)
}

@Preview(showBackground = true, name = "NavigationRail", widthDp = 700)
@Composable
private fun AppScaffoldPreviewNavigationRail() {
    AppScaffoldPreviewContent(NavigationSuiteType.NavigationRail)
}

@Preview(showBackground = true, name = "NavigationDrawer", widthDp = 900)
@Composable
private fun AppScaffoldPreviewNavigationDrawer() {
    AppScaffoldPreviewContent(NavigationSuiteType.NavigationDrawer)
}
