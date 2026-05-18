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

class GetCompatibleDevicesUseCaseTest {
    private lateinit var useCase: GetCompatibleDevicesUseCase
    private val repository = mockk<CompatibleDeviceRepository>()

    @Before
    fun setUp() {
        useCase = GetCompatibleDevicesUseCase(repository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invokeがrepositoryのgetCompatibleDevicesのFlowを返す`() =
        runTest {
            val devices = listOf(CompatibleDevice(name = "AirPods Pro", modelCode = 0x2002))
            val fakeFlow = MutableStateFlow(devices)
            every { repository.getCompatibleDevices() } returns fakeFlow

            val result = useCase().first()

            assertEquals(devices, result)
            verify(exactly = 1) { repository.getCompatibleDevices() }
            confirmVerified(repository)
        }
}
