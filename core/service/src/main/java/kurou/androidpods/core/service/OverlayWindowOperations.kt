package kurou.androidpods.core.service

import android.content.Context
import android.provider.Settings
import android.view.View
import android.view.WindowManager

internal interface OverlayWindowOperations {
    fun canDrawOverlays(): Boolean

    fun addView(
        view: View,
        params: WindowManager.LayoutParams,
    )

    fun removeViewImmediate(view: View)

    fun updateViewLayout(
        view: View,
        params: WindowManager.LayoutParams,
    )
}

internal class DefaultOverlayWindowOperations(private val context: Context) : OverlayWindowOperations {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    override fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(context)

    override fun addView(
        view: View,
        params: WindowManager.LayoutParams,
    ) = windowManager.addView(view, params)

    override fun removeViewImmediate(view: View) = windowManager.removeViewImmediate(view)

    override fun updateViewLayout(
        view: View,
        params: WindowManager.LayoutParams,
    ) = windowManager.updateViewLayout(view, params)
}
