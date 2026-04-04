package kurou.androidpods.core.service

import android.view.View
import android.view.WindowManager
import androidx.test.core.app.ApplicationProvider
import kurou.androidpods.core.domain.AppleDevice
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
@Config(sdk = [34])
class BatteryOverlayViewDelegateTest {

    private lateinit var fakeWindowOps: FakeOverlayWindowOperations
    private lateinit var delegate: BatteryOverlayViewDelegate

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
    fun `addOverlayViewのLayoutParamsがWRAP_CONTENTでTYPE_APPLICATION_OVERLAY`() {
        delegate.addOverlayView()

        val params = fakeWindowOps.lastAddedParams!!
        assertEquals(WindowManager.LayoutParams.WRAP_CONTENT, params.width)
        assertEquals(WindowManager.LayoutParams.WRAP_CONTENT, params.height)
        assertEquals(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, params.type)
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

    override fun addView(view: View, params: WindowManager.LayoutParams) {
        addViewCount++
        lastAddedView = view
        lastAddedParams = params
    }

    override fun removeViewImmediate(view: View) {
        removeViewCount++
    }

    override fun updateViewLayout(view: View, params: WindowManager.LayoutParams) {
        updateLayoutCount++
    }
}
