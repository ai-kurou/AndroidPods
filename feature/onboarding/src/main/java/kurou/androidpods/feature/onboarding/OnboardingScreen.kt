package kurou.androidpods.feature.onboarding

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import kotlinx.coroutines.launch

private fun requiredPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val coroutineScope = rememberCoroutineScope()
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showBluetoothDeniedDialog by remember { mutableStateOf(false) }
    var showBluetoothUnavailableDialog by remember { mutableStateOf(false) }
    val bluetoothAdapter = remember {
        if (isPreview) null else context.getSystemService<BluetoothManager>()?.adapter
    }

    val permissionLauncher = if (!isPreview)
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results ->
            if (results.values.all { it })
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            else showPermissionDeniedDialog = true
        }
    else null

    val overlayPermissionLauncher = if (!isPreview)
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }
    else null

    val bluetoothEnableLauncher = if (!isPreview)
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) onComplete()
            else showBluetoothDeniedDialog = true
        }
    else null

    if (!isPreview) {
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                if (page == BLUETOOTH_PAGE && bluetoothAdapter == null) {
                    showBluetoothUnavailableDialog = true
                }
            }
        }
    }

    BackHandler(enabled = pagerState.currentPage > 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }

    OnboardingDialogs(
        showPermissionDeniedDialog = showPermissionDeniedDialog,
        showBluetoothUnavailableDialog = showBluetoothUnavailableDialog,
        showBluetoothDeniedDialog = showBluetoothDeniedDialog,
        onDismissPermissionDenied = { showPermissionDeniedDialog = false },
        onConfirmPermissionDenied = {
            showPermissionDeniedDialog = false
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        },
        onDismissBluetoothUnavailable = {
            showBluetoothUnavailableDialog = false
            onComplete()
        },
        onDismissBluetoothDenied = { showBluetoothDeniedDialog = false },
        onConfirmBluetoothDenied = {
            showBluetoothDeniedDialog = false
            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
        },
    )

    OnboardingContent(
        pagerState = pagerState,
        onButtonClick = {
            handleOnboardingButtonClick(
                page = pagerState.currentPage,
                context = context,
                bluetoothAdapter = bluetoothAdapter,
                permissionLauncher = permissionLauncher,
                overlayPermissionLauncher = overlayPermissionLauncher,
                bluetoothEnableLauncher = bluetoothEnableLauncher,
                onComplete = onComplete,
                onBluetoothUnavailable = { showBluetoothUnavailableDialog = true },
                onScrollToNextPage = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun OnboardingDialogs(
    showPermissionDeniedDialog: Boolean,
    showBluetoothUnavailableDialog: Boolean,
    showBluetoothDeniedDialog: Boolean,
    onDismissPermissionDenied: () -> Unit,
    onConfirmPermissionDenied: () -> Unit,
    onDismissBluetoothUnavailable: () -> Unit,
    onDismissBluetoothDenied: () -> Unit,
    onConfirmBluetoothDenied: () -> Unit,
) {
    if (showPermissionDeniedDialog) {
        PermissionDeniedDialog(
            onDismiss = onDismissPermissionDenied,
            onConfirm = onConfirmPermissionDenied,
        )
    }
    if (showBluetoothUnavailableDialog) {
        BluetoothUnavailableDialog(onDismiss = onDismissBluetoothUnavailable)
    }
    if (showBluetoothDeniedDialog) {
        BluetoothDeniedDialog(
            onDismiss = onDismissBluetoothDenied,
            onConfirm = onConfirmBluetoothDenied,
        )
    }
}

private fun handleOnboardingButtonClick(
    page: Int,
    context: Context,
    bluetoothAdapter: BluetoothAdapter?,
    permissionLauncher: ActivityResultLauncher<Array<String>>?,
    overlayPermissionLauncher: ActivityResultLauncher<Intent>?,
    bluetoothEnableLauncher: ActivityResultLauncher<Intent>?,
    onComplete: () -> Unit,
    onBluetoothUnavailable: () -> Unit,
    onScrollToNextPage: () -> Unit,
) {
    when (page) {
        PERMISSION_PAGE -> permissionLauncher?.launch(requiredPermissions())
        OVERLAY_PAGE -> {
            if (Settings.canDrawOverlays(context)) {
                onScrollToNextPage()
            } else {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${context.packageName}".toUri(),
                )
                overlayPermissionLauncher?.launch(intent)
            }
        }
        BLUETOOTH_PAGE -> when {
            bluetoothAdapter == null -> onBluetoothUnavailable()
            bluetoothAdapter.isEnabled -> onComplete()
            else -> bluetoothEnableLauncher?.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
        else -> onScrollToNextPage()
    }
}
