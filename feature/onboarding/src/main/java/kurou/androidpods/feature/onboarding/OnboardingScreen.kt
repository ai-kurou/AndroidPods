package kurou.androidpods.feature.onboarding

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.getSystemService
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import android.app.Activity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 3
private const val PERMISSION_PAGE = 1
private const val BLUETOOTH_PAGE = 2

private fun requiredPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

private data class OnboardingPageData(
    @RawRes val lottieResId: Int,
    @StringRes val textResId: Int,
)

private val pages = listOf(
    OnboardingPageData(
        lottieResId = R.raw.charging,
        textResId = R.string.onboarding_page1_text,
    ),
    OnboardingPageData(
        lottieResId = R.raw.location,
        textResId = R.string.onboarding_page2_text,
    ),
    OnboardingPageData(
        lottieResId = R.raw.bluetooth,
        textResId = R.string.onboarding_page3_text,
    ),
)

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

    val permissionLauncher = if (!isPreview) {
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results ->
            if (results.values.all { it }) {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            } else {
                showPermissionDeniedDialog = true
            }
        }
    } else {
        null
    }

    val bluetoothEnableLauncher = if (!isPreview) {
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) onComplete()
            else showBluetoothDeniedDialog = true
        }
    } else {
        null
    }

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
            onDismiss = { showBluetoothUnavailableDialog = false },
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

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { page ->
            OnboardingPage(pageData = pages[page])
        }

        PageIndicator(
            pageCount = PAGE_COUNT,
            currentPage = pagerState.currentPage,
            modifier = Modifier.padding(16.dp),
        )

        Button(
            onClick = {
                when {
                    pagerState.currentPage == PERMISSION_PAGE -> {
                        permissionLauncher?.launch(requiredPermissions())
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            Text(
                text = stringResource(
                    when {
                        pagerState.currentPage == PERMISSION_PAGE -> R.string.onboarding_button_grant_permission
                        pagerState.currentPage == BLUETOOTH_PAGE -> R.string.onboarding_button_enable_bluetooth
                        pagerState.currentPage < PAGE_COUNT - 1 -> R.string.onboarding_button_next
                        else -> R.string.onboarding_button_get_started
                    },
                ),
            )
        }
    }
}

@Composable
private fun OnboardingPage(pageData: OnboardingPageData, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.screenHeightDp > configuration.screenWidthDp

    if (isPortrait) {
        Column(
            modifier = modifier.fillMaxSize().padding(36.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LottieContent(
                lottieResId = pageData.lottieResId,
                modifier = Modifier.size(200.dp),
            )
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                text = stringResource(pageData.textResId),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    } else {
        Row(
            modifier = modifier.fillMaxSize().padding(36.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LottieContent(
                lottieResId = pageData.lottieResId,
                modifier = Modifier.size(200.dp),
            )
            Spacer(modifier = Modifier.width(64.dp))
            Text(
                text = stringResource(pageData.textResId),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LottieContent(@RawRes lottieResId: Int, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier,
    )
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == currentPage) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                    ),
            )
        }
    }
}

@Preview(showBackground = true, name = "Portrait", device = "spec:width=411dp,height=891dp")
@Preview(showBackground = true, name = "Landscape", device = "spec:width=891dp,height=411dp")
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(onComplete = {})
}
