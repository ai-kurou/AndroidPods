package kurou.androidpods.core.service

import kurou.androidpods.core.domain.AppleDevice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BatteryOverlayManagerTest {

    private lateinit var fakeDelegate: FakeOverlayViewDelegate
    private lateinit var manager: BatteryOverlayManager

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
}

private class FakeOverlayViewDelegate : OverlayViewDelegate {

    var overlayPermission = true
    var addOverlayViewCount = 0
    var removeOverlayViewCount = 0
    var animateHideCount = 0
    var updateContentCount = 0
    var lastUpdatedDevices: List<AppleDevice> = emptyList()

    private var viewAdded = false

    override val hasView: Boolean get() = viewAdded

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
}
