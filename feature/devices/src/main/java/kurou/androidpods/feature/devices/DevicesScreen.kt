package kurou.androidpods.feature.devices

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
fun DevicesScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val permissions = requiredPermissions()

    val permissionStates = remember { mutableStateMapOf<String, Boolean>() }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var initialRequestDone by remember { mutableStateOf(false) }

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
        initialRequestDone = true
    }

    // 起動時に未許可の権限をリクエスト
    LaunchedEffect(Unit) {
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) launcher.launch(notGranted.toTypedArray())
    }

    // アプリ復帰時（ON_RESUME）に権限状態を再チェックし、未許可なら設定画面へ誘導
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        permissions.forEach { permission ->
            permissionStates[permission] =
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        if (initialRequestDone) {
            val hasNotGranted = permissions.any {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }
            if (hasNotGranted) showSettingsDialog = true
        }
    }

    // 設定画面への誘導ダイアログ
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text(stringResource(R.string.bluetooth_permission_required_title)) },
            text = { Text(stringResource(R.string.bluetooth_permission_required_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text(stringResource(R.string.open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Column(modifier = modifier.padding(16.dp)) {
        permissionStates.forEach { (permission, granted) ->
            val shortName = permission.substringAfterLast(".")
            val status = if (granted) stringResource(R.string.permission_granted) else stringResource(R.string.permission_not_granted)
            Text(text = "$shortName: $status")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DevicesScreenPreview() {
    DevicesScreen()
}
