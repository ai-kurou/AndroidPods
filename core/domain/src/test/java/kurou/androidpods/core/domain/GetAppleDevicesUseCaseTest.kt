package kurou.androidpods.core.domain

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetAppleDevicesUseCaseTest {
    private lateinit var useCase: GetAppleDevicesUseCase
    private val repository = mockk<AppleDeviceRepository>(relaxUnitFun = true)

    @Before
    fun setUp() {
        useCase = GetAppleDevicesUseCase(repository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `observeがrepositoryのobserveDevicesのFlowを返す`() =
        runTest {
            val device =
                AppleDevice(
                    address = "AA:BB:CC:DD:EE:FF",
                    modelName = "AirPods Pro",
                    modelCode = 0x2002,
                    rssi = -60,
                    leftBattery = 80,
                    rightBattery = 75,
                    caseBattery = 90,
                )
            val fakeFlow = MutableStateFlow(mapOf("AA:BB:CC:DD:EE:FF" to device))
            every { repository.observeDevices() } returns fakeFlow

            val result = useCase.observe().first()

            assertEquals(mapOf("AA:BB:CC:DD:EE:FF" to device), result)
            verify(exactly = 1) { repository.observeDevices() }
            confirmVerified(repository)
        }

    @Test
    fun `startScanでrepositoryのstartScanが呼ばれる`() {
        useCase.startScan()

        verify(exactly = 1) { repository.startScan() }
        confirmVerified(repository)
    }

    @Test
    fun `stopScanでrepositoryのstopScanが呼ばれる`() {
        useCase.stopScan()

        verify(exactly = 1) { repository.stopScan() }
        confirmVerified(repository)
    }
}
