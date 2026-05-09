package kurou.androidpods.feature.onboarding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private lateinit var viewModel: OnboardingViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = OnboardingViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `showPermissionDeniedとdismissPermissionDeniedでフラグが切り替わる`() =
        runTest {
            assertEquals(false, viewModel.uiState.value.showPermissionDeniedDialog)

            viewModel.showPermissionDenied()
            assertEquals(true, viewModel.uiState.value.showPermissionDeniedDialog)

            viewModel.dismissPermissionDenied()
            assertEquals(false, viewModel.uiState.value.showPermissionDeniedDialog)
        }

    @Test
    fun `showBluetoothUnavailableとdismissBluetoothUnavailableでフラグが切り替わる`() =
        runTest {
            assertEquals(false, viewModel.uiState.value.showBluetoothUnavailableDialog)

            viewModel.showBluetoothUnavailable()
            assertEquals(true, viewModel.uiState.value.showBluetoothUnavailableDialog)

            viewModel.dismissBluetoothUnavailable()
            assertEquals(false, viewModel.uiState.value.showBluetoothUnavailableDialog)
        }

    @Test
    fun `showBluetoothDeniedとdismissBluetoothDeniedでフラグが切り替わる`() =
        runTest {
            assertEquals(false, viewModel.uiState.value.showBluetoothDeniedDialog)

            viewModel.showBluetoothDenied()
            assertEquals(true, viewModel.uiState.value.showBluetoothDeniedDialog)

            viewModel.dismissBluetoothDenied()
            assertEquals(false, viewModel.uiState.value.showBluetoothDeniedDialog)
        }
}
