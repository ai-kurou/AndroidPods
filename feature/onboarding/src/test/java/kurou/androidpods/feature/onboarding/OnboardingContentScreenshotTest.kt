package kurou.androidpods.feature.onboarding

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-port-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OnboardingContentScreenshotTest {

    private val roborazziOptions = RoborazziOptions(
        compareOptions = RoborazziOptions.CompareOptions(
            changeThreshold = 0.05f,
        ),
    )

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `ページ0_イントロ`() {
        composeTestRule.setContent {
            OnboardingContent(
                pagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = { PAGE_COUNT },
                ),
                onButtonClick = {},
            )
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    fun `ページ1_権限リクエスト`() {
        composeTestRule.setContent {
            OnboardingContent(
                pagerState = rememberPagerState(
                    initialPage = PERMISSION_PAGE,
                    pageCount = { PAGE_COUNT },
                ),
                onButtonClick = {},
            )
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    fun `ページ2_オーバーレイ許可`() {
        composeTestRule.setContent {
            OnboardingContent(
                pagerState = rememberPagerState(
                    initialPage = OVERLAY_PAGE,
                    pageCount = { PAGE_COUNT },
                ),
                onButtonClick = {},
            )
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    fun `ページ3_Bluetooth有効化`() {
        composeTestRule.setContent {
            OnboardingContent(
                pagerState = rememberPagerState(
                    initialPage = BLUETOOTH_PAGE,
                    pageCount = { PAGE_COUNT },
                ),
                onButtonClick = {},
            )
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    @Config(qualifiers = "w640dp-h360dp-land-xxhdpi")
    fun `ページ0_イントロ_横向き`() {
        composeTestRule.setContent {
            OnboardingContent(
                pagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = { PAGE_COUNT },
                ),
                onButtonClick = {},
            )
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    @Config(qualifiers = "w640dp-h360dp-land-xxhdpi")
    fun `ページ1_権限リクエスト_横向き`() {
        composeTestRule.setContent {
            OnboardingContent(
                pagerState = rememberPagerState(
                    initialPage = PERMISSION_PAGE,
                    pageCount = { PAGE_COUNT },
                ),
                onButtonClick = {},
            )
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    @Config(qualifiers = "w640dp-h360dp-land-xxhdpi")
    fun `ページ2_オーバーレイ許可_横向き`() {
        composeTestRule.setContent {
            OnboardingContent(
                pagerState = rememberPagerState(
                    initialPage = OVERLAY_PAGE,
                    pageCount = { PAGE_COUNT },
                ),
                onButtonClick = {},
            )
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    @Config(qualifiers = "w640dp-h360dp-land-xxhdpi")
    fun `ページ3_Bluetooth有効化_横向き`() {
        composeTestRule.setContent {
            OnboardingContent(
                pagerState = rememberPagerState(
                    initialPage = BLUETOOTH_PAGE,
                    pageCount = { PAGE_COUNT },
                ),
                onButtonClick = {},
            )
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }
}
