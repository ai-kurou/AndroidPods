package kurou.androidpods.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurou.androidpods.core.domain.OverlayPosition

@Composable
internal fun OverlayPositionDialog(
    currentPosition: OverlayPosition,
    onDismiss: () -> Unit,
    onPositionSelected: (OverlayPosition) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.overlay_position_label)) },
        text = {
            Column {
                OverlayPosition.entries.forEach { position ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onPositionSelected(position) }
                                .padding(vertical = 4.dp),
                    ) {
                        RadioButton(
                            selected = position == currentPosition,
                            onClick = { onPositionSelected(position) },
                        )
                        Text(stringResource(position.toStringRes()))
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
private fun OverlayPositionDialogPreview() {
    OverlayPositionDialog(
        currentPosition = OverlayPosition.TOP,
        onDismiss = {},
        onPositionSelected = {},
    )
}
