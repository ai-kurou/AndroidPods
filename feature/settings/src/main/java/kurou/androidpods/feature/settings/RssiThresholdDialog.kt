package kurou.androidpods.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurou.androidpods.core.domain.RssiThreshold

@Composable
internal fun RssiThresholdDialog(
    currentThreshold: RssiThreshold,
    onDismiss: () -> Unit,
    onThresholdSelected: (RssiThreshold) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rssi_threshold_label)) },
        text = {
            Column {
                RssiThreshold.entries.forEach { threshold ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = threshold == currentThreshold,
                                    onClick = { onThresholdSelected(threshold) },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 4.dp),
                    ) {
                        RadioButton(
                            selected = threshold == currentThreshold,
                            onClick = null,
                            modifier = Modifier.minimumInteractiveComponentSize(),
                        )
                        Text(stringResource(threshold.toStringRes()))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun RssiThresholdDialogPreview() {
    RssiThresholdDialog(
        currentThreshold = RssiThreshold.VERY_NEAR,
        onDismiss = {},
        onThresholdSelected = {},
    )
}
