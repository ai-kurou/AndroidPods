package kurou.androidpods

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import kurou.androidpods.feature.devices.DevicesScreen
import dagger.hilt.android.AndroidEntryPoint
import kurou.androidpods.ui.theme.AndroidPodsTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidPodsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DevicesScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
