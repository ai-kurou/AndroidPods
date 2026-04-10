package kurou.androidpods

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
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
    private lateinit var viewModel: MainViewModel

    private val testDispatcher = UnconfinedTestDispatcher()
    private val fakeFlow = MutableStateFlow(true)
    private val firstLaunchUseCase = mockk<FirstLaunchUseCase>(relaxUnitFun = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { firstLaunchUseCase.observe() } returns fakeFlow
        viewModel = MainViewModel(firstLaunchUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `初期状態はnullを返す`() {
        val viewModel = MainViewModel(firstLaunchUseCase)

        assertNull(viewModel.isFirstLaunch.value)
        // setUpのviewModel生成で1回 + このテストのviewModel生成で1回 = 合計2回
        verify(exactly = 2) { firstLaunchUseCase.observe() }
        confirmVerified(firstLaunchUseCase)
    }

    @Test
    fun `初回起動時はtrueが反映される`() = runTest {
        fakeFlow.value = true

        val job = launch(testDispatcher) { viewModel.isFirstLaunch.collect {} }
        assertEquals(true, viewModel.isFirstLaunch.value)
        job.cancel()
        verify(exactly = 1) { firstLaunchUseCase.observe() }
        confirmVerified(firstLaunchUseCase)
    }

    @Test
    fun `2回目以降はfalseが反映される`() = runTest {
        fakeFlow.value = false

        val job = launch(testDispatcher) { viewModel.isFirstLaunch.collect {} }
        assertEquals(false, viewModel.isFirstLaunch.value)
        job.cancel()
        verify(exactly = 1) { firstLaunchUseCase.observe() }
        confirmVerified(firstLaunchUseCase)
    }

    @Test
    fun `markAsLaunchedでUseCaseのmarkAsLaunchedが呼ばれる`() = runTest {
        viewModel.markAsLaunched()

        verify(exactly = 1) { firstLaunchUseCase.observe() }
        coVerify(exactly = 1) { firstLaunchUseCase.markAsLaunched() }
        confirmVerified(firstLaunchUseCase)
    }
}
