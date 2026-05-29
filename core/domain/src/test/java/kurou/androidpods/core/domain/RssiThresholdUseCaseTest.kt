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

class RssiThresholdUseCaseTest {
    private lateinit var useCase: RssiThresholdUseCase
    private val repository = mockk<RssiThresholdRepository>(relaxUnitFun = true)

    @Before
    fun setUp() {
        useCase = RssiThresholdUseCase(repository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `observeがrepositoryのobserveのFlowを返す`() =
        runTest {
            val fakeFlow = MutableStateFlow(RssiThreshold.VERY_NEAR)
            every { repository.observe() } returns fakeFlow

            val result = useCase.observe().first()

            assertEquals(RssiThreshold.VERY_NEAR, result)
            verify(exactly = 1) { repository.observe() }
            confirmVerified(repository)
        }

    @Test
    fun `updateでrepositoryのupdateが呼ばれる`() =
        runTest {
            useCase.update(RssiThreshold.NEAR)

            coVerify(exactly = 1) { repository.update(RssiThreshold.NEAR) }
            confirmVerified(repository)
        }
}
