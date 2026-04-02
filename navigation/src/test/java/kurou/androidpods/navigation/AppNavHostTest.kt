package kurou.androidpods.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = HiltTestApplication::class)
class AppNavHostTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Test
    fun `startDestinationがonboardingのときオンボーディング画面が表示される`() {
        composeTestRule.setContent {
            AppNavHost(
                navController = rememberNavController(),
                startDestination = Route.ONBOARDING,
                onOnboardingComplete = {},
                onStartScanService = {},
                onStopScanService = {},
            )
        }

        composeTestRule.onNodeWithText("Welcome to AndroidPods").assertIsDisplayed()
        composeTestRule.onNodeWithText("Get Started").assertIsDisplayed()
    }

    @Test
    fun `オンボーディング完了でコールバックが呼ばれてsettingsに遷移する`() {
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

        composeTestRule.onNodeWithText("Get Started").performClick()
        composeTestRule.waitForIdle()

        assertTrue(completeCalled)
        assertEquals(Route.SETTINGS, navController.currentDestination?.route)
    }
}
