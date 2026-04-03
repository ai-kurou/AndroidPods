package kurou.androidpods.navigation

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.os.Build
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = HiltTestApplication::class)
class AppScaffoldTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val shadowApp = Shadows.shadowOf(app)
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        else
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.forEach { shadowApp.grantPermissions(it) }

        Shadows.shadowOf(BluetoothAdapter.getDefaultAdapter()).setEnabled(true)
    }

    @Test
    fun `ナビゲーションアイテムが表示される`() {
        composeTestRule.setContent {
            AppScaffold(
                windowWidthSizeClass = WindowWidthSizeClass.Compact,
                onStartScanService = {},
                onStopScanService = {},
            )
        }

        TopLevelDestination.entries.forEach { destination ->
            composeTestRule
                .onNodeWithContentDescription(
                    composeTestRule.activity.getString(destination.labelResId),
                    useUnmergedTree = true,
                )
                .assertExists()
        }
    }
}
