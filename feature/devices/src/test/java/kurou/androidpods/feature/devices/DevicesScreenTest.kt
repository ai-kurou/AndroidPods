package kurou.androidpods.feature.devices

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
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
            CompatibleDevice(name = "AirPods Pro (2nd Gen)", modelCode = 0x1420),
            CompatibleDevice(name = "AirPods Max", modelCode = 0x0A20),
        )
    private val onBack = mockk<() -> Unit>(relaxed = true)

    private fun windowSizeClassOf(widthDp: Float) =
        WindowSizeClass.BREAKPOINTS_V1.computeWindowSizeClass(widthDp = widthDp, heightDp = 800f)

    @Test
    fun `WindowWidthSizeClassがCompactの場合`() {
        assertIsDisplayedWithBack(windowSizeClassOf(400f))
    }

    @Test
    fun `WindowWidthSizeClassがMediumの場合`() {
        assertIsDisplayedWithBack(windowSizeClassOf(700f))
    }

    @Test
    fun `WindowWidthSizeClassがExpandedの場合`() {
        assertIsDisplayedWithBack(windowSizeClassOf(1000f))
    }

    private fun assertIsDisplayedWithBack(size: WindowSizeClass) {
        every { useCase() } returns flowOf(devices)

        composeTestRule.setContent {
            DevicesScreen(
                onBack = onBack,
                windowSizeClass = size,
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
