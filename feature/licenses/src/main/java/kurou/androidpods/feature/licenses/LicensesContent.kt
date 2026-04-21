package kurou.androidpods.feature.licenses

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer

@Composable
internal fun LicensesContent(modifier: Modifier = Modifier) {
    val libraries by produceLibraries()
    LibrariesContainer(libraries = libraries, modifier = modifier)
}

@Preview(showBackground = true)
@Composable
private fun LicensesContentPreview() {
    LicensesContent()
}
