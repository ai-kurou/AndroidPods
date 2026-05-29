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
    private val rssiThresholdRepository = mockk<RssiThresholdRepository>()

    private fun device(rssi: Int) = AppleDevice(
        address = "AA:BB:CC:DD:EE:FF",
        modelName = "AirPods Pro",
        modelCode = 0x2002,
        rssi = rssi,
        leftBattery = 80,
        rightBattery = 75,
        caseBattery = 90,
    )

    @Before
    fun setUp() {
        useCase = GetAppleDevicesUseCase(repository, rssiThresholdRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `ALLの場合はRSSIに関わらず全デバイスを返す`() =
        runTest {
            val devices = mapOf("key" to device(rssi = -90))
            every { repository.observeDevices() } returns MutableStateFlow(devices)
            every { rssiThresholdRepository.observe() } returns MutableStateFlow(RssiThreshold.ALL)

            val result = useCase.observe().first()

            assertEquals(devices, result)
            verify(exactly = 1) { repository.observeDevices() }
            verify(exactly = 1) { rssiThresholdRepository.observe() }
            confirmVerified(repository, rssiThresholdRepository)
        }

    @Test
    fun `閾値以上のRSSIを持つデバイスのみ返す`() =
        runTest {
            val nearDevice = device(rssi = -60)
            val farDevice = device(rssi = -80).copy(modelCode = 0x2003)
            val devices = mapOf("near" to nearDevice, "far" to farDevice)
            every { repository.observeDevices() } returns MutableStateFlow(devices)
            every { rssiThresholdRepository.observe() } returns MutableStateFlow(RssiThreshold.MEDIUM)

            val result = useCase.observe().first()

            assertEquals(mapOf("near" to nearDevice), result)
            verify(exactly = 1) { repository.observeDevices() }
            verify(exactly = 1) { rssiThresholdRepository.observe() }
            confirmVerified(repository, rssiThresholdRepository)
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
