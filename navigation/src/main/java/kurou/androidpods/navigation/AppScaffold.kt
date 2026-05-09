package kurou.androidpods.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import kurou.androidpods.feature.devices.DevicesScreen
import kurou.androidpods.feature.licenses.LicensesScreen
import kurou.androidpods.feature.onboarding.OnboardingScreen
import kurou.androidpods.feature.settings.SettingsScreen

private sealed interface Route {
    @Serializable data object Onboarding : Route
    @Serializable data object Settings : Route
    @Serializable data object Licenses : Route
    @Serializable data object Devices : Route
}

@Composable
fun AppScaffold(
    isFirstLaunch: Boolean,
    onOnboardingComplete: () -> Unit,
    onStartScanService: () -> Unit,
    onStopScanService: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val startDestination: Route = if (isFirstLaunch) Route.Onboarding else Route.Settings

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
            composable<Route.Onboarding> {
                OnboardingScreen(
                    onComplete = {
                        onOnboardingComplete()
                        navController.navigate(Route.Settings) {
                            popUpTo<Route.Onboarding> { inclusive = true }
                        }
                    },
                )
            }
            composable<Route.Settings> {
                SettingsScreen(
                    windowSizeClass = windowSizeClass,
                    onStartScanService = onStartScanService,
                    onStopScanService = onStopScanService,
                    onLicensesClick = { navController.navigate(Route.Licenses) },
                    onDevicesClick = { navController.navigate(Route.Devices) },
                )
            }
            composable<Route.Devices> {
                DevicesScreen(
                    onBack = { navController.popBackStack() },
                    windowSizeClass = windowSizeClass,
                )
            }
            composable<Route.Licenses> {
                LicensesScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
