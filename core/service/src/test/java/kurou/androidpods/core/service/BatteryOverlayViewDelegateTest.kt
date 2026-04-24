package kurou.androidpods.core.service

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.DeviceImages
import kurou.androidpods.core.domain.OverlayPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BatteryOverlayViewDelegateTest {
    private lateinit var fakeWindowOps: FakeOverlayWindowOperations
    private lateinit var delegate: BatteryOverlayViewDelegate

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
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        fakeWindowOps = FakeOverlayWindowOperations()
        delegate = BatteryOverlayViewDelegate(context, fakeWindowOps)
    }

    @Test
    fun `初期状態ではhasViewはfalse`() {
        assertFalse(delegate.hasView)
    }

    @Test
    fun `addOverlayView後にhasViewがtrueになる`() {
        delegate.addOverlayView()

        assertTrue(delegate.hasView)
        assertEquals(1, fakeWindowOps.addViewCount)
    }

    @Test
    fun `removeOverlayView後にhasViewがfalseになる`() {
        delegate.addOverlayView()

        delegate.removeOverlayView()

        assertFalse(delegate.hasView)
        assertEquals(1, fakeWindowOps.removeViewCount)
    }

    @Test
    fun `removeOverlayViewはviewがない場合は何もしない`() {
        delegate.removeOverlayView()

        assertEquals(0, fakeWindowOps.removeViewCount)
    }

    @Test
    fun `canDrawOverlaysはwindowOpsに委譲される`() {
        fakeWindowOps.canDrawOverlaysResult = true
        assertTrue(delegate.canDrawOverlays())

        fakeWindowOps.canDrawOverlaysResult = false
        assertFalse(delegate.canDrawOverlays())
    }

    @Test
    fun `updateContentでカードが追加される`() {
        delegate.addOverlayView()

        delegate.updateContent(listOf(baseDevice))

        // addOverlayViewでwindowOpsにaddされたviewが存在する
        val rootView = fakeWindowOps.lastAddedView
        assertNotNull(rootView)
    }

    @Test
    fun `animateHide中にupdateContentが呼ばれるとアニメーションがキャンセルされviewが保持される`() {
        delegate.addOverlayView()
        delegate.updateContent(listOf(baseDevice))

        // animateHide開始（アニメーション進行中の状態）
        var hideCompleted = false
        delegate.animateHide { hideCompleted = true }

        // アニメーション完了前にupdateContentが呼ばれる
        delegate.updateContent(listOf(baseDevice))

        // viewは保持されている
        assertTrue(delegate.hasView)

        // Robolectricでアニメーションを進めてもonCompleteは呼ばれない（リスナー除去済み）
        shadowOf(android.os.Looper.getMainLooper()).idle()
        assertFalse(hideCompleted)
        assertTrue(delegate.hasView)
    }

    @Test
    fun `animateHide完了後にonCompleteが呼ばれる`() {
        delegate.addOverlayView()

        var hideCompleted = false
        delegate.animateHide { hideCompleted = true }

        // Robolectricでアニメーションを完了させる
        shadowOf(android.os.Looper.getMainLooper()).idle()

        assertTrue(hideCompleted)
    }

    @Test
    fun `removeOverlayViewは進行中のhideアニメーションをキャンセルする`() {
        delegate.addOverlayView()

        var hideCompleted = false
        delegate.animateHide { hideCompleted = true }

        // アニメーション完了前にremoveOverlayView
        delegate.removeOverlayView()

        assertFalse(delegate.hasView)

        // アニメーションが進んでもonCompleteは呼ばれない
        shadowOf(android.os.Looper.getMainLooper()).idle()
        assertFalse(hideCompleted)
    }

    @Test
    fun `addOverlayView後にviewのalphaとscaleが初期アニメーション値で設定される`() {
        delegate.addOverlayView()

        val view = fakeWindowOps.lastAddedView!!
        assertEquals(0f, view.alpha)
        assertEquals(0.85f, view.scaleX)
        assertEquals(0.85f, view.scaleY)
    }

    @Test
    fun `addOverlayViewのアニメーション完了後にalphaが1でscaleが1になる`() {
        delegate.addOverlayView()

        shadowOf(android.os.Looper.getMainLooper()).idle()

        val view = fakeWindowOps.lastAddedView!!
        assertEquals(1f, view.alpha)
        assertEquals(1f, view.scaleX)
        assertEquals(1f, view.scaleY)
    }

    @Test
    fun `animateHide完了後にviewのalphaが0でscaleが縮小される`() {
        delegate.addOverlayView()
        shadowOf(android.os.Looper.getMainLooper()).idle()

        delegate.animateHide {}
        shadowOf(android.os.Looper.getMainLooper()).idle()

        // onComplete呼び出し後もviewが残っている（removeはonComplete側の責務）
        // スケールとalphaがアニメーション終端値になっている
        val view = fakeWindowOps.lastAddedView!!
        assertEquals(0f, view.alpha)
        assertEquals(0.85f, view.scaleX)
        assertEquals(0.85f, view.scaleY)
    }

    @Test
    fun `animateHideはviewがない場合にonCompleteを呼ばない`() {
        var called = false
        delegate.animateHide { called = true }

        shadowOf(android.os.Looper.getMainLooper()).idle()

        assertFalse(called)
    }

    @Test
    fun `updateContentが空リストのとき全カードが削除される`() {
        delegate.addOverlayView()
        delegate.updateContent(listOf(baseDevice))
        assertEquals(1, findCardsContainer().childCount)

        delegate.updateContent(emptyList())

        assertEquals(0, findCardsContainer().childCount)
    }

    @Test
    fun `animateHideキャンセル後のupdateContentでscaleとalphaがリセットされる`() {
        delegate.addOverlayView()
        shadowOf(android.os.Looper.getMainLooper()).idle()

        delegate.animateHide {}
        // アニメーション完了前にupdateContent
        delegate.updateContent(listOf(baseDevice))

        val view = fakeWindowOps.lastAddedView!!
        assertEquals(1f, view.alpha)
        assertEquals(1f, view.scaleX)
        assertEquals(1f, view.scaleY)
    }

    @Test
    fun `onUserDismissコールバックが設定され呼び出し可能`() {
        var dismissed = false
        delegate.onUserDismiss = { dismissed = true }

        delegate.onUserDismiss?.invoke()

        assertTrue(dismissed)
    }

    @Test
    fun `addOverlayViewでWindowManagerにviewが追加される`() {
        delegate.addOverlayView()

        assertEquals(1, fakeWindowOps.addViewCount)
        assertNotNull(fakeWindowOps.lastAddedView)
        assertNotNull(fakeWindowOps.lastAddedParams)
    }

    @Test
    fun `addOverlayViewのデフォルトgravityがBOTTOM`() {
        delegate.addOverlayView()

        val params = fakeWindowOps.lastAddedParams!!
        assertEquals(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, params.gravity)
    }

    @Test
    fun `updatePositionでTOPに設定してaddOverlayViewするとgravityがTOP`() {
        delegate.updatePosition(OverlayPosition.TOP)
        delegate.addOverlayView()

        val params = fakeWindowOps.lastAddedParams!!
        assertEquals(Gravity.TOP or Gravity.CENTER_HORIZONTAL, params.gravity)
    }

    @Test
    fun `表示中にupdatePositionを呼ぶとviewのgravityが更新される`() {
        delegate.addOverlayView()

        delegate.updatePosition(OverlayPosition.TOP)

        val params = fakeWindowOps.lastUpdatedParams!!
        assertEquals(Gravity.TOP or Gravity.CENTER_HORIZONTAL, params.gravity)
        assertEquals(1, fakeWindowOps.updateLayoutCount)
    }

    @Test
    fun `addOverlayViewのLayoutParamsがWRAP_CONTENTでTYPE_APPLICATION_OVERLAY`() {
        delegate.addOverlayView()

        val params = fakeWindowOps.lastAddedParams!!
        assertEquals(WindowManager.LayoutParams.WRAP_CONTENT, params.width)
        assertEquals(WindowManager.LayoutParams.WRAP_CONTENT, params.height)
        assertEquals(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, params.type)
    }

    // --- buildDeviceView のテスト（updateContent経由） ---

    @Test
    fun `TWSデバイスでデバイス名とバッテリーテキストが正しく表示される`() {
        val twsDevice =
            baseDevice.copy(
                modelName = "AirPods Pro 2",
                leftBattery = 8,
                rightBattery = 6,
                caseBattery = 3,
                images =
                    DeviceImages.Tws(
                        left = android.R.drawable.ic_menu_info_details,
                        right = android.R.drawable.ic_menu_info_details,
                        case = android.R.drawable.ic_menu_info_details,
                    ),
            )
        delegate.addOverlayView()
        delegate.updateContent(listOf(twsDevice))

        val card = findFirstCard()
        assertEquals("AirPods Pro 2", card.findViewById<TextView>(R.id.device_model_name).text)
        assertEquals("85%", card.findViewById<TextView>(R.id.text_left_battery).text)
        assertEquals("65%", card.findViewById<TextView>(R.id.text_right_battery).text)
        assertEquals("35%", card.findViewById<TextView>(R.id.text_case_battery).text)
    }

    @Test
    fun `Singleデバイスでデバイス名とバッテリーテキストが正しく表示される`() {
        val singleDevice =
            baseDevice.copy(
                modelName = "AirPods Max",
                isSingle = true,
                leftBattery = 9,
                images =
                    DeviceImages.Single(
                        body = android.R.drawable.ic_menu_info_details,
                    ),
            )
        delegate.addOverlayView()
        delegate.updateContent(listOf(singleDevice))

        val card = findFirstCard()
        assertEquals("AirPods Max", card.findViewById<TextView>(R.id.device_model_name).text)
        assertEquals("95%", card.findViewById<TextView>(R.id.text_body_battery).text)
    }

    @Test
    fun `画像なしTWSデバイスでバッテリーサマリーが正しく表示される`() {
        val textOnlyDevice =
            baseDevice.copy(
                modelName = "Unknown TWS",
                leftBattery = 5,
                rightBattery = 7,
                caseBattery = null,
            )
        delegate.addOverlayView()
        delegate.updateContent(listOf(textOnlyDevice))

        val card = findFirstCard()
        assertEquals("Unknown TWS", card.findViewById<TextView>(R.id.device_model_name).text)
        assertEquals("L:55% R:75% Case:--", card.findViewById<TextView>(R.id.text_battery_summary).text)
    }

    @Test
    fun `画像なしSingleデバイスでバッテリーテキストのみ表示される`() {
        val textOnlySingle =
            baseDevice.copy(
                modelName = "Unknown Single",
                isSingle = true,
                leftBattery = 10,
            )
        delegate.addOverlayView()
        delegate.updateContent(listOf(textOnlySingle))

        val card = findFirstCard()
        assertEquals("Unknown Single", card.findViewById<TextView>(R.id.device_model_name).text)
        assertEquals("100%", card.findViewById<TextView>(R.id.text_battery_summary).text)
    }

    @Test
    fun `複数デバイスで全カードが生成される`() {
        val device1 = baseDevice.copy(modelName = "Device A")
        val device2 = baseDevice.copy(modelName = "Device B", modelCode = 1)
        delegate.addOverlayView()
        delegate.updateContent(listOf(device1, device2))

        val container = findCardsContainer()
        assertEquals(2, container.childCount)
        assertEquals("Device A", container.getChildAt(0).findViewById<TextView>(R.id.device_model_name).text)
        assertEquals("Device B", container.getChildAt(1).findViewById<TextView>(R.id.device_model_name).text)
    }

    @Test
    fun `updateContentを再度呼ぶと既存カードが置き換わる`() {
        delegate.addOverlayView()
        delegate.updateContent(listOf(baseDevice))

        val updatedDevice = baseDevice.copy(modelName = "Updated Name")
        delegate.updateContent(listOf(updatedDevice))

        val container = findCardsContainer()
        assertEquals(1, container.childCount)
        assertEquals("Updated Name", container.getChildAt(0).findViewById<TextView>(R.id.device_model_name).text)
    }

    /** rootView → ScrollView → LinearLayout(cardsContainer) を辿る */
    private fun findCardsContainer(): ViewGroup {
        val root = fakeWindowOps.lastAddedView as ViewGroup
        // root(FrameLayout) → 最初の子がScrollView → その子がcardsContainer
        val scrollView = root.getChildAt(0) as ViewGroup
        return scrollView.getChildAt(0) as ViewGroup
    }

    private fun findFirstCard(): View {
        val container = findCardsContainer()
        assertTrue("カードが存在しない", container.childCount > 0)
        return container.getChildAt(0)
    }
}

private class FakeOverlayWindowOperations : OverlayWindowOperations {
    var canDrawOverlaysResult = true
    var addViewCount = 0
    var removeViewCount = 0
    var updateLayoutCount = 0
    var lastAddedView: View? = null
    var lastAddedParams: WindowManager.LayoutParams? = null

    override fun canDrawOverlays(): Boolean = canDrawOverlaysResult

    override fun addView(
        view: View,
        params: WindowManager.LayoutParams,
    ) {
        addViewCount++
        lastAddedView = view
        lastAddedParams = params
    }

    override fun removeViewImmediate(view: View) {
        removeViewCount++
    }

    var lastUpdatedParams: WindowManager.LayoutParams? = null

    override fun updateViewLayout(
        view: View,
        params: WindowManager.LayoutParams,
    ) {
        updateLayoutCount++
        lastUpdatedParams = params
    }
}
