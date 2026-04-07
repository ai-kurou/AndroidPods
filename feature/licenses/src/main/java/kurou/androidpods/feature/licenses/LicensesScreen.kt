package kurou.androidpods.feature.licenses

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LicensesViewModel = hiltViewModel(),
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("オープンソースライセンス") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LibrariesContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}
