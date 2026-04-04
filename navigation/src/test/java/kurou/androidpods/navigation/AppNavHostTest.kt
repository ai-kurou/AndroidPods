package kurou.androidpods.navigation

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.os.Build
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = HiltTestApplication::class)
class AppNavHostTest {

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
        ShadowSettings.setCanDrawOverlays(true)
    }

    @Test
    fun `オンボーディングを進めて完了するとコールバックが呼ばれてsettingsに遷移する`() {
        var completeCalled = false
        lateinit var navController: NavHostController

        composeTestRule.setContent {
            navController = rememberNavController()
            AppNavHost(
                navController = navController,
                startDestination = Route.ONBOARDING,
                windowWidthSizeClass = WindowWidthSizeClass.Compact,
                onOnboardingComplete = { completeCalled = true },
                onStartScanService = {},
                onStopScanService = {},
            )
        }

        // ページ1 → ページ2
        composeTestRule.onNodeWithText("Next").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next").performClick()
        composeTestRule.waitForIdle()
        // ページ2: 権限要求ボタンを押す（テスト環境では権限が自動許可される）
        composeTestRule.onNodeWithText("Grant Permission").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grant Permission").performClick()
        composeTestRule.waitForIdle()
        // ページ3: オーバーレイ権限ボタンを押す（テスト環境では既に許可済み）
        composeTestRule.onNodeWithText("Allow Overlay").assertIsDisplayed()
        composeTestRule.onNodeWithText("Allow Overlay").performClick()
        composeTestRule.waitForIdle()
        // ページ4: Bluetooth ONボタンを押す（テスト環境ではBluetoothが有効のため直接完了する）
        composeTestRule.onNodeWithText("Enable Bluetooth").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enable Bluetooth").performClick()
        composeTestRule.waitForIdle()

        assertTrue(completeCalled)
        assertEquals(Route.SETTINGS, navController.currentDestination?.route)
    }
}
