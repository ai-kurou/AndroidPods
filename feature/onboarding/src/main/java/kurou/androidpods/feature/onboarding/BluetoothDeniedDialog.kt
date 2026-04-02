package kurou.androidpods.feature.onboarding

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun BluetoothDeniedDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.onboarding_bluetooth_dialog_title)) },
        text = { Text(stringResource(R.string.onboarding_bluetooth_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.onboarding_bluetooth_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.onboarding_bluetooth_dialog_dismiss))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun BluetoothDeniedDialogPreview() {
    BluetoothDeniedDialog(onDismiss = {}, onConfirm = {})
}
