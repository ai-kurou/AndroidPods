package kurou.androidpods

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kurou.androidpods.core.service.DeviceScanService
import kurou.androidpods.navigation.AppNavHost
import kurou.androidpods.navigation.AppScaffold
import kurou.androidpods.navigation.Route
import kurou.androidpods.ui.theme.AndroidPodsTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidPodsTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val isFirstLaunch by viewModel.isFirstLaunch.collectAsStateWithLifecycle()

                when (isFirstLaunch) {
                    true -> {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            val navController = rememberNavController()
                            AppNavHost(
                                navController = navController,
                                startDestination = Route.ONBOARDING,
                                onOnboardingComplete = { viewModel.markAsLaunched() },
                                onStartScanService = {
                                    DeviceScanService.start(this@MainActivity)
                                },
                                onStopScanService = {
                                    DeviceScanService.stop(this@MainActivity)
                                },
                                modifier = Modifier.padding(innerPadding),
                            )
                        }
                    }
                    false -> {
                        AppScaffold(
                            onStartScanService = {
                                DeviceScanService.start(this@MainActivity)
                            },
                            onStopScanService = {
                                DeviceScanService.stop(this@MainActivity)
                            },
                        )
                    }
                    null -> {}
                }
            }
        }
    }
}
