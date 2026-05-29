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
import kotlinx.coroutines.launch
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
import kurou.androidpods.core.domain.RssiThreshold
import kurou.androidpods.core.domain.RssiThresholdUseCase
import kurou.androidpods.core.domain.ThemeMode
import kurou.androidpods.core.domain.ThemeSettings
import kurou.androidpods.core.domain.ThemeSettingsUseCase
import kurou.androidpods.core.domain.UnknownDeviceUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    private val fakeOverlayEnabledFlow = MutableStateFlow(false)
    private val fakeUnknownModelCodesFlow = MutableStateFlow<Set<String>>(emptySet())
    private val fakeRssiThresholdFlow = MutableStateFlow(RssiThreshold.VERY_NEAR)
    private val getBluetoothAdapterStateUseCase = mockk<GetBluetoothAdapterStateUseCase>()
    private val getAppleDevicesUseCase = mockk<GetAppleDevicesUseCase>(relaxUnitFun = true)
    private val getOverlaySettingsUseCase = mockk<GetOverlaySettingsUseCase>(relaxUnitFun = true)
    private val checkUpdateUseCase = mockk<CheckUpdateUseCase>()
    private val themeSettingsUseCase = mockk<ThemeSettingsUseCase>()
    private val overlayPositionUseCase = mockk<OverlayPositionUseCase>(relaxUnitFun = true)
    private val unknownDeviceUseCase = mockk<UnknownDeviceUseCase>()
    private val rssiThresholdUseCase = mockk<RssiThresholdUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getBluetoothAdapterStateUseCase.observe() } returns fakeBluetoothFlow
        every { getAppleDevicesUseCase.observe() } returns fakeAppleDevicesFlow
        every { getOverlaySettingsUseCase.observe() } returns fakeOverlayEnabledFlow
        every { themeSettingsUseCase.observe() } returns fakeThemeSettingsFlow
        every { overlayPositionUseCase.observe() } returns fakeOverlayPositionFlow
        every { unknownDeviceUseCase.observe() } returns fakeUnknownModelCodesFlow
        every { rssiThresholdUseCase.observe() } returns fakeRssiThresholdFlow
        viewModel =
            SettingsViewModel(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
                unknownDeviceUseCase,
                rssiThresholdUseCase,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `checkUpdateがResult_success_trueを返すとupdateAvailableがtrueになる`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            val version = "0.1.0"
            coEvery { checkUpdateUseCase(version) } returns Result.success(true)

            viewModel.checkUpdate(version)

            assertEquals(true, viewModel.uiState.value.updateAvailable)
            verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
            verify(exactly = 1) { getAppleDevicesUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.observe() }
            verify(exactly = 1) { themeSettingsUseCase.observe() }
            verify(exactly = 1) { overlayPositionUseCase.observe() }
            verify(exactly = 1) { unknownDeviceUseCase.observe() }
            verify(exactly = 1) { rssiThresholdUseCase.observe() }
            coVerify(exactly = 1) { checkUpdateUseCase(version) }
            confirmVerified(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
                unknownDeviceUseCase,
                rssiThresholdUseCase,
            )
        }

    @Test
    fun `checkUpdateがResult_success_falseを返すとupdateAvailableがfalseのまま`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            val version = "0.1.0"
            coEvery { checkUpdateUseCase(version) } returns Result.success(false)

            viewModel.checkUpdate(version)

            assertEquals(false, viewModel.uiState.value.updateAvailable)
            verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
            verify(exactly = 1) { getAppleDevicesUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.observe() }
            verify(exactly = 1) { themeSettingsUseCase.observe() }
            verify(exactly = 1) { overlayPositionUseCase.observe() }
            verify(exactly = 1) { unknownDeviceUseCase.observe() }
            verify(exactly = 1) { rssiThresholdUseCase.observe() }
            coVerify(exactly = 1) { checkUpdateUseCase(version) }
            confirmVerified(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
                unknownDeviceUseCase,
                rssiThresholdUseCase,
            )
        }

    @Test
    fun `checkUpdateがResult_failureを返すとupdateAvailableがfalseのまま`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            val version = "0.1.0"
            coEvery { checkUpdateUseCase(version) } returns Result.failure(Exception("network error"))

            viewModel.checkUpdate(version)

            assertEquals(false, viewModel.uiState.value.updateAvailable)
            verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
            verify(exactly = 1) { getAppleDevicesUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.observe() }
            verify(exactly = 1) { themeSettingsUseCase.observe() }
            verify(exactly = 1) { overlayPositionUseCase.observe() }
            verify(exactly = 1) { unknownDeviceUseCase.observe() }
            verify(exactly = 1) { rssiThresholdUseCase.observe() }
            coVerify(exactly = 1) { checkUpdateUseCase(version) }
            confirmVerified(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
                unknownDeviceUseCase,
                rssiThresholdUseCase,
            )
        }

    @Test
    fun `refreshOverlayStateを呼ぶとUseCaseのrefreshが呼ばれuiStateに反映される`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            assertEquals(false, viewModel.uiState.value.overlayEnabled)

            fakeOverlayEnabledFlow.emit(true)
            viewModel.refreshOverlayState()

            assertEquals(true, viewModel.uiState.value.overlayEnabled)
            verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
            verify(exactly = 1) { getAppleDevicesUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.refresh() }
            verify(exactly = 1) { themeSettingsUseCase.observe() }
            verify(exactly = 1) { overlayPositionUseCase.observe() }
            verify(exactly = 1) { unknownDeviceUseCase.observe() }
            verify(exactly = 1) { rssiThresholdUseCase.observe() }
            confirmVerified(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
                unknownDeviceUseCase,
                rssiThresholdUseCase,
            )
        }

    @Test
    fun `refreshNotificationStateでisNotificationsDisabledが更新される`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            assertEquals(false, viewModel.uiState.value.isNotificationsDisabled)

            viewModel.refreshNotificationState(isDisabled = true)

            assertEquals(true, viewModel.uiState.value.isNotificationsDisabled)
            verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
            verify(exactly = 1) { getAppleDevicesUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.observe() }
            verify(exactly = 1) { themeSettingsUseCase.observe() }
            verify(exactly = 1) { overlayPositionUseCase.observe() }
            verify(exactly = 1) { unknownDeviceUseCase.observe() }
            verify(exactly = 1) { rssiThresholdUseCase.observe() }
            confirmVerified(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
                unknownDeviceUseCase,
                rssiThresholdUseCase,
            )
        }

    @Test
    fun `refreshDeviceScanChannelStateでisDeviceScanChannelDisabledが更新される`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            assertEquals(false, viewModel.uiState.value.isDeviceScanChannelDisabled)

            viewModel.refreshDeviceScanChannelState(isDisabled = true)

            assertEquals(true, viewModel.uiState.value.isDeviceScanChannelDisabled)
            verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
            verify(exactly = 1) { getAppleDevicesUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.observe() }
            verify(exactly = 1) { themeSettingsUseCase.observe() }
            verify(exactly = 1) { overlayPositionUseCase.observe() }
            verify(exactly = 1) { unknownDeviceUseCase.observe() }
            verify(exactly = 1) { rssiThresholdUseCase.observe() }
            confirmVerified(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
                unknownDeviceUseCase,
                rssiThresholdUseCase,
            )
        }

    @Test
    fun `refreshBatteryOptimizationStateでisBatteryOptimizationExemptが更新される`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            assertEquals(false, viewModel.uiState.value.isBatteryOptimizationExempt)

            viewModel.refreshBatteryOptimizationState(isExempt = true)

            assertEquals(true, viewModel.uiState.value.isBatteryOptimizationExempt)
            verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
            verify(exactly = 1) { getAppleDevicesUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.observe() }
            verify(exactly = 1) { themeSettingsUseCase.observe() }
            verify(exactly = 1) { overlayPositionUseCase.observe() }
            verify(exactly = 1) { unknownDeviceUseCase.observe() }
            verify(exactly = 1) { rssiThresholdUseCase.observe() }
            confirmVerified(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
                unknownDeviceUseCase,
                rssiThresholdUseCase,
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
            verify(exactly = 1) { getOverlaySettingsUseCase.observe() }
            verify(exactly = 1) { themeSettingsUseCase.observe() }
            verify(exactly = 1) { overlayPositionUseCase.observe() }
            verify(exactly = 1) { unknownDeviceUseCase.observe() }
            verify(exactly = 1) { rssiThresholdUseCase.observe() }
            coVerify(exactly = 1) { themeSettingsUseCase.update(settings) }
            confirmVerified(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
                unknownDeviceUseCase,
                rssiThresholdUseCase,
            )
        }

    @Test
    fun `updateOverlayPositionでUseCaseのupdateが呼ばれる`() =
        runTest {
            viewModel.updateOverlayPosition(OverlayPosition.TOP)

            verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
            verify(exactly = 1) { getAppleDevicesUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.observe() }
            verify(exactly = 1) { themeSettingsUseCase.observe() }
            verify(exactly = 1) { overlayPositionUseCase.observe() }
            verify(exactly = 1) { unknownDeviceUseCase.observe() }
            verify(exactly = 1) { rssiThresholdUseCase.observe() }
            coVerify(exactly = 1) { overlayPositionUseCase.update(OverlayPosition.TOP) }
            confirmVerified(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
                unknownDeviceUseCase,
                rssiThresholdUseCase,
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
        verify(exactly = 1) { getOverlaySettingsUseCase.observe() }
        verify(exactly = 1) { themeSettingsUseCase.observe() }
        verify(exactly = 1) { overlayPositionUseCase.observe() }
        verify(exactly = 1) { unknownDeviceUseCase.observe() }
        verify(exactly = 1) { rssiThresholdUseCase.observe() }
        confirmVerified(
            getBluetoothAdapterStateUseCase,
            getAppleDevicesUseCase,
            getOverlaySettingsUseCase,
            checkUpdateUseCase,
            themeSettingsUseCase,
            overlayPositionUseCase,
            unknownDeviceUseCase,
            rssiThresholdUseCase,
        )
    }

    @Test
    fun `showPermissionRequiredDialogを呼ぶとuiState_showPermissionRequiredDialogがtrueになる`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            viewModel.showPermissionRequiredDialog()
            assertTrue(viewModel.uiState.value.showPermissionRequiredDialog)
        }

    @Test
    fun `dismissPermissionRequiredDialogを呼ぶとuiState_showPermissionRequiredDialogがfalseになる`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            viewModel.showPermissionRequiredDialog()
            viewModel.dismissPermissionRequiredDialog()
            assertFalse(viewModel.uiState.value.showPermissionRequiredDialog)
        }

    @Test
    fun `showThemeModeDialogを呼ぶとuiState_showThemeModeDialogがtrueになる`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            viewModel.showThemeModeDialog()
            assertTrue(viewModel.uiState.value.showThemeModeDialog)
        }

    @Test
    fun `dismissThemeModeDialogを呼ぶとuiState_showThemeModeDialogがfalseになる`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            viewModel.showThemeModeDialog()
            viewModel.dismissThemeModeDialog()
            assertFalse(viewModel.uiState.value.showThemeModeDialog)
        }

    @Test
    fun `showOverlayPositionDialogを呼ぶとuiState_showOverlayPositionDialogがtrueになる`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            viewModel.showOverlayPositionDialog()
            assertTrue(viewModel.uiState.value.showOverlayPositionDialog)
        }

    @Test
    fun `dismissOverlayPositionDialogを呼ぶとuiState_showOverlayPositionDialogがfalseになる`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            viewModel.showOverlayPositionDialog()
            viewModel.dismissOverlayPositionDialog()
            assertFalse(viewModel.uiState.value.showOverlayPositionDialog)
        }

    @Test
    fun `showUnknownDeviceSheetとdismissUnknownDeviceSheetでシートの表示状態が切り替わる`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)

            viewModel.showUnknownDeviceSheet()
            assertTrue(viewModel.uiState.value.showUnknownDeviceSheet)

            viewModel.dismissUnknownDeviceSheet()
            assertFalse(viewModel.uiState.value.showUnknownDeviceSheet)
        }

    @Test
    fun `シートを表示後に報告するとUseCaseのreportが呼ばれシートが閉じる`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            coEvery { unknownDeviceUseCase.report(0x1234, "AirPods Max") } just Runs

            viewModel.showUnknownDeviceSheet()
            assertTrue(viewModel.uiState.value.showUnknownDeviceSheet)

            viewModel.reportUnknownDevice("0x1234", "AirPods Max")

            coVerify(exactly = 1) { unknownDeviceUseCase.report(0x1234, "AirPods Max") }
            assertFalse(viewModel.uiState.value.showUnknownDeviceSheet)
        }

    @Test
    fun `権限なしでsetOverlayEnabled_trueを呼ぶとsetEnabled_trueが呼ばれる`() =
        runTest {
            every { getOverlaySettingsUseCase.hasPermission() } returns false
            coEvery { getOverlaySettingsUseCase.setEnabled(any()) } just Runs

            viewModel.setOverlayEnabled(true)

            verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
            verify(exactly = 1) { getAppleDevicesUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.hasPermission() }
            coVerify(exactly = 1) { getOverlaySettingsUseCase.setEnabled(true) }
            verify(exactly = 1) { themeSettingsUseCase.observe() }
            verify(exactly = 1) { overlayPositionUseCase.observe() }
            verify(exactly = 1) { unknownDeviceUseCase.observe() }
            verify(exactly = 1) { rssiThresholdUseCase.observe() }
            confirmVerified(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
                unknownDeviceUseCase,
                rssiThresholdUseCase,
            )
        }

    @Test
    fun `権限ありでsetOverlayEnabledを呼ぶとUseCaseのsetEnabledが呼ばれる`() =
        runTest {
            every { getOverlaySettingsUseCase.hasPermission() } returns true
            coEvery { getOverlaySettingsUseCase.setEnabled(any()) } just Runs

            viewModel.setOverlayEnabled(true)
            viewModel.setOverlayEnabled(false)

            verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
            verify(exactly = 1) { getAppleDevicesUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.hasPermission() }
            coVerify(exactly = 1) { getOverlaySettingsUseCase.setEnabled(true) }
            coVerify(exactly = 1) { getOverlaySettingsUseCase.setEnabled(false) }
            verify(exactly = 1) { themeSettingsUseCase.observe() }
            verify(exactly = 1) { overlayPositionUseCase.observe() }
            verify(exactly = 1) { unknownDeviceUseCase.observe() }
            verify(exactly = 1) { rssiThresholdUseCase.observe() }
            confirmVerified(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
                unknownDeviceUseCase,
                rssiThresholdUseCase,
            )
        }

    @Test
    fun `showRssiThresholdDialogを呼び出してupdateRssiThresholdを呼び出すとUseCaseのupdateが呼ばれダイアログが閉じる`() =
        runTest {
            fakeBluetoothFlow.emit(BluetoothAdapter.STATE_ON)
            coEvery { rssiThresholdUseCase.update(RssiThreshold.NEAR) } just Runs

            viewModel.showRssiThresholdDialog()
            assertTrue(viewModel.uiState.value.showRssiThresholdDialog)
            viewModel.updateRssiThreshold(RssiThreshold.NEAR)

            assertFalse(viewModel.uiState.value.showRssiThresholdDialog)
            verify(exactly = 1) { getBluetoothAdapterStateUseCase.observe() }
            verify(exactly = 1) { getAppleDevicesUseCase.observe() }
            verify(exactly = 1) { getOverlaySettingsUseCase.observe() }
            verify(exactly = 1) { themeSettingsUseCase.observe() }
            verify(exactly = 1) { overlayPositionUseCase.observe() }
            verify(exactly = 1) { unknownDeviceUseCase.observe() }
            verify(exactly = 1) { rssiThresholdUseCase.observe() }
            coVerify(exactly = 1) { rssiThresholdUseCase.update(RssiThreshold.NEAR) }
            confirmVerified(
                getBluetoothAdapterStateUseCase,
                getAppleDevicesUseCase,
                getOverlaySettingsUseCase,
                checkUpdateUseCase,
                themeSettingsUseCase,
                overlayPositionUseCase,
                unknownDeviceUseCase,
                rssiThresholdUseCase,
            )
        }

    @Test
    fun `権限なしでsetOverlayEnabled_trueを呼ぶとOpenOverlayPermissionSettingsイベントが発行される`() =
        runTest {
            every { getOverlaySettingsUseCase.hasPermission() } returns false
            coEvery { getOverlaySettingsUseCase.setEnabled(any()) } just Runs
            val events = mutableListOf<ServiceEvent>()
            val job = launch(testDispatcher) { viewModel.serviceEvents.collect { events.add(it) } }

            viewModel.setOverlayEnabled(true)

            assertEquals(ServiceEvent.OpenOverlayPermissionSettings, events.first())
            job.cancel()
        }
}
