package kurou.androidpods

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kurou.androidpods.core.domain.ThemeMode
import kurou.androidpods.core.service.DeviceScanService
import kurou.androidpods.navigation.AppScaffold
import kurou.androidpods.ui.theme.AndroidPodsTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val uiState = viewModel.uiState.collectAsStateWithLifecycle()
            val isFirstLaunch = uiState.value.isFirstLaunch ?: return@setContent
            val themeSettings = uiState.value.themeSettings
            val darkTheme =
                when (themeSettings.themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }
            AndroidPodsTheme(
                darkTheme = darkTheme,
                dynamicColor = themeSettings.useDynamicColor,
            ) {
                val window = this@MainActivity.window
                androidx.compose.runtime.SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }

                val windowSizeClass = calculateWindowSizeClass(this@MainActivity)

                AppScaffold(
                    isFirstLaunch = isFirstLaunch,
                    windowWidthSizeClass = windowSizeClass.widthSizeClass,
                    onOnboardingComplete = { viewModel.markAsLaunched() },
                    onStartScanService = { DeviceScanService.start(this@MainActivity) },
                    onStopScanService = { DeviceScanService.stop(this@MainActivity) },
                )
            }
        }
    }
}
