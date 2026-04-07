package kurou.androidpods.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kurou.androidpods.feature.licenses.LicensesScreen
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
    var showLicenses by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = showLicenses) { showLicenses = false }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        when {
            showLicenses -> {
                LicensesScreen(
                    onBack = { showLicenses = false },
                    modifier = Modifier.padding(innerPadding),
                )
            }
            isFirstLaunch == true -> {
                OnboardingScreen(
                    onComplete = onOnboardingComplete,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            isFirstLaunch == false -> {
                SettingsScreen(
                    windowWidthSizeClass = windowWidthSizeClass,
                    onStartScanService = onStartScanService,
                    onStopScanService = onStopScanService,
                    onLicensesClick = { showLicenses = true },
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}
