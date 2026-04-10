package kurou.androidpods.feature.settings

import android.bluetooth.BluetoothAdapter
import io.mockk.every
import io.mockk.mockk
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val fakeFlow = MutableSharedFlow<Int?>()
    // productionと同様、observe()は初期値emptyMapをもつStateFlowを返す
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
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態にcurrentの値が反映される`() {
        every { getBluetoothAdapterStateUseCase.current() } returns BluetoothAdapter.STATE_ON

        val viewModel = SettingsViewModel(getBluetoothAdapterStateUseCase, getAppleDevicesUseCase, getOverlaySettingsUseCase)

        assertEquals(BluetoothAdapter.STATE_ON, viewModel.uiState.value.bluetoothAdapterState)
    }

    @Test
    fun `observeのFlowに値を流すとbluetoothAdapterStateが更新される`() = runTest {
        every { getBluetoothAdapterStateUseCase.current() } returns BluetoothAdapter.STATE_OFF

        val viewModel = SettingsViewModel(getBluetoothAdapterStateUseCase, getAppleDevicesUseCase, getOverlaySettingsUseCase)

        // fakeAppleDevicesFlowはStateFlowなので初期値emptyMapを既に持っている
        fakeFlow.emit(BluetoothAdapter.STATE_ON)
        assertEquals(BluetoothAdapter.STATE_ON, viewModel.uiState.value.bluetoothAdapterState)

        fakeFlow.emit(BluetoothAdapter.STATE_TURNING_OFF)
        assertEquals(BluetoothAdapter.STATE_TURNING_OFF, viewModel.uiState.value.bluetoothAdapterState)
    }

    @Test
    fun `アダプタ非対応時はnullが設定される`() {
        every { getBluetoothAdapterStateUseCase.current() } returns null

        val viewModel = SettingsViewModel(getBluetoothAdapterStateUseCase, getAppleDevicesUseCase, getOverlaySettingsUseCase)

        assertNull(viewModel.uiState.value.bluetoothAdapterState)
    }

    @Test
    fun `初期状態ではAppleデバイスが空`() {
        every { getBluetoothAdapterStateUseCase.current() } returns BluetoothAdapter.STATE_ON

        val viewModel = SettingsViewModel(getBluetoothAdapterStateUseCase, getAppleDevicesUseCase, getOverlaySettingsUseCase)

        assertEquals(emptyMap<String, AppleDevice>(), viewModel.uiState.value.appleDevices)
    }

    @Test
    fun `observeのFlowに値を流すとappleDevicesが更新される`() = runTest {
        every { getBluetoothAdapterStateUseCase.current() } returns BluetoothAdapter.STATE_ON

        val viewModel = SettingsViewModel(getBluetoothAdapterStateUseCase, getAppleDevicesUseCase, getOverlaySettingsUseCase)

        // combineはすべてのupstreamが1回以上emitするまで値を出さないため、
        // bluetoothFlowに初期値を流してcombineを動作可能にする
        fakeFlow.emit(BluetoothAdapter.STATE_ON)

        val device = AppleDevice("AA:BB:CC:DD:EE:FF", "AirPods Pro (2nd Gen)", 0x1420, -45, 8, 9, 7)
        val devices = mapOf(device.address to device)
        fakeAppleDevicesFlow.emit(devices)
        assertEquals(devices, viewModel.uiState.value.appleDevices)
    }

    @Test
    fun `refreshOverlayStateでisEnabledの最新値が反映される`() = runTest {
        every { getBluetoothAdapterStateUseCase.current() } returns BluetoothAdapter.STATE_ON

        val viewModel = SettingsViewModel(getBluetoothAdapterStateUseCase, getAppleDevicesUseCase, getOverlaySettingsUseCase)

        // combineはすべてのupstreamが1回以上emitするまで値を出さないため、
        // bluetoothFlowに初期値を流してcombineを動作可能にする
        fakeFlow.emit(BluetoothAdapter.STATE_ON)
        assertEquals(false, viewModel.uiState.value.overlayEnabled)

        every { getOverlaySettingsUseCase.isEnabled() } returns true
        viewModel.refreshOverlayState()
        assertEquals(true, viewModel.uiState.value.overlayEnabled)
    }

    @Test
    fun `startScanでUseCaseのstartScanが呼ばれる`() {
        every { getBluetoothAdapterStateUseCase.current() } returns BluetoothAdapter.STATE_ON

        val viewModel = SettingsViewModel(getBluetoothAdapterStateUseCase, getAppleDevicesUseCase, getOverlaySettingsUseCase)
        viewModel.startScan()

        verify(exactly = 1) { getAppleDevicesUseCase.startScan() }
    }

    @Test
    fun `stopScanでUseCaseのstopScanが呼ばれる`() {
        every { getBluetoothAdapterStateUseCase.current() } returns BluetoothAdapter.STATE_ON

        val viewModel = SettingsViewModel(getBluetoothAdapterStateUseCase, getAppleDevicesUseCase, getOverlaySettingsUseCase)
        viewModel.stopScan()

        verify(exactly = 1) { getAppleDevicesUseCase.stopScan() }
    }
}
