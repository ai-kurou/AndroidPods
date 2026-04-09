package kurou.androidpods.feature.devices

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.androidpods.core.domain.CompatibleDevice
import kurou.androidpods.core.domain.DeviceImages
import kurou.androidpods.core.domain.GetCompatibleDevicesUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DevicesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val useCase = mockk<GetCompatibleDevicesUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `UseCaseが返したデバイスリストがdevicesに反映される`() {
        val devices = listOf(
            CompatibleDevice(name = "AirPods Pro (2nd Gen)", images = null),
            CompatibleDevice(name = "AirPods Max", images = DeviceImages.Single(body = 0)),
        )
        every { useCase() } returns devices

        val viewModel = DevicesViewModel(useCase)

        assertEquals(devices, viewModel.devices.value)
    }
}
