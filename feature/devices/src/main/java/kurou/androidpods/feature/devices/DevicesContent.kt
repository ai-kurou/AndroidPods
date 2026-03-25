package kurou.androidpods.feature.devices

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun DevicesContent(
    permissionStates: Map<String, Boolean>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {
        permissionStates.forEach { (permission, granted) ->
            val shortName = permission.substringAfterLast(".")
            val status = if (granted) stringResource(R.string.permission_granted) else stringResource(R.string.permission_not_granted)
            Text(text = "$shortName: $status")
        }
    }
}

@Preview(showBackground = true, name = "API 31+ (Android 12+)")
@Composable
private fun DevicesContentPreviewApi31() {
    DevicesContent(
        permissionStates = mapOf(
            android.Manifest.permission.BLUETOOTH_CONNECT to true,
            android.Manifest.permission.BLUETOOTH_SCAN to true,
        ),
    )
}

@Preview(showBackground = true, name = "API 30以下")
@Composable
private fun DevicesContentPreviewApi30() {
    DevicesContent(
        permissionStates = mapOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION to true,
        ),
    )
}
