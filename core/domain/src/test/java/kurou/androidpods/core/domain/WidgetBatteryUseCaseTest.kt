package kurou.androidpods.core.domain

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class WidgetBatteryUseCaseTest {
    private lateinit var useCase: WidgetBatteryUseCase
    private val repository = mockk<WidgetBatteryRepository>(relaxUnitFun = true)

    @Before
    fun setUp() {
        useCase = WidgetBatteryUseCase(repository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `observeがrepositoryのobserveのFlowを返す`() =
        runTest {
            val state = WidgetBatteryState(
                deviceName = "AirPods Pro",
                leftBattery = 7,
                rightBattery = 8,
                caseBattery = 9,
                isSingle = false,
                recordedAt = 1000L,
            )
            val fakeFlow = MutableStateFlow<WidgetBatteryState?>(state)
            every { repository.observe() } returns fakeFlow

            val result = useCase.observe().first()

            assertEquals(state, result)
        }

    @Test
    fun `saveでrepositoryのsaveが呼ばれる`() =
        runTest {
            val device = AppleDevice(
                address = "AA:BB:CC:DD:EE:FF",
                modelName = "AirPods Pro",
                modelCode = 0x2002,
                rssi = -60,
                leftBattery = 7,
                rightBattery = 8,
                caseBattery = 9,
            )

            useCase.save(device)

            coVerify(exactly = 1) { repository.save(device) }
            confirmVerified(repository)
        }
}
