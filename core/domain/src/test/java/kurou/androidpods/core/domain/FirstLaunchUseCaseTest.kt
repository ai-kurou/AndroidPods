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

class FirstLaunchUseCaseTest {
    private lateinit var useCase: FirstLaunchUseCase
    private val repository = mockk<FirstLaunchRepository>(relaxUnitFun = true)

    @Before
    fun setUp() {
        useCase = FirstLaunchUseCase(repository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `observeがrepositoryのobserveIsFirstLaunchのFlowを返す`() =
        runTest {
            val fakeFlow = MutableStateFlow(true)
            every { repository.observeIsFirstLaunch() } returns fakeFlow

            val result = useCase.observe().first()

            assertEquals(true, result)
            verify(exactly = 1) { repository.observeIsFirstLaunch() }
            confirmVerified(repository)
        }

    @Test
    fun `markAsLaunchedでrepositoryのmarkAsLaunchedが呼ばれる`() =
        runTest {
            useCase.markAsLaunched()

            coVerify(exactly = 1) { repository.markAsLaunched() }
            confirmVerified(repository)
        }
}
