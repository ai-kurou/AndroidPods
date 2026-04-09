package kurou.androidpods.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kurou.androidpods.feature.devices.DevicesScreen
import kurou.androidpods.feature.licenses.LicensesScreen
import kurou.androidpods.feature.onboarding.OnboardingScreen
import kurou.androidpods.feature.settings.SettingsScreen

private object Route {
    const val ONBOARDING = "onboarding"
    const val SETTINGS = "settings"
    const val LICENSES = "licenses"
    const val DEVICES = "devices"
}

@Composable
fun AppScaffold(
    isFirstLaunch: Boolean?,
    windowWidthSizeClass: WindowWidthSizeClass,
    onOnboardingComplete: () -> Unit,
    onStartScanService: () -> Unit,
    onStopScanService: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isFirstLaunch == null) return

    val navController = rememberNavController()
    val startDestination = if (isFirstLaunch) Route.ONBOARDING else Route.SETTINGS

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally { it } },
            exitTransition = { slideOutHorizontally { -it } },
            popEnterTransition = { slideInHorizontally { -it } },
            popExitTransition = { slideOutHorizontally { it } },
        ) {
            composable(Route.ONBOARDING) {
                OnboardingScreen(
                    onComplete = {
                        onOnboardingComplete()
                        navController.navigate(Route.SETTINGS) {
                            popUpTo(Route.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }
            composable(Route.SETTINGS) {
                SettingsScreen(
                    windowWidthSizeClass = windowWidthSizeClass,
                    onStartScanService = onStartScanService,
                    onStopScanService = onStopScanService,
                    onLicensesClick = { navController.navigate(Route.LICENSES) },
                    onDevicesClick = { navController.navigate(Route.DEVICES) },
                )
            }
            composable(Route.DEVICES) {
                DevicesScreen(
                    onBack = { navController.popBackStack() },
                    windowWidthSizeClass = windowWidthSizeClass,
                )
            }
            composable(Route.LICENSES) {
                LicensesScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
