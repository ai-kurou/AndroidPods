package kurou.androidpods.feature.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kurou.androidpods.core.domain.ThemeMode

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
    var showThemeModeDialog by remember { mutableStateOf(false) }
    var showOverlayPositionDialog by remember { mutableStateOf(false) }
    var initialRequestDone by remember { mutableStateOf(false) }
    var isServiceRestarting by remember { mutableStateOf(false) }

    // 初期状態を設定
    if (permissionStates.isEmpty()) {
        permissions.forEach { permission ->
            permissionStates[permission] =
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    val overlaySettingsLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) {
            viewModel.refreshOverlayState()
        }

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results ->
            results.forEach { (permission, granted) ->
                permissionStates[permission] = granted
            }
            initialRequestDone = true
        }

    SettingsEffects(
        permissions = permissions,
        initialRequestDone = initialRequestDone,
        onLaunchPermissions = { launcher.launch(it) },
        onUpdatePermissionState = { permission, granted -> permissionStates[permission] = granted },
        onShowSettingsDialog = { showSettingsDialog = true },
        onStartScanService = onStartScanService,
        viewModel = viewModel,
    )

    // 設定画面への誘導ダイアログ
    if (showSettingsDialog) {
        PermissionRequiredDialog(
            onDismiss = { showSettingsDialog = false },
            onConfirm = {
                showSettingsDialog = false
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                context.startActivity(intent)
            },
        )
    }

    if (showThemeModeDialog) {
        ThemeModeDialog(
            currentMode = uiState.themeSettings.themeMode,
            onDismiss = { showThemeModeDialog = false },
            onModeSelected = { mode ->
                viewModel.updateThemeSettings(uiState.themeSettings.copy(themeMode = mode))
                showThemeModeDialog = false
            },
        )
    }

    if (showOverlayPositionDialog) {
        OverlayPositionDialog(
            currentPosition = uiState.overlayPosition,
            onDismiss = { showOverlayPositionDialog = false },
            onPositionSelected = { position ->
                viewModel.updateOverlayPosition(position)
                showOverlayPositionDialog = false
            },
        )
    }

    val columns =
        when (windowWidthSizeClass) {
            WindowWidthSizeClass.Compact -> 1
            WindowWidthSizeClass.Medium -> 2
            else -> 3
        }

    val restartServiceMessage = stringResource(R.string.restart_service_completed)

    SettingsScaffold(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        permissionStates = permissionStates,
        uiState = uiState,
        isServiceRestarting = isServiceRestarting,
        columns = columns,
        onPermissionWarningClick = {
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
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
            val intent =
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${context.packageName}".toUri(),
                )
            overlaySettingsLauncher.launch(intent)
        },
        onRestartServiceClick = {
            scope.launch {
                isServiceRestarting = true
                onStopScanService()
                onStartScanService()
                delay(5000)
                isServiceRestarting = false
                snackbarHostState.showSnackbar(restartServiceMessage)
            }
        },
        onThemeModeClick = { showThemeModeDialog = true },
        onDynamicColorToggle = { enabled ->
            viewModel.updateThemeSettings(uiState.themeSettings.copy(useDynamicColor = enabled))
        },
        onOverlayPositionClick = { showOverlayPositionDialog = true },
    )
}

@Composable
private fun SettingsEffects(
    permissions: List<String>,
    initialRequestDone: Boolean,
    onLaunchPermissions: (Array<String>) -> Unit,
    onUpdatePermissionState: (String, Boolean) -> Unit,
    onShowSettingsDialog: () -> Unit,
    onStartScanService: () -> Unit,
    viewModel: SettingsViewModel,
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val notGranted =
            permissions.filter {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }
        if (notGranted.isNotEmpty()) onLaunchPermissions(notGranted.toTypedArray())
    }

    LaunchedEffect(Unit) {
        @Suppress("DEPRECATION")
        val versionName =
            runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            }.getOrNull() ?: return@LaunchedEffect
        viewModel.checkUpdate(versionName)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        permissions.forEach { permission ->
            onUpdatePermissionState(
                permission,
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED,
            )
        }
        viewModel.refreshOverlayState()
        onStartScanService()
        if (initialRequestDone) {
            val hasNotGranted =
                permissions.any {
                    ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                }
            if (hasNotGranted) onShowSettingsDialog()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScaffold(
    snackbarHostState: SnackbarHostState,
    permissionStates: Map<String, Boolean>,
    uiState: SettingsUiState,
    isServiceRestarting: Boolean,
    columns: Int,
    onPermissionWarningClick: () -> Unit,
    onBluetoothWarningClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onDevicesClick: () -> Unit,
    onGithubClick: () -> Unit,
    onOverlayToggle: (Boolean) -> Unit,
    onOverlayPositionClick: () -> Unit,
    onRestartServiceClick: () -> Unit,
    onThemeModeClick: () -> Unit,
    onDynamicColorToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
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
            overlayPosition = uiState.overlayPosition,
            updateAvailable = uiState.updateAvailable,
            isServiceRestarting = isServiceRestarting,
            columns = columns,
            themeSettings = uiState.themeSettings,
            onPermissionWarningClick = onPermissionWarningClick,
            onBluetoothWarningClick = onBluetoothWarningClick,
            onUpdateClick = onUpdateClick,
            onLicensesClick = onLicensesClick,
            onDevicesClick = onDevicesClick,
            onGithubClick = onGithubClick,
            onOverlayToggle = onOverlayToggle,
            onOverlayPositionClick = onOverlayPositionClick,
            onRestartServiceClick = onRestartServiceClick,
            onThemeModeClick = onThemeModeClick,
            onDynamicColorToggle = onDynamicColorToggle,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun ThemeModeDialog(
    currentMode: ThemeMode,
    onDismiss: () -> Unit,
    onModeSelected: (ThemeMode) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.theme_mode_label)) },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onModeSelected(mode) }
                                .padding(vertical = 4.dp),
                    ) {
                        RadioButton(
                            selected = mode == currentMode,
                            onClick = { onModeSelected(mode) },
                        )
                        Text(stringResource(mode.toStringRes()))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}
