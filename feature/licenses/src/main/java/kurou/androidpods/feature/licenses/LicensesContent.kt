package kurou.androidpods.feature.licenses

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer

// LibrariesContainer のようなランタイムデータ依存のサードパーティコンポーネントは Preview に向かない
@Composable
internal fun LicensesContent(modifier: Modifier = Modifier) {
    val libraries by produceLibraries()
    LibrariesContainer(libraries = libraries, modifier = modifier)
}
