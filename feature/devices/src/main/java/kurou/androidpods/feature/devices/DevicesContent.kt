package kurou.androidpods.feature.devices

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurou.androidpods.core.domain.CompatibleDevice
import kurou.androidpods.core.domain.DeviceImages

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
            DeviceItem(device = device, modifier = Modifier.fillMaxWidth().aspectRatio(1f))
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

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun DevicesContentPreviewTwoColumns() {
    DevicesContent(devices = previewDevices, columns = 2)
}

@Preview(showBackground = true, widthDp = 700, heightDp = 400)
@Composable
private fun DevicesContentPreviewThreeColumns() {
    DevicesContent(devices = previewDevices, columns = 3)
}

@Preview(showBackground = true, widthDp = 900, heightDp = 400)
@Composable
private fun DevicesContentPreviewFourColumns() {
    DevicesContent(devices = previewDevices, columns = 4)
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
            modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp),
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
                            modifier = Modifier.fillMaxSize().padding(2.dp),
                        )
                    }
                    Box(modifier = Modifier.weight(2f).fillMaxHeight()) {
                        Image(
                            painter = painterResource(img.case),
                            contentDescription = device.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Image(
                            painter = painterResource(img.right),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(2.dp),
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
            modifier = Modifier.padding(8.dp)
        )
    }
}
