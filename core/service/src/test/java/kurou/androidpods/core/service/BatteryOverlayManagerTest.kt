package kurou.androidpods.core.service

import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.OverlayPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BatteryOverlayManagerTest {
    private lateinit var fakeDelegate: FakeOverlayViewDelegate
    private lateinit var manager: BatteryOverlayManager

    private val baseDevice =
        AppleDevice(
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
        fakeDelegate = FakeOverlayViewDelegate()
        manager = BatteryOverlayManager(fakeDelegate)
    }

    @Test
    fun `オーバーレイ権限がない場合はshowで何もしない`() {
        fakeDelegate.overlayPermission = false

        manager.show(listOf(baseDevice))

        assertFalse(fakeDelegate.hasView)
        assertEquals(0, fakeDelegate.addOverlayViewCount)
    }

    @Test
    fun `デバイスが空の場合はhideが呼ばれる`() {
        fakeDelegate.overlayPermission = true
        // 先にビューを表示
        manager.show(listOf(baseDevice))
        assertTrue(fakeDelegate.hasView)

        manager.show(emptyList())

        assertEquals(1, fakeDelegate.animateHideCount)
    }

    @Test
    fun `デバイスがある場合はビューが追加されコンテンツが更新される`() {
        fakeDelegate.overlayPermission = true

        manager.show(listOf(baseDevice))

        assertTrue(fakeDelegate.hasView)
        assertEquals(1, fakeDelegate.addOverlayViewCount)
        assertEquals(1, fakeDelegate.updateContentCount)
        assertEquals(listOf(baseDevice), fakeDelegate.lastUpdatedDevices)
    }

    @Test
    fun `既にビューがある場合はaddOverlayViewは呼ばれずupdateContentのみ呼ばれる`() {
        fakeDelegate.overlayPermission = true

        manager.show(listOf(baseDevice))
        val updatedDevice = baseDevice.copy(leftBattery = 8)
        manager.show(listOf(updatedDevice))

        assertEquals(1, fakeDelegate.addOverlayViewCount)
        assertEquals(2, fakeDelegate.updateContentCount)
        assertEquals(listOf(updatedDevice), fakeDelegate.lastUpdatedDevices)
    }

    @Test
    fun `hideでビューがない場合は何もしない`() {
        manager.hide()

        assertEquals(0, fakeDelegate.animateHideCount)
    }

    @Test
    fun `hideでアニメーション完了後にremoveOverlayViewが呼ばれる`() {
        fakeDelegate.overlayPermission = true
        manager.show(listOf(baseDevice))

        manager.hide()

        assertEquals(1, fakeDelegate.animateHideCount)
        assertEquals(1, fakeDelegate.removeOverlayViewCount)
        assertFalse(fakeDelegate.hasView)
    }

    @Test
    fun `hide後にshowすると再度addOverlayViewが呼ばれる`() {
        fakeDelegate.overlayPermission = true
        manager.show(listOf(baseDevice))
        manager.hide()

        manager.show(listOf(baseDevice))

        assertEquals(2, fakeDelegate.addOverlayViewCount)
    }

    @Test
    fun `ユーザーがオーバーレイを閉じた後は同じlidOpenCounterでは再表示されない`() {
        fakeDelegate.overlayPermission = true
        manager.show(listOf(baseDevice))
        assertTrue(fakeDelegate.hasView)

        // ユーザーが背景タップで閉じる
        fakeDelegate.onUserDismiss?.invoke()
        manager.hide()

        // 同じlidOpenCounterで再度検出されても表示されない
        manager.show(listOf(baseDevice))
        assertFalse(fakeDelegate.hasView)
        assertEquals(1, fakeDelegate.addOverlayViewCount)
    }

    @Test
    fun `dismiss後にlidOpenCounterが変化すると再表示される`() {
        fakeDelegate.overlayPermission = true
        manager.show(listOf(baseDevice))

        // ユーザーが閉じる
        fakeDelegate.onUserDismiss?.invoke()
        manager.hide()

        // 蓋が再度開かれた（lidOpenCounterが変化）
        val reopened = baseDevice.copy(lidOpenCounter = baseDevice.lidOpenCounter + 1)
        manager.show(listOf(reopened))
        assertTrue(fakeDelegate.hasView)
        assertEquals(2, fakeDelegate.addOverlayViewCount)
    }

    @Test
    fun `dismissedデバイスが圏外になった後に再検出されると再表示される`() {
        fakeDelegate.overlayPermission = true
        manager.show(listOf(baseDevice))

        // ユーザーが閉じる
        fakeDelegate.onUserDismiss?.invoke()
        manager.hide()

        // デバイスが圏外になる（空リストでshow）
        manager.show(emptyList())

        // 同じlidOpenCounterでも圏外後なら再表示される
        manager.show(listOf(baseDevice))
        assertTrue(fakeDelegate.hasView)
        assertEquals(2, fakeDelegate.addOverlayViewCount)
    }

    @Test
    fun `dismissedでないデバイスは引き続き表示される`() {
        fakeDelegate.overlayPermission = true
        val device2 = baseDevice.copy(modelCode = 1, modelName = "AirPods Max")

        // 2台表示中にユーザーが閉じる
        manager.show(listOf(baseDevice, device2))
        fakeDelegate.onUserDismiss?.invoke()
        manager.hide()

        // 別のデバイスのみ検出 → dismissed対象外なので表示される
        val device3 = baseDevice.copy(modelCode = 2, modelName = "Beats Solo3")
        manager.show(listOf(device3))
        assertTrue(fakeDelegate.hasView)
        assertEquals(listOf(device3), fakeDelegate.lastUpdatedDevices)
    }

    @Test
    fun `updatePositionでdelegateのupdatePositionが呼ばれる`() {
        manager.updatePosition(OverlayPosition.TOP)

        assertEquals(OverlayPosition.TOP, fakeDelegate.lastUpdatedPosition)
    }

    @Test
    fun `dismiss後に蓋を閉じて開き直すとlidOpenCounterが循環しても再表示される`() {
        fakeDelegate.overlayPermission = true
        // lidOpenCounter = 7（最大値）
        val deviceAtMax = baseDevice.copy(lidOpenCounter = 7)
        manager.show(listOf(deviceAtMax))

        fakeDelegate.onUserDismiss?.invoke()
        manager.hide()

        // カウンターが0に循環
        val deviceWrapped = baseDevice.copy(lidOpenCounter = 0)
        manager.show(listOf(deviceWrapped))
        assertTrue(fakeDelegate.hasView)
        assertEquals(2, fakeDelegate.addOverlayViewCount)
    }
}

private class FakeOverlayViewDelegate : OverlayViewDelegate {
    var overlayPermission = true
    var addOverlayViewCount = 0
    var removeOverlayViewCount = 0
    var animateHideCount = 0
    var updateContentCount = 0
    var lastUpdatedDevices: List<AppleDevice> = emptyList()
    var lastUpdatedPosition: OverlayPosition? = null

    private var viewAdded = false

    override val hasView: Boolean get() = viewAdded
    override var onUserDismiss: (() -> Unit)? = null

    override fun canDrawOverlays(): Boolean = overlayPermission

    override fun addOverlayView() {
        addOverlayViewCount++
        viewAdded = true
    }

    override fun removeOverlayView() {
        removeOverlayViewCount++
        viewAdded = false
    }

    override fun animateHide(onComplete: () -> Unit) {
        animateHideCount++
        onComplete()
    }

    override fun updateContent(devices: List<AppleDevice>) {
        updateContentCount++
        lastUpdatedDevices = devices
    }

    override fun updatePosition(position: OverlayPosition) {
        lastUpdatedPosition = position
    }
}
