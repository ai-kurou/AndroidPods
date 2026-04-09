package kurou.androidpods.feature.devices

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.androidpods.core.domain.CompatibleDevice
import kurou.androidpods.core.domain.DeviceImages

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    onBack: () -> Unit,
    windowWidthSizeClass: WindowWidthSizeClass,
    modifier: Modifier = Modifier,
    viewModel: DevicesViewModel = hiltViewModel(),
) {
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val columns = when (windowWidthSizeClass) {
        WindowWidthSizeClass.Compact -> 2
        WindowWidthSizeClass.Medium -> 3
        else -> 4
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.compatible_devices)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        DevicesContent(
            devices = devices,
            columns = columns,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
internal fun DevicesContent(
    devices: List<CompatibleDevice>,
    columns: Int,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize().padding(16.dp),
    ) {
        items(devices) { device ->
            DeviceItem(device = device)
        }
    }
}

private val previewDevices = listOf(
    CompatibleDevice(name = "AirPods Pro (2nd Gen)", images = null),
    CompatibleDevice(name = "AirPods (4th Gen)", images = null),
    CompatibleDevice(name = "AirPods Max", images = null),
    CompatibleDevice(name = "Beats Studio Buds+", images = null),
    CompatibleDevice(name = "Powerbeats Pro", images = null),
    CompatibleDevice(name = "Beats Solo3", images = null),
)

@Preview(showBackground = true, name = "Compact (2列)")
@Composable
private fun DevicesContentPreviewCompact() {
    DevicesContent(devices = previewDevices, columns = 2)
}

@Preview(showBackground = true, widthDp = 700, name = "Medium (3列)")
@Composable
private fun DevicesContentPreviewMedium() {
    DevicesContent(devices = previewDevices, columns = 3)
}

@Preview(showBackground = true, widthDp = 900, name = "Expanded (4列)")
@Composable
private fun DevicesContentPreviewExpanded() {
    DevicesContent(devices = previewDevices, columns = 4)
}

@Preview(showBackground = true, name = "アイテム単体")
@Composable
private fun DeviceItemPreview() {
    DeviceItem(device = CompatibleDevice(name = "AirPods Pro (2nd Gen)", images = null))
}

@Composable
private fun DeviceItem(
    device: CompatibleDevice,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(16.dp),
        ) {
            when (val img = device.images) {
                is DeviceImages.Tws -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                ) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Image(
                            painter = painterResource(img.left),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                        )
                    }
                    Box(modifier = Modifier.weight(2f).fillMaxHeight()) {
                        Image(
                            painter = painterResource(img.case),
                            contentDescription = device.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                        )
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Image(
                            painter = painterResource(img.right),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                        )
                    }
                }
                is DeviceImages.Single -> Image(
                    painter = painterResource(img.body),
                    contentDescription = device.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
                null -> Unit
            }
        }
        Text(
            text = device.name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }
}
