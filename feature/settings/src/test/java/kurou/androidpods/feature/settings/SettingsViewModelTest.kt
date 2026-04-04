package kurou.androidpods.feature.settings

import android.bluetooth.BluetoothAdapter
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
    private val fakeAppleDevicesFlow = MutableSharedFlow<Map<String, AppleDevice>>()
    private val useCase = mockk<GetBluetoothAdapterStateUseCase>()
    private val appleDevicesUseCase = mockk<GetAppleDevicesUseCase>(relaxUnitFun = true)
    private val overlaySettingsUseCase = mockk<GetOverlaySettingsUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { useCase.observe() } returns fakeFlow
        every { appleDevicesUseCase.observe() } returns fakeAppleDevicesFlow
        every { overlaySettingsUseCase.isEnabled() } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態にcurrentの値が反映される`() {
        every { useCase.current() } returns BluetoothAdapter.STATE_ON

        val viewModel = SettingsViewModel(useCase, appleDevicesUseCase, overlaySettingsUseCase)

        assertEquals(BluetoothAdapter.STATE_ON, viewModel.bluetoothAdapterState.value)
    }

    @Test
    fun `observeのFlowに値を流すとbluetoothAdapterStateが更新される`() = runTest {
        every { useCase.current() } returns BluetoothAdapter.STATE_OFF

        val viewModel = SettingsViewModel(useCase, appleDevicesUseCase, overlaySettingsUseCase)

        fakeFlow.emit(BluetoothAdapter.STATE_ON)
        assertEquals(BluetoothAdapter.STATE_ON, viewModel.bluetoothAdapterState.value)

        fakeFlow.emit(BluetoothAdapter.STATE_TURNING_OFF)
        assertEquals(BluetoothAdapter.STATE_TURNING_OFF, viewModel.bluetoothAdapterState.value)
    }

    @Test
    fun `refreshBluetoothStateでcurrentの最新値が反映される`() {
        every { useCase.current() } returns BluetoothAdapter.STATE_OFF

        val viewModel = SettingsViewModel(useCase, appleDevicesUseCase, overlaySettingsUseCase)
        assertEquals(BluetoothAdapter.STATE_OFF, viewModel.bluetoothAdapterState.value)

        every { useCase.current() } returns BluetoothAdapter.STATE_ON
        viewModel.refreshBluetoothState()
        assertEquals(BluetoothAdapter.STATE_ON, viewModel.bluetoothAdapterState.value)
    }

    @Test
    fun `アダプタ非対応時はnullが設定される`() {
        every { useCase.current() } returns null

        val viewModel = SettingsViewModel(useCase, appleDevicesUseCase, overlaySettingsUseCase)

        assertNull(viewModel.bluetoothAdapterState.value)
    }

    @Test
    fun `初期状態ではAppleデバイスが空`() {
        every { useCase.current() } returns BluetoothAdapter.STATE_ON

        val viewModel = SettingsViewModel(useCase, appleDevicesUseCase, overlaySettingsUseCase)

        assertEquals(emptyMap<String, AppleDevice>(), viewModel.appleDevices.value)
    }

    @Test
    fun `observeのFlowに値を流すとappleDevicesが更新される`() = runTest {
        every { useCase.current() } returns BluetoothAdapter.STATE_ON

        val viewModel = SettingsViewModel(useCase, appleDevicesUseCase, overlaySettingsUseCase)

        val device = AppleDevice("AA:BB:CC:DD:EE:FF", "AirPods Pro (2nd Gen)", 0x1420, -45, 8, 9, 7)
        val devices = mapOf(device.address to device)
        fakeAppleDevicesFlow.emit(devices)
        assertEquals(devices, viewModel.appleDevices.value)
    }

    @Test
    fun `startScanでUseCaseのstartScanが呼ばれる`() {
        every { useCase.current() } returns BluetoothAdapter.STATE_ON

        val viewModel = SettingsViewModel(useCase, appleDevicesUseCase, overlaySettingsUseCase)
        viewModel.startScan()

        verify(exactly = 1) { appleDevicesUseCase.startScan() }
    }

    @Test
    fun `stopScanでUseCaseのstopScanが呼ばれる`() {
        every { useCase.current() } returns BluetoothAdapter.STATE_ON

        val viewModel = SettingsViewModel(useCase, appleDevicesUseCase, overlaySettingsUseCase)
        viewModel.stopScan()

        verify(exactly = 1) { appleDevicesUseCase.stopScan() }
    }
}
