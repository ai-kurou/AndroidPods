package kurou.androidpods.core.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.androidpods.core.data.DataModule
import kurou.androidpods.core.domain.AppleDevice
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@UninstallModules(DataModule::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = HiltTestApplication::class)
class DeviceScanServiceTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var controller: ServiceController<DeviceScanService>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        resetFakeRepository()
        hiltRule.inject()
        controller = Robolectric.buildService(DeviceScanService::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onStartCommandでstartScanが呼ばれる`() {
        controller.create().startCommand(0, 0)

        assertTrue(startScanCalled)
    }

    @Test
    fun `onDestroyでstopScanが呼ばれる`() {
        controller.create().startCommand(0, 0)
        controller.destroy()

        assertTrue(stopScanCalled)
    }

    @Test
    fun `onStartCommandで通知が作成される`() {
        controller.create().startCommand(0, 0)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val nm = context.getSystemService(NotificationManager::class.java)
        val notifications = nm.activeNotifications

        assertEquals(1, notifications.size)
        assertNotNull(notifications[0])
    }

    @Test
    fun `デバイス更新時に通知テキストが更新される`() {
        controller.create().startCommand(0, 0)

        val device = AppleDevice(
            address = "AA:BB:CC:DD:EE:FF",
            modelName = "AirPods Pro",
            modelCode = 0,
            rssi = -50,
            leftBattery = 8,
            rightBattery = 7,
            caseBattery = 5,
        )
        fakeDevicesFlow.tryEmit(mapOf(device.address to device))

        val context = ApplicationProvider.getApplicationContext<Context>()
        val nm = context.getSystemService(NotificationManager::class.java)
        val notification = nm.activeNotifications.first().notification
        val extras = notification.extras
        val text = extras.getCharSequence("android.text")?.toString()

        assertTrue(text?.contains("AirPods Pro") == true)
    }

    // --- formatDevicesSummary / batteryText ---

    private fun device(
        modelName: String = "AirPods Pro",
        leftBattery: Int? = 5,
        rightBattery: Int? = 5,
        caseBattery: Int? = 5,
        isSingle: Boolean = false,
        leftCharging: Boolean = false,
        rightCharging: Boolean = false,
        caseCharging: Boolean = false,
    ) = AppleDevice(
        address = "00:00:00:00:00:00",
        modelName = modelName,
        modelCode = 0,
        rssi = -50,
        leftBattery = leftBattery,
        rightBattery = rightBattery,
        caseBattery = caseBattery,
        isSingle = isSingle,
        leftCharging = leftCharging,
        rightCharging = rightCharging,
        caseCharging = caseCharging,
    )

    @Test
    fun `デバイスが空の場合はNo Apple devices foundを返す`() {
        assertEquals("No Apple devices found", formatDevicesSummary(emptyList()))
    }

    @Test
    fun `singleデバイスはleftBatteryのみ表示する`() {
        val result = formatDevicesSummary(listOf(device(isSingle = true, leftBattery = 5)))
        assertEquals("AirPods Pro — 55%", result)
    }

    @Test
    fun `非singleデバイスはL R Caseを表示する`() {
        val result = formatDevicesSummary(
            listOf(device(leftBattery = 5, rightBattery = 7, caseBattery = 3)),
        )
        assertEquals("AirPods Pro — L:55% R:75% Case:35%", result)
    }

    @Test
    fun `バッテリーがnullの場合はハイフンを表示する`() {
        val result = formatDevicesSummary(
            listOf(device(leftBattery = null, rightBattery = null, caseBattery = null)),
        )
        assertEquals("AirPods Pro — L:-- R:-- Case:--", result)
    }

    @Test
    fun `バッテリーが10以上の場合は100パーセントを表示する`() {
        assertEquals("100%", batteryText(10, false))
        assertEquals("100%", batteryText(15, false))
    }

    @Test
    fun `充電中はイナズママークが付く`() {
        assertEquals("55%⚡", batteryText(5, true))
        assertEquals("--⚡", batteryText(null, true))
    }

    @Test
    fun `複数デバイスは改行区切りで表示する`() {
        val devices = listOf(
            device(modelName = "AirPods Pro", isSingle = false, leftBattery = 5, rightBattery = 7, caseBattery = 3),
            device(modelName = "AirPods Max", isSingle = true, leftBattery = 8),
        )
        val result = formatDevicesSummary(devices)
        assertEquals("AirPods Pro — L:55% R:75% Case:35%\nAirPods Max — 85%", result)
    }
}
