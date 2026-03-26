package kurou.androidpods

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.androidpods.core.domain.FirstLaunchUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val fakeFlow = MutableStateFlow(true)
    private val useCase = mockk<FirstLaunchUseCase>(relaxUnitFun = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { useCase.observe() } returns fakeFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態はnullを返す`() {
        val viewModel = MainViewModel(useCase)

        assertNull(viewModel.isFirstLaunch.value)
    }

    @Test
    fun `初回起動時はtrueが反映される`() = runTest {
        fakeFlow.value = true
        val viewModel = MainViewModel(useCase)

        val job = launch(testDispatcher) { viewModel.isFirstLaunch.collect {} }
        assertEquals(true, viewModel.isFirstLaunch.value)
        job.cancel()
    }

    @Test
    fun `2回目以降はfalseが反映される`() = runTest {
        fakeFlow.value = false
        val viewModel = MainViewModel(useCase)

        val job = launch(testDispatcher) { viewModel.isFirstLaunch.collect {} }
        assertEquals(false, viewModel.isFirstLaunch.value)
        job.cancel()
    }

    @Test
    fun `markAsLaunchedでUseCaseのmarkAsLaunchedが呼ばれる`() = runTest {
        val viewModel = MainViewModel(useCase)

        viewModel.markAsLaunched()

        coVerify(exactly = 1) { useCase.markAsLaunched() }
    }
}
