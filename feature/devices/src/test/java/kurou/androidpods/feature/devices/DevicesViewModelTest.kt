package kurou.androidpods.feature.devices

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
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
    private lateinit var viewModel: DevicesViewModel

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getCompatibleDevicesUseCase = mockk<GetCompatibleDevicesUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `UseCaseが返したデバイスリストがdevicesに反映される`() {
        val devices = listOf(
            CompatibleDevice(name = "AirPods Pro (2nd Gen)", images = null),
            CompatibleDevice(name = "AirPods Max", images = DeviceImages.Single(body = 0)),
        )
        every { getCompatibleDevicesUseCase() } returns devices
        viewModel = DevicesViewModel(getCompatibleDevicesUseCase)

        assertEquals(devices, viewModel.devices.value)
        verify(exactly = 1) { getCompatibleDevicesUseCase() }
        confirmVerified(getCompatibleDevicesUseCase)
    }
}
