package kurou.androidpods.feature.onboarding

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.launch
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OnboardingContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `ページ0はNextボタンとアニメーションが表示される`() {
        composeTestRule.setContent {
            OnboardingContent(
                pagerState = rememberPagerState(pageCount = { PAGE_COUNT }),
                onButtonClick = {},
            )
        }

        composeTestRule.onNodeWithTag("lottie_animation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Check the remaining battery level of your AirPods.").assertExists()
    }

    @Test
    fun `ページ1はGrant Permissionボタンとアニメーションが表示される`() {
        composeTestRule.setContent {
            val pagerState =
                rememberPagerState(
                    initialPage = PERMISSION_PAGE,
                    pageCount = { PAGE_COUNT },
                )
            OnboardingContent(
                pagerState = pagerState,
                onButtonClick = {},
            )
        }

        composeTestRule.onNodeWithTag("lottie_animation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Location permission is required to use Bluetooth.").assertExists()
    }

    @Test
    fun `ページ2はAllow Overlayボタンとアニメーションが表示される`() {
        composeTestRule.setContent {
            val pagerState =
                rememberPagerState(
                    initialPage = OVERLAY_PAGE,
                    pageCount = { PAGE_COUNT },
                )
            OnboardingContent(
                pagerState = pagerState,
                onButtonClick = {},
            )
        }

        composeTestRule.onNodeWithTag("lottie_animation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Allow overlay display to show battery info on screen.").assertExists()
    }

    @Test
    fun `ページ3はEnable Bluetoothボタンとアニメーションが表示される`() {
        composeTestRule.setContent {
            val pagerState =
                rememberPagerState(
                    initialPage = BLUETOOTH_PAGE,
                    pageCount = { PAGE_COUNT },
                )
            OnboardingContent(
                pagerState = pagerState,
                onButtonClick = {},
            )
        }

        composeTestRule.onNodeWithTag("lottie_animation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please enable Bluetooth to connect to your AirPods.").assertExists()
    }

    @Test
    fun `ボタンをクリックするとonButtonClickが呼ばれる`() {
        var clicked = false
        composeTestRule.setContent {
            OnboardingContent(
                pagerState = rememberPagerState(pageCount = { PAGE_COUNT }),
                onButtonClick = { clicked = true },
            )
        }

        composeTestRule.onNodeWithText("Next").performClick()
        assertTrue(clicked)
    }

    @Test
    fun `ページを進めるとボタンテキストが切り替わる`() {
        composeTestRule.setContent {
            val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
            val scope = rememberCoroutineScope()
            OnboardingContent(
                pagerState = pagerState,
                onButtonClick = {
                    scope.launch { pagerState.scrollToPage(pagerState.currentPage + 1) }
                },
            )
        }

        composeTestRule.onNodeWithText("Next").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Grant Permission").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Allow Overlay").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Enable Bluetooth").performClick()
        composeTestRule.waitForIdle()
    }
}
