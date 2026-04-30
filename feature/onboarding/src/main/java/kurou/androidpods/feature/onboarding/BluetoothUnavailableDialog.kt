package kurou.androidpods.feature.onboarding

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun BluetoothUnavailableDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.onboarding_bluetooth_unavailable_dialog_title)) },
        text = { Text(stringResource(R.string.onboarding_bluetooth_unavailable_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        },
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun BluetoothUnavailableDialogPreview() {
    BluetoothUnavailableDialog(onDismiss = {})
}
