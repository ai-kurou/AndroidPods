package kurou.androidpods.feature.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    windowWidthSizeClass: WindowWidthSizeClass,
    onStartScanService: () -> Unit,
    onStopScanService: () -> Unit,
    onLicensesClick: () -> Unit,
    onDevicesClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val permissions = requiredPermissions()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionStates = remember { mutableStateMapOf<String, Boolean>() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSettingsDialog by remember { mutableStateOf(false) }
    var initialRequestDone by remember { mutableStateOf(false) }

    // 初期状態を設定
    if (permissionStates.isEmpty()) {
        permissions.forEach { permission ->
            permissionStates[permission] =
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    val overlaySettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.refreshOverlayState()
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

    // 起動時にアップデート確認
    LaunchedEffect(Unit) {
        @Suppress("DEPRECATION")
        val versionName = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: return@LaunchedEffect
        viewModel.checkUpdate(versionName)
    }

    // アプリ復帰時（ON_RESUME）にスキャン開始、権限状態とアダプタ状態を再チェック
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        permissions.forEach { permission ->
            permissionStates[permission] =
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        viewModel.refreshOverlayState()
        onStartScanService()
        if (initialRequestDone) {
            val hasNotGranted = permissions.any {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }
            if (hasNotGranted) showSettingsDialog = true
        }
    }

    // 設定画面への誘導ダイアログ
    if (showSettingsDialog) {
        PermissionRequiredDialog(
            onDismiss = { showSettingsDialog = false },
            onConfirm = {
                showSettingsDialog = false
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            },
        )
    }

    val columns = when (windowWidthSizeClass) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium -> 2
        else -> 3
    }

    val restartServiceMessage = stringResource(R.string.restart_service_completed)

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
            )
        },
    ) { innerPadding ->
        SettingsContent(
            permissionStates = permissionStates,
            bluetoothAdapterState = uiState.bluetoothAdapterState,
            overlayEnabled = uiState.overlayEnabled,
            updateAvailable = uiState.updateAvailable,
            columns = columns,
            onPermissionWarningClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            },
            onBluetoothWarningClick = {
                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            },
            onUpdateClick = {
                val intent = Intent(Intent.ACTION_VIEW, "https://github.com/ai-kurou/AndroidPods/releases/latest".toUri())
                context.startActivity(intent)
            },
            onLicensesClick = onLicensesClick,
            onDevicesClick = onDevicesClick,
            onGithubClick = {
                val intent = Intent(Intent.ACTION_VIEW, "https://github.com/ai-kurou/AndroidPods".toUri())
                context.startActivity(intent)
            },
            onOverlayToggle = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${context.packageName}".toUri(),
                )
                overlaySettingsLauncher.launch(intent)
            },
            onRestartServiceClick = {
                scope.launch {
                    onStopScanService()
                    onStartScanService()
                    snackbarHostState.showSnackbar(restartServiceMessage)
                }
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}
