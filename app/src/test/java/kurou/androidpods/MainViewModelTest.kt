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
import kurou.androidpods.core.domain.ThemeSettings
import kurou.androidpods.core.domain.ThemeSettingsUseCase
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
    private val fakeThemeFlow = MutableStateFlow(ThemeSettings())
    private val firstLaunchUseCase = mockk<FirstLaunchUseCase>(relaxUnitFun = true)
    private val themeSettingsUseCase = mockk<ThemeSettingsUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { firstLaunchUseCase.observe() } returns fakeFlow
        every { themeSettingsUseCase.observe() } returns fakeThemeFlow
        viewModel = MainViewModel(firstLaunchUseCase, themeSettingsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `初期状態はisFirstLaunchがnullのUiStateを返す`() {
        assertNull(viewModel.uiState.value.isFirstLaunch)
        verify(exactly = 1) { firstLaunchUseCase.observe() }
        verify(exactly = 1) { themeSettingsUseCase.observe() }
        confirmVerified(firstLaunchUseCase, themeSettingsUseCase)
    }

    @Test
    fun `初回起動時はisFirstLaunchがtrueのUiStateが反映される`() = runTest {
        fakeFlow.value = true

        val job = launch(testDispatcher) { viewModel.uiState.collect {} }
        assertEquals(true, viewModel.uiState.value.isFirstLaunch)
        job.cancel()
        verify(exactly = 1) { firstLaunchUseCase.observe() }
        verify(exactly = 1) { themeSettingsUseCase.observe() }
        confirmVerified(firstLaunchUseCase, themeSettingsUseCase)
    }

    @Test
    fun `2回目以降はisFirstLaunchがfalseのUiStateが反映される`() = runTest {
        fakeFlow.value = false

        val job = launch(testDispatcher) { viewModel.uiState.collect {} }
        assertEquals(false, viewModel.uiState.value.isFirstLaunch)
        job.cancel()
        verify(exactly = 1) { firstLaunchUseCase.observe() }
        verify(exactly = 1) { themeSettingsUseCase.observe() }
        confirmVerified(firstLaunchUseCase, themeSettingsUseCase)
    }

    @Test
    fun `markAsLaunchedでUseCaseのmarkAsLaunchedが呼ばれる`() = runTest {
        viewModel.markAsLaunched()

        verify(exactly = 1) { firstLaunchUseCase.observe() }
        verify(exactly = 1) { themeSettingsUseCase.observe() }
        coVerify(exactly = 1) { firstLaunchUseCase.markAsLaunched() }
        confirmVerified(firstLaunchUseCase, themeSettingsUseCase)
    }
}
