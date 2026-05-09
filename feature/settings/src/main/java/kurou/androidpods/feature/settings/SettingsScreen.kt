package kurou.androidpods.feature.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import kurou.androidpods.core.domain.NotificationChannels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    windowSizeClass: WindowSizeClass,
    onStartScanService: () -> Unit,
    onStopScanService: () -> Unit,
    onLicensesClick: () -> Unit,
    onDevicesClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val permissions = requiredPermissions()
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showThemeModeDialog by remember { mutableStateOf(false) }
    var showOverlayPositionDialog by remember { mutableStateOf(false) }
    var initialRequestDone by remember { mutableStateOf(false) }

    val restartServiceMessage = stringResource(R.string.restart_service_completed)

    LaunchedEffect(viewModel) {
        viewModel.serviceEvents.collect { event ->
            when (event) {
                ServiceEvent.StopScan -> onStopScanService()
                ServiceEvent.StartScan -> onStartScanService()
                ServiceEvent.ShowRestartSnackbar -> snackbarHostState.showSnackbar(restartServiceMessage)
            }
        }
    }

    val overlaySettingsLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) {
            viewModel.refreshOverlayState()
        }

    val batteryOptimizationLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) {
            val pm = context.getSystemService(PowerManager::class.java)
            viewModel.refreshBatteryOptimizationState(pm.isIgnoringBatteryOptimizations(context.packageName))
        }

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results ->
            viewModel.refreshPermissionStates(results)
            initialRequestDone = true
        }

    SettingsEffects(
        permissions = permissions,
        initialRequestDone = initialRequestDone,
        onLaunchPermissions = { launcher.launch(it) },
        onShowSettingsDialog = { showSettingsDialog = true },
        onStartScanService = onStartScanService,
        onCheckUpdate = viewModel::checkUpdate,
        onRefreshOverlayState = viewModel::refreshOverlayState,
        onRefreshBatteryOptimizationState = viewModel::refreshBatteryOptimizationState,
        onRefreshNotificationState = viewModel::refreshNotificationState,
        onRefreshDeviceScanChannelState = viewModel::refreshDeviceScanChannelState,
        onRefreshPermissionStates = viewModel::refreshPermissionStates,
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
        when {
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 3
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 2
            else -> 1
        }

    SettingsScaffold(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        permissionStates = uiState.permissionStates,
        uiState = uiState,
        isServiceRestarting = uiState.isServiceRestarting,
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
        onNotificationWarningClick = {
            context.openAppNotificationSettings()
        },
        onDeviceScanChannelWarningClick = {
            context.openNotificationChannelSettings(NotificationChannels.DEVICE_SCAN)
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
        onRestartServiceClick = viewModel::restartService,
        onBatteryOptimizationClick = {
            val pm = context.getSystemService(PowerManager::class.java)
            val intent = if (pm.isIgnoringBatteryOptimizations(context.packageName)) {
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            } else {
                Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    "package:${context.packageName}".toUri(),
                )
            }
            batteryOptimizationLauncher.launch(intent)
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
    onShowSettingsDialog: () -> Unit,
    onStartScanService: () -> Unit,
    onCheckUpdate: (String) -> Unit,
    onRefreshOverlayState: () -> Unit,
    onRefreshBatteryOptimizationState: (Boolean) -> Unit,
    onRefreshNotificationState: (Boolean) -> Unit,
    onRefreshDeviceScanChannelState: (Boolean) -> Unit,
    onRefreshPermissionStates: (Map<String, Boolean>) -> Unit,
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
        onCheckUpdate(versionName)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        onRefreshPermissionStates(
            permissions.associateWith {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            },
        )
        onRefreshOverlayState()
        val pm = context.getSystemService(PowerManager::class.java)
        onRefreshBatteryOptimizationState(pm.isIgnoringBatteryOptimizations(context.packageName))
        val notificationManager = NotificationManagerCompat.from(context)
        val notificationsEnabled = notificationManager.areNotificationsEnabled()
        onRefreshNotificationState(!notificationsEnabled)
        onRefreshDeviceScanChannelState(
            notificationsEnabled &&
                notificationManager.getNotificationChannel(NotificationChannels.DEVICE_SCAN)
                    ?.importance == android.app.NotificationManager.IMPORTANCE_NONE,
        )
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
    onNotificationWarningClick: () -> Unit,
    onDeviceScanChannelWarningClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onDevicesClick: () -> Unit,
    onGithubClick: () -> Unit,
    onOverlayToggle: (Boolean) -> Unit,
    onOverlayPositionClick: () -> Unit,
    onRestartServiceClick: () -> Unit,
    onBatteryOptimizationClick: () -> Unit,
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
            isNotificationsDisabled = uiState.isNotificationsDisabled,
            isDeviceScanChannelDisabled = uiState.isDeviceScanChannelDisabled,
            isServiceRestarting = isServiceRestarting,
            isBatteryOptimizationExempt = uiState.isBatteryOptimizationExempt,
            columns = columns,
            themeSettings = uiState.themeSettings,
            onPermissionWarningClick = onPermissionWarningClick,
            onBluetoothWarningClick = onBluetoothWarningClick,
            onNotificationWarningClick = onNotificationWarningClick,
            onDeviceScanChannelWarningClick = onDeviceScanChannelWarningClick,
            onUpdateClick = onUpdateClick,
            onLicensesClick = onLicensesClick,
            onDevicesClick = onDevicesClick,
            onGithubClick = onGithubClick,
            onOverlayToggle = onOverlayToggle,
            onOverlayPositionClick = onOverlayPositionClick,
            onRestartServiceClick = onRestartServiceClick,
            onBatteryOptimizationClick = onBatteryOptimizationClick,
            onThemeModeClick = onThemeModeClick,
            onDynamicColorToggle = onDynamicColorToggle,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

private fun android.content.Context.openAppNotificationSettings() {
    startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        },
    )
}

private fun android.content.Context.openNotificationChannelSettings(channelId: String) {
    startActivity(
        Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
        },
    )
}
