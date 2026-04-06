package kurou.androidpods.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.androidpods.feature.onboarding.OnboardingScreen
import kurou.androidpods.feature.settings.SettingsScreen

@Composable
fun AppScaffold(
    isFirstLaunch: Boolean?,
    windowWidthSizeClass: WindowWidthSizeClass,
    onOnboardingComplete: () -> Unit,
    onStartScanService: () -> Unit,
    onStopScanService: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { _ ->
        when (isFirstLaunch) {
            true -> {
                OnboardingScreen(
                    onComplete = onOnboardingComplete,
                )
            }
            false -> {
                SettingsScreen(
                    windowWidthSizeClass = windowWidthSizeClass,
                    onStartScanService = onStartScanService,
                    onStopScanService = onStopScanService,
                )
            }
            null -> {}
        }
    }
}
