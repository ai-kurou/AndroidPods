package kurou.androidpods.feature.onboarding

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
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

    if (showPermissionDeniedDialog) {
        PermissionDeniedDialog(
            onDismiss = { showPermissionDeniedDialog = false },
            onConfirm = {
                showPermissionDeniedDialog = false
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            },
        )
    }

    if (showBluetoothUnavailableDialog) {
        BluetoothUnavailableDialog(
            onDismiss = {
                showBluetoothUnavailableDialog = false
                onComplete()
            },
        )
    }

    if (showBluetoothDeniedDialog) {
        BluetoothDeniedDialog(
            onDismiss = { showBluetoothDeniedDialog = false },
            onConfirm = {
                showBluetoothDeniedDialog = false
                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            },
        )
    }

    OnboardingContent(
        pagerState = pagerState,
        onButtonClick = {
            when {
                pagerState.currentPage == PERMISSION_PAGE -> {
                    permissionLauncher?.launch(requiredPermissions())
                }
                pagerState.currentPage == OVERLAY_PAGE -> {
                    if (Settings.canDrawOverlays(context)) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            "package:${context.packageName}".toUri(),
                        )
                        overlayPermissionLauncher?.launch(intent)
                    }
                }
                pagerState.currentPage == BLUETOOTH_PAGE -> {
                    when {
                        bluetoothAdapter == null -> {
                            showBluetoothUnavailableDialog = true
                        }
                        bluetoothAdapter.isEnabled -> onComplete()
                        else -> bluetoothEnableLauncher?.launch(
                            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                        )
                    }
                }
                pagerState.currentPage < PAGE_COUNT - 1 -> {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
                else -> onComplete()
            }
        },
        modifier = modifier,
    )
}
