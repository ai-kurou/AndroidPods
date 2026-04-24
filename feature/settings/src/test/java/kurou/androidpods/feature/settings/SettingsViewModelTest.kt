package kurou.androidpods.feature.settings

import android.bluetooth.BluetoothAdapter
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
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
import kurou.androidpods.core.domain.CheckUpdateUseCase
import kurou.androidpods.core.domain.GetAppleDevicesUseCase
import kurou.androidpods.core.domain.GetBluetoothAdapterStateUseCase
import kurou.androidpods.core.domain.GetOverlaySettingsUseCase
import kurou.androidpods.core.domain.OverlayPosition
import kurou.androidpods.core.domain.OverlayPositionUseCase
import kurou.androidpods.core.domain.ThemeMode
import kurou.androidpods.core.domain.ThemeSettings
import kurou.androidpods.core.domain.ThemeSettingsUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private lateinit var viewModel: SettingsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()
    private val fakeBluetoothFlow = MutableSharedFlow<Int?>()
    private val fakeAppleDevicesFlow = MutableStateFlow<Map<String, AppleDevice>>(emptyMap())
    private val fakeThemeSettingsFlow = MutableStateFlow(ThemeSettings())
    private val fakeOverlayPositionFlow = MutableStateFlow(OverlayPosition.BOTTOM)
    private val getBluetoothAdapterStateUseCase = mockk<GetBluetoothAdapterStateUseCase>()
    private val getAppleDevicesUseCase = mockk<GetAppleDevicesUseCase>(relaxUnitFun = true)
    private val getOverlaySettingsUseCase = mockk<GetOverlaySettingsUseCase>()
    private val checkUpdateUseCase = mockk<CheckUpdateUseCase>()
    private val themeSettingsUseCase = mockk<ThemeSettingsUseCase>()
    private val overlayPositionUseCase = mockk<OverlayPositionUseCase>(relaxUnitFun = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getBluetoothAdapterStateUseCase.observe() } returns fakeBluetoothFlow
        every { getAppleDevicesUseCase.observe() } returns fakeAppleDevicesFlow
        every { getOverlaySettingsUseCase.isEnabled() } returns false
        every { themeSettingsUseCase.observe() } returns fakeThemeSettingsFlow
        every { overlayPositionUseCase.observe() } returns fakeOverlayPositionFlow
        viewModel =
            SettingsViewModel(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `refreshOverlayStateでisEnabledの最新値が反映される`() =
        runTest {
            // combineはすべてのupstreamが1回以上emitするまで値を出さないため、
            // bluetoothFlowに初期値を流してcombineを動作可能にする
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            assertEquals(false, viewModel.uiState.value.overlayEnabled)

            every { getOverlaySettingsUseCase.isEnabled() } returns true
            viewModel.refreshOverlayState()

            assertEquals(true, viewModel.uiState.value.overlayEnabled)
            verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
            verify(exactly = 1) { getAppleDevicesUseCase.observe() }
            // コンストラクタで1回 + refreshOverlayState()で1回 = 合計2回
            verify(exactly = 2) { getOverlaySettingsUseCase.isEnabled() }
            verify(exactly = 1) { themeSettingsUseCase.observe() }
            verify(exactly = 1) { overlayPositionUseCase.observe() }
            confirmVerified(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
            )
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
        verify(exactly = 1) { themeSettingsUseCase.observe() }
        verify(exactly = 1) { overlayPositionUseCase.observe() }
        confirmVerified(
            getBluetoothAdapterStateUseCase,
            getAppleDevicesUseCase,
            getOverlaySettingsUseCase,
            checkUpdateUseCase,
            themeSettingsUseCase,
            overlayPositionUseCase,
        )
    }

    @Test
    fun `checkUpdateがtrueを返すとupdateAvailableがtrueになる`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            val version = "0.1.0"
            coEvery { checkUpdateUseCase(version) } returns true

            viewModel.checkUpdate(version)

            assertEquals(true, viewModel.uiState.value.updateAvailable)
            verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
            verify(exactly = 1) { getAppleDevicesUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.isEnabled() }
            verify(exactly = 1) { themeSettingsUseCase.observe() }
            verify(exactly = 1) { overlayPositionUseCase.observe() }
            coVerify(exactly = 1) { checkUpdateUseCase(version) }
            confirmVerified(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
            )
        }

    @Test
    fun `checkUpdateがfalseを返すとupdateAvailableがfalseのまま`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            val version = "0.1.0"
            coEvery { checkUpdateUseCase(version) } returns false

            viewModel.checkUpdate(version)

            assertEquals(false, viewModel.uiState.value.updateAvailable)
            verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
            verify(exactly = 1) { getAppleDevicesUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.isEnabled() }
            verify(exactly = 1) { themeSettingsUseCase.observe() }
            verify(exactly = 1) { overlayPositionUseCase.observe() }
            coVerify(exactly = 1) { checkUpdateUseCase(version) }
            confirmVerified(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
            )
        }

    @Test
    fun `updateThemeSettingsでUseCaseのupdateが呼ばれる`() =
        runTest {
            val settings = ThemeSettings(themeMode = ThemeMode.LIGHT, useDynamicColor = true)
            coEvery { themeSettingsUseCase.update(settings) } just Runs

            viewModel.updateThemeSettings(settings)

            verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
            verify(exactly = 1) { getAppleDevicesUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.isEnabled() }
            verify(exactly = 1) { themeSettingsUseCase.observe() }
            verify(exactly = 1) { overlayPositionUseCase.observe() }
            coVerify(exactly = 1) { themeSettingsUseCase.update(settings) }
            confirmVerified(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
            )
        }

    @Test
    fun `updateOverlayPositionでUseCaseのupdateが呼ばれる`() =
        runTest {
            viewModel.updateOverlayPosition(OverlayPosition.TOP)

            verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
            verify(exactly = 1) { getAppleDevicesUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.isEnabled() }
            verify(exactly = 1) { themeSettingsUseCase.observe() }
            verify(exactly = 1) { overlayPositionUseCase.observe() }
            coVerify(exactly = 1) { overlayPositionUseCase.update(OverlayPosition.TOP) }
            confirmVerified(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
            )
        }
}
