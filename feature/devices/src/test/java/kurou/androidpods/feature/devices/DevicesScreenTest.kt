package kurou.androidpods.feature.devices

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.androidpods.core.domain.CompatibleDevice
import kurou.androidpods.core.domain.GetCompatibleDevicesUseCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DevicesScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val useCase = mockk<GetCompatibleDevicesUseCase>()

    private val devices =
        listOf(
            CompatibleDevice(name = "AirPods Pro (2nd Gen)", images = null),
            CompatibleDevice(name = "AirPods Max", images = null),
        )
    private val onBack = mockk<() -> Unit>(relaxed = true)

    @Test
    fun `WindowWidthSizeClassがCompactの場合`() {
        assertIsDisplayedWithBack(WindowWidthSizeClass.Compact)
    }

    @Test
    fun `WindowWidthSizeClassがMediumの場合`() {
        assertIsDisplayedWithBack(WindowWidthSizeClass.Medium)
    }

    @Test
    fun `WindowWidthSizeClassがExpandedの場合`() {
        assertIsDisplayedWithBack(WindowWidthSizeClass.Expanded)
    }

    private fun assertIsDisplayedWithBack(size: WindowWidthSizeClass) {
        every { useCase() } returns devices

        composeTestRule.setContent {
            DevicesScreen(
                onBack = onBack,
                windowWidthSizeClass = size,
                viewModel = DevicesViewModel(useCase),
            )
        }

        composeTestRule.onNodeWithText("Compatible devices").assertIsDisplayed()
        composeTestRule.onNodeWithText("AirPods Pro (2nd Gen)").assertExists()
        composeTestRule.onNodeWithText("AirPods Max").assertExists()
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        verify { onBack() }
    }
}
