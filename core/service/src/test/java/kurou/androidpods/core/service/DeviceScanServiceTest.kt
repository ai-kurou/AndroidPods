package kurou.androidpods.core.service

import android.app.NotificationManager
import android.content.Context
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
    fun `onStartCommandを複数回呼んでもstartScanは1回だけ呼ばれる`() {
        controller.create().startCommand(0, 0)
        controller.startCommand(0, 0)
        controller.startCommand(0, 0)

        assertEquals(1, startScanCount)
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

    // --- createLaunchPendingIntent ---

    @Test
    fun `ランチャーActivity未登録の場合はnullを返す`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = createLaunchPendingIntent(context)
        assertEquals(null, result)
    }

    @Test
    fun `ランチャーActivityが登録済みの場合はPendingIntentを返す`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadowPm = org.robolectric.Shadows.shadowOf(context.packageManager)
        val component = android.content.ComponentName(context.packageName, "kurou.androidpods.MainActivity")
        shadowPm.addActivityIfNotPresent(component)
        shadowPm.addIntentFilterForActivity(
            component,
            android.content.IntentFilter(android.content.Intent.ACTION_MAIN).apply {
                addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            },
        )

        val result = createLaunchPendingIntent(context)
        assertNotNull(result)
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
        assertEquals("100%", batteryText(10))
        assertEquals("100%", batteryText(15))
    }

    @Test
    fun `充電中フラグがtrueの場合はイナズママークが付く`() {
        assertEquals("55%⚡", batteryText(5, charging = true))
        assertEquals("--⚡", batteryText(null, charging = true))
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

    // --- batteryIconRes ---

    @Test
    fun `バッテリーがnullの場合はnullアイコンを返す`() {
        assertEquals(R.drawable.icon_battery_null, batteryIconRes(null, false))
        assertEquals(R.drawable.icon_battery_null, batteryIconRes(null, true))
    }

    @Test
    fun `非充電時の各レベルで正しいアイコンを返す`() {
        assertEquals(R.drawable.icon_battery_5_19, batteryIconRes(0, false))   // 5%
        assertEquals(R.drawable.icon_battery_5_19, batteryIconRes(1, false))   // 15%
        assertEquals(R.drawable.icon_battery_20_39, batteryIconRes(2, false))  // 25%
        assertEquals(R.drawable.icon_battery_20_39, batteryIconRes(3, false))  // 35%
        assertEquals(R.drawable.icon_battery_40_59, batteryIconRes(4, false))  // 45%
        assertEquals(R.drawable.icon_battery_40_59, batteryIconRes(5, false))  // 55%
        assertEquals(R.drawable.icon_battery_60_79, batteryIconRes(6, false))  // 65%
        assertEquals(R.drawable.icon_battery_60_79, batteryIconRes(7, false))  // 75%
        assertEquals(R.drawable.icon_battery_80_94, batteryIconRes(8, false))  // 85%
        assertEquals(R.drawable.icon_battery_95_100, batteryIconRes(9, false)) // 95%
    }

    @Test
    fun `充電中の各レベルで正しいアイコンを返す`() {
        assertEquals(R.drawable.icon_battery_charging_0_19, batteryIconRes(0, true))   // 5%
        assertEquals(R.drawable.icon_battery_charging_0_19, batteryIconRes(1, true))   // 15%
        assertEquals(R.drawable.icon_battery_charging_20_39, batteryIconRes(2, true))  // 25%
        assertEquals(R.drawable.icon_battery_charging_20_39, batteryIconRes(3, true))  // 35%
        assertEquals(R.drawable.icon_battery_charging_40_59, batteryIconRes(4, true))  // 45%
        assertEquals(R.drawable.icon_battery_charging_40_59, batteryIconRes(5, true))  // 55%
        assertEquals(R.drawable.icon_battery_charging_60_79, batteryIconRes(6, true))  // 65%
        assertEquals(R.drawable.icon_battery_charging_60_79, batteryIconRes(7, true))  // 75%
        assertEquals(R.drawable.icon_battery_charging_80_94, batteryIconRes(8, true))  // 85%
        assertEquals(R.drawable.icon_battery_charging_95_99, batteryIconRes(9, true))  // 95%
    }

    @Test
    fun `レベル10以上は満充電アイコンを返す`() {
        assertEquals(R.drawable.icon_battery_95_100, batteryIconRes(10, false))
        assertEquals(R.drawable.icon_battery_95_100, batteryIconRes(15, false))
        assertEquals(R.drawable.icon_battery_charging_100, batteryIconRes(10, true))
        assertEquals(R.drawable.icon_battery_charging_100, batteryIconRes(15, true))
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
    fun `折りたたみビューが正しいレイアウトを使用する`() {
        val devices = listOf(
            baseDevice.copy(modelName = "Device A"),
            baseDevice.copy(modelName = "Device B"),
        )
        val collapsedView = buildCollapsedRemoteViews(testPackageName, devices)
        assertEquals(R.layout.notification_collapsed, collapsedView.layoutId)
    }

    @Test
    fun `showChargingがtrueの場合は充電中マークが付く`() {
        val device = baseDevice.copy(leftBattery = 5, leftCharging = true)
        val result = formatDevicesSummary(listOf(device), showCharging = true)
        assertTrue(result.contains("⚡"))
    }

    @Test
    fun `showChargingがfalseの場合は充電中マークが付かない`() {
        val device = baseDevice.copy(leftBattery = 5, leftCharging = true)
        val result = formatDevicesSummary(listOf(device), showCharging = false)
        assertTrue(!result.contains("⚡"))
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
