package kurou.androidpods.core.domain

import io.mockk.coVerify
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

class UnknownDeviceUseCaseTest {
    private lateinit var useCase: UnknownDeviceUseCase
    private val repository = mockk<UnknownDeviceRepository>(relaxUnitFun = true)

    @Before
    fun setUp() {
        useCase = UnknownDeviceUseCase(repository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `observeがrepositoryのobserveUnknownModelCodesのFlowを返す`() =
        runTest {
            val fakeFlow = MutableStateFlow(setOf("0x1234"))
            every { repository.observeUnknownModelCodes() } returns fakeFlow

            val result = useCase.observe().first()

            assertEquals(setOf("0x1234"), result)
            verify(exactly = 1) { repository.observeUnknownModelCodes() }
            confirmVerified(repository)
        }

    @Test
    fun `saveでrepositoryのsaveUnknownModelCodeが呼ばれる`() =
        runTest {
            useCase.save(0x1234)

            coVerify(exactly = 1) { repository.saveUnknownModelCode(0x1234) }
            confirmVerified(repository)
        }

    @Test
    fun `reportでrepositoryのreportWithDeviceNameが呼ばれる`() =
        runTest {
            useCase.report(0x1234, "AirPods Max")

            coVerify(exactly = 1) { repository.reportWithDeviceName(0x1234, "AirPods Max") }
            confirmVerified(repository)
        }
}
