package kurou.androidpods.feature.onboarding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
            assertFalse(viewModel.uiState.value.showPermissionDeniedDialog)

            viewModel.showPermissionDenied()
            assertTrue(viewModel.uiState.value.showPermissionDeniedDialog)

            viewModel.dismissPermissionDenied()
            assertFalse(viewModel.uiState.value.showPermissionDeniedDialog)
        }

    @Test
    fun `showBluetoothUnavailableとdismissBluetoothUnavailableでフラグが切り替わる`() =
        runTest {
            assertFalse(viewModel.uiState.value.showBluetoothUnavailableDialog)

            viewModel.showBluetoothUnavailable()
            assertTrue(viewModel.uiState.value.showBluetoothUnavailableDialog)

            viewModel.dismissBluetoothUnavailable()
            assertFalse(viewModel.uiState.value.showBluetoothUnavailableDialog)
        }

    @Test
    fun `showBluetoothDeniedとdismissBluetoothDeniedでフラグが切り替わる`() =
        runTest {
            assertFalse(viewModel.uiState.value.showBluetoothDeniedDialog)

            viewModel.showBluetoothDenied()
            assertTrue(viewModel.uiState.value.showBluetoothDeniedDialog)

            viewModel.dismissBluetoothDenied()
            assertFalse(viewModel.uiState.value.showBluetoothDeniedDialog)
        }
}
