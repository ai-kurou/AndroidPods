package kurou.androidpods

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kurou.androidpods.ui.theme.AndroidPodsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidPodsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BluetoothPermissionScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

private fun requiredPermissions(): List<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

@Composable
fun BluetoothPermissionScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val permissions = requiredPermissions()

    val permissionStates = remember { mutableStateMapOf<String, Boolean>() }

    // 初期状態を設定
    if (permissionStates.isEmpty()) {
        permissions.forEach { permission ->
            permissionStates[permission] =
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        results.forEach { (permission, granted) ->
            permissionStates[permission] = granted
        }
    }

    // 起動時に未許可の権限をリクエスト
    LaunchedEffect(Unit) {
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            launcher.launch(notGranted.toTypedArray())
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        permissionStates.forEach { (permission, granted) ->
            val shortName = permission.substringAfterLast(".")
            val status = if (granted) "許可済み" else "未許可"
            Text(text = "$shortName: $status")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BluetoothPermissionScreenPreview() {
    AndroidPodsTheme {
        BluetoothPermissionScreen()
    }
}
