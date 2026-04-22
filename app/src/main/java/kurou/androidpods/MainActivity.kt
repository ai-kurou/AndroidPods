package kurou.androidpods

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
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
            val isFirstLaunch by viewModel.isFirstLaunch.collectAsStateWithLifecycle()
            val themeSettings by viewModel.themeSettings.collectAsStateWithLifecycle()
            val darkTheme = when (themeSettings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            AndroidPodsTheme(
                darkTheme = darkTheme,
                dynamicColor = themeSettings.useDynamicColor,
            ) {
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
