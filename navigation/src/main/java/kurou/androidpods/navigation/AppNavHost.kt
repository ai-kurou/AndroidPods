package kurou.androidpods.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kurou.androidpods.feature.devices.DevicesScreen
import kurou.androidpods.feature.onboarding.OnboardingScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    onOnboardingComplete: () -> Unit,
    onStartScanService: () -> Unit,
    onStopScanService: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Route.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    onOnboardingComplete()
                    navController.navigate(Route.DEVICES) {
                        popUpTo(Route.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
        composable(Route.DEVICES) {
            DevicesScreen(
                onStartScanService = onStartScanService,
                onStopScanService = onStopScanService,
            )
        }
    }
}
