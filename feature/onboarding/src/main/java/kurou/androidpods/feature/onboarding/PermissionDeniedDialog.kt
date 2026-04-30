package kurou.androidpods.feature.onboarding

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun PermissionDeniedDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.onboarding_permission_dialog_title)) },
        text = { Text(stringResource(R.string.onboarding_permission_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.onboarding_permission_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.onboarding_permission_dialog_dismiss))
            }
        },
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun PermissionDeniedDialogPreview() {
    PermissionDeniedDialog(onDismiss = {}, onConfirm = {})
}
