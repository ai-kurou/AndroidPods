package kurou.androidpods.feature.devices

import android.bluetooth.BluetoothAdapter
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.androidpods.core.domain.GetBluetoothAdapterStateUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DevicesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val fakeFlow = MutableSharedFlow<Int?>()
    private val useCase = mockk<GetBluetoothAdapterStateUseCase>()

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
    fun `初期状態にcurrentの値が反映される`() {
        every { useCase.current() } returns BluetoothAdapter.STATE_ON

        val viewModel = DevicesViewModel(useCase)

        assertEquals(BluetoothAdapter.STATE_ON, viewModel.bluetoothAdapterState.value)
    }

    @Test
    fun `observeのFlowに値を流すとbluetoothAdapterStateが更新される`() = runTest {
        every { useCase.current() } returns BluetoothAdapter.STATE_OFF

        val viewModel = DevicesViewModel(useCase)

        fakeFlow.emit(BluetoothAdapter.STATE_ON)
        assertEquals(BluetoothAdapter.STATE_ON, viewModel.bluetoothAdapterState.value)

        fakeFlow.emit(BluetoothAdapter.STATE_TURNING_OFF)
        assertEquals(BluetoothAdapter.STATE_TURNING_OFF, viewModel.bluetoothAdapterState.value)
    }

    @Test
    fun `refreshBluetoothStateでcurrentの最新値が反映される`() {
        every { useCase.current() } returns BluetoothAdapter.STATE_OFF

        val viewModel = DevicesViewModel(useCase)
        assertEquals(BluetoothAdapter.STATE_OFF, viewModel.bluetoothAdapterState.value)

        every { useCase.current() } returns BluetoothAdapter.STATE_ON
        viewModel.refreshBluetoothState()
        assertEquals(BluetoothAdapter.STATE_ON, viewModel.bluetoothAdapterState.value)
    }

    @Test
    fun `アダプタ非対応時はnullが設定される`() {
        every { useCase.current() } returns null

        val viewModel = DevicesViewModel(useCase)

        assertNull(viewModel.bluetoothAdapterState.value)
    }
}
