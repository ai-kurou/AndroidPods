package kurou.androidpods.feature.settings

import android.bluetooth.BluetoothAdapter
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.GetAppleDevicesUseCase
import kurou.androidpods.core.domain.GetBluetoothAdapterStateUseCase
import kurou.androidpods.core.domain.GetOverlaySettingsUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private lateinit var viewModel: SettingsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()
    private val fakeFlow = MutableSharedFlow<Int?>()
    private val fakeAppleDevicesFlow = MutableStateFlow<Map<String, AppleDevice>>(emptyMap())
    private val getBluetoothAdapterStateUseCase = mockk<GetBluetoothAdapterStateUseCase>()
    private val getAppleDevicesUseCase = mockk<GetAppleDevicesUseCase>(relaxUnitFun = true)
    private val getOverlaySettingsUseCase = mockk<GetOverlaySettingsUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getBluetoothAdapterStateUseCase.observe() } returns fakeFlow
        every { getAppleDevicesUseCase.observe() } returns fakeAppleDevicesFlow
        every { getOverlaySettingsUseCase.isEnabled() } returns false
        viewModel = SettingsViewModel(getBluetoothAdapterStateUseCase, getAppleDevicesUseCase, getOverlaySettingsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `refreshOverlayStateでisEnabledの最新値が反映される`() = runTest {
        // combineはすべてのupstreamが1回以上emitするまで値を出さないため、
        // bluetoothFlowに初期値を流してcombineを動作可能にする
        fakeFlow.emit(BluetoothAdapter.STATE_ON)
        assertEquals(false, viewModel.uiState.value.overlayEnabled)

        every { getOverlaySettingsUseCase.isEnabled() } returns true
        viewModel.refreshOverlayState()
        assertEquals(true, viewModel.uiState.value.overlayEnabled)
        verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
        verify(exactly = 1) { getAppleDevicesUseCase.observe() }
        // コンストラクタで1回 + refreshOverlayState()で1回 = 合計2回
        verify(exactly = 2) { getOverlaySettingsUseCase.isEnabled() }
        confirmVerified(getBluetoothAdapterStateUseCase, getAppleDevicesUseCase, getOverlaySettingsUseCase)
    }

    @Test
    fun `startScanとstopScanでUseCaseのメソッドが呼ばれる`() {
        viewModel.startScan()
        viewModel.stopScan()
        verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
        verify(exactly = 1) { getAppleDevicesUseCase.observe() }
        verify(exactly = 1) { getAppleDevicesUseCase.startScan() }
        verify(exactly = 1) { getAppleDevicesUseCase.stopScan() }
        verify(exactly = 1) { getOverlaySettingsUseCase.isEnabled() }
        confirmVerified(getBluetoothAdapterStateUseCase, getAppleDevicesUseCase, getOverlaySettingsUseCase)
    }
}
