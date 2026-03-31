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
import kurou.androidpods.core.domain.DeviceImages
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

    private val baseDevice = AppleDevice(
        address = "00:00:00:00:00:00",
        modelName = "AirPods Pro",
        modelCode = 0,
        rssi = -50,
        leftBattery = 5,
        rightBattery = 5,
        caseBattery = 5,
    )

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

        val device = baseDevice.copy(
            address = "AA:BB:CC:DD:EE:FF",
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

    @Test
    fun `デバイスが空の場合はNo Apple devices foundを返す`() {
        assertEquals("No Apple devices found", formatDevicesSummary(emptyList()))
    }

    @Test
    fun `singleデバイスはleftBatteryのみ表示する`() {
        val result = formatDevicesSummary(listOf(baseDevice.copy(isSingle = true)))
        assertEquals("AirPods Pro — 55%", result)
    }

    @Test
    fun `非singleデバイスはL R Caseを表示する`() {
        val result = formatDevicesSummary(
            listOf(baseDevice.copy(leftBattery = 5, rightBattery = 7, caseBattery = 3)),
        )
        assertEquals("AirPods Pro — L:55% R:75% Case:35%", result)
    }

    @Test
    fun `バッテリーがnullの場合はハイフンを表示する`() {
        val result = formatDevicesSummary(
            listOf(baseDevice.copy(leftBattery = null, rightBattery = null, caseBattery = null)),
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
            baseDevice.copy(leftBattery = 5, rightBattery = 7, caseBattery = 3),
            baseDevice.copy(modelName = "AirPods Max", isSingle = true, leftBattery = 8),
        )
        val result = formatDevicesSummary(devices)
        assertEquals("AirPods Pro — L:55% R:75% Case:35%\nAirPods Max — 85%", result)
    }

    // --- buildDeviceRemoteViews / buildExpandedRemoteViews ---

    private val testPackageName = "kurou.androidpods.core.service.test"

    @Test
    fun `TWSデバイスのRemoteViewsが正しいレイアウトを使用する`() {
        val tws = baseDevice.copy(
            images = DeviceImages.Tws(
                left = android.R.drawable.ic_menu_gallery,
                right = android.R.drawable.ic_menu_gallery,
                case = android.R.drawable.ic_menu_gallery,
            ),
        )
        val remoteViews = buildDeviceRemoteViews(testPackageName, tws)
        assertEquals(R.layout.notification_device_tws, remoteViews.layoutId)
    }

    @Test
    fun `SingleデバイスのRemoteViewsが正しいレイアウトを使用する`() {
        val single = baseDevice.copy(
            isSingle = true,
            images = DeviceImages.Single(
                body = android.R.drawable.ic_menu_gallery,
            ),
        )
        val remoteViews = buildDeviceRemoteViews(testPackageName, single)
        assertEquals(R.layout.notification_device_single, remoteViews.layoutId)
    }

    @Test
    fun `画像なしデバイスのRemoteViewsがテキストのみレイアウトを使用する`() {
        val remoteViews = buildDeviceRemoteViews(testPackageName, baseDevice)
        assertEquals(R.layout.notification_device_text_only, remoteViews.layoutId)
    }

    @Test
    fun `展開ビューが全デバイス分の子ビューを含む`() {
        val devices = listOf(
            baseDevice.copy(modelName = "Device A"),
            baseDevice.copy(modelName = "Device B"),
            baseDevice.copy(modelName = "Device C"),
        )
        val expandedView = buildExpandedRemoteViews(testPackageName, devices)
        assertEquals(R.layout.notification_expanded, expandedView.layoutId)
    }

    @Test
    fun `デバイス更新時に展開ビューが通知に設定される`() {
        controller.create().startCommand(0, 0)

        val tws = baseDevice.copy(
            address = "AA:BB:CC:DD:EE:FF",
            leftBattery = 8,
            rightBattery = 7,
            caseBattery = 5,
            images = DeviceImages.Tws(
                left = android.R.drawable.ic_menu_gallery,
                right = android.R.drawable.ic_menu_gallery,
                case = android.R.drawable.ic_menu_gallery,
            ),
        )
        fakeDevicesFlow.tryEmit(mapOf(tws.address to tws))

        val context = ApplicationProvider.getApplicationContext<Context>()
        val nm = context.getSystemService(NotificationManager::class.java)
        val notification = nm.activeNotifications.first().notification

        @Suppress("DEPRECATION")
        val bigContentView = notification.bigContentView
        assertNotNull(bigContentView)
    }
}
