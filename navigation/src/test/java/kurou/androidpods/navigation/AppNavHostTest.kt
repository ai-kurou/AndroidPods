package kurou.androidpods.navigation

import android.Manifest
import android.os.Build
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

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = HiltTestApplication::class)
class AppNavHostTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun grantPermissions() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val shadowApp = Shadows.shadowOf(app)
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        permissions.forEach { shadowApp.grantPermissions(it) }
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
        // ページ3で完了
        composeTestRule.onNodeWithText("Get Started").assertIsDisplayed()
        composeTestRule.onNodeWithText("Get Started").performClick()
        composeTestRule.waitForIdle()

        assertTrue(completeCalled)
        assertEquals(Route.SETTINGS, navController.currentDestination?.route)
    }
}
