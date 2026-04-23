package kurou.androidpods.core.service

import android.content.Context
import android.view.View
import android.view.WindowManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowWindowManagerImpl

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DefaultOverlayWindowOperationsTest {

    private lateinit var context: Context
    private lateinit var operations: DefaultOverlayWindowOperations

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        operations = DefaultOverlayWindowOperations(context)
    }

    @Test
    fun `canDrawOverlaysはデフォルトでfalseを返す`() {
        assertFalse(operations.canDrawOverlays())
    }

    @Test
    fun `addViewでWindowManagerにビューが追加される`() {
        val view = View(context)
        val params = WindowManager.LayoutParams()

        operations.addView(view, params)

        val shadow = shadowOf(context.getSystemService(Context.WINDOW_SERVICE) as WindowManager) as ShadowWindowManagerImpl
        assertTrue(shadow.views.contains(view))
    }

    @Test
    fun `removeViewImmediateでWindowManagerからビューが削除される`() {
        val view = View(context)
        val params = WindowManager.LayoutParams()
        operations.addView(view, params)

        operations.removeViewImmediate(view)

        val shadow = shadowOf(context.getSystemService(Context.WINDOW_SERVICE) as WindowManager) as ShadowWindowManagerImpl
        assertFalse(shadow.views.contains(view))
    }

    @Test
    fun `updateViewLayoutで例外が発生しない`() {
        val view = View(context)
        val params = WindowManager.LayoutParams()
        operations.addView(view, params)

        operations.updateViewLayout(view, params)
    }
}
