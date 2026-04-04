package kurou.androidpods.core.service

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.DeviceImages

internal class BatteryOverlayManager(private val context: Context) {

    companion object {
        private const val CARD_SIZE_DP = 280f
        private const val CARD_MARGIN_BOTTOM_DP = 32f
        private const val ANIM_DURATION_MS = 300L
    }

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null

    fun show(devices: List<AppleDevice>) {
        if (!Settings.canDrawOverlays(context)) return
        if (devices.isEmpty()) {
            hide()
            return
        }

        if (overlayView == null) {
            addOverlayView()
        }
        updateContent(devices)
    }

    fun hide() {
        val wrapper = overlayView ?: return
        val card = wrapper.findViewById<View>(R.id.overlay_root) ?: run {
            windowManager.removeViewImmediate(wrapper)
            overlayView = null
            return
        }

        val slideOut = ObjectAnimator.ofFloat(card, View.TRANSLATION_Y, 0f, card.height.toFloat())
        val fadeOut = ObjectAnimator.ofFloat(wrapper, View.ALPHA, 1f, 0f)
        AnimatorSet().apply {
            playTogether(slideOut, fadeOut)
            duration = ANIM_DURATION_MS
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (overlayView === wrapper) {
                        windowManager.removeViewImmediate(wrapper)
                        overlayView = null
                    }
                }
            })
            start()
        }
    }

    val isShowing: Boolean get() = overlayView != null

    private fun addOverlayView() {
        val wrapper = FrameLayout(context).apply {
            setBackgroundColor(0x80000000.toInt())
            setOnClickListener { hide() }
        }

        val card = LayoutInflater.from(context)
            .inflate(R.layout.overlay_container, wrapper, false)
        // コンテンツ部分のタップは背景に伝播させない
        card.setOnClickListener { /* consume */ }

        val dm = context.resources.displayMetrics
        val sizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CARD_SIZE_DP, dm).toInt()
        val marginPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CARD_MARGIN_BOTTOM_DP, dm).toInt()
        val lp = FrameLayout.LayoutParams(sizePx, sizePx, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL).apply {
            bottomMargin = marginPx
        }
        wrapper.addView(card, lp)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        )
        // 初期状態: 背景透明、カード画面外
        wrapper.alpha = 0f
        windowManager.addView(wrapper, params)
        overlayView = wrapper

        // レイアウト確定後にアニメーション開始
        card.post {
            val dm = context.resources.displayMetrics
            val slideDistance = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, CARD_SIZE_DP + CARD_MARGIN_BOTTOM_DP, dm,
            )
            card.translationY = slideDistance

            val slideIn = ObjectAnimator.ofFloat(card, View.TRANSLATION_Y, slideDistance, 0f)
            val fadeIn = ObjectAnimator.ofFloat(wrapper, View.ALPHA, 0f, 1f)
            AnimatorSet().apply {
                playTogether(slideIn, fadeIn)
                duration = ANIM_DURATION_MS
                interpolator = DecelerateInterpolator()
                start()
            }
        }
    }

    private fun updateContent(devices: List<AppleDevice>) {
        val container = overlayView?.findViewById<LinearLayout>(R.id.overlay_device_container)
            ?: return
        container.removeAllViews()

        val inflater = LayoutInflater.from(context)
        for (device in devices) {
            val deviceView = buildDeviceView(inflater, container, device)
            container.addView(deviceView)
        }
    }

    private fun buildDeviceView(
        inflater: LayoutInflater,
        parent: LinearLayout,
        device: AppleDevice,
    ): View {
        return when (val images = device.images) {
            is DeviceImages.Tws -> {
                inflater.inflate(R.layout.overlay_device_tws, parent, false).apply {
                    findViewById<TextView>(R.id.device_model_name).text = device.modelName
                    findViewById<ImageView>(R.id.img_left).setImageResource(images.left)
                    findViewById<ImageView>(R.id.img_right).setImageResource(images.right)
                    findViewById<ImageView>(R.id.img_case).setImageResource(images.case)
                    findViewById<ImageView>(R.id.img_left_battery)
                        .setImageResource(batteryIconRes(device.leftBattery, device.leftCharging))
                    findViewById<TextView>(R.id.text_left_battery).text =
                        batteryText(device.leftBattery)
                    findViewById<ImageView>(R.id.img_right_battery)
                        .setImageResource(batteryIconRes(device.rightBattery, device.rightCharging))
                    findViewById<TextView>(R.id.text_right_battery).text =
                        batteryText(device.rightBattery)
                    findViewById<ImageView>(R.id.img_case_battery)
                        .setImageResource(batteryIconRes(device.caseBattery, device.caseCharging))
                    findViewById<TextView>(R.id.text_case_battery).text =
                        batteryText(device.caseBattery)
                }
            }
            is DeviceImages.Single -> {
                inflater.inflate(R.layout.overlay_device_single, parent, false).apply {
                    findViewById<TextView>(R.id.device_model_name).text = device.modelName
                    findViewById<ImageView>(R.id.img_body).setImageResource(images.body)
                    findViewById<ImageView>(R.id.img_body_battery)
                        .setImageResource(batteryIconRes(device.leftBattery, device.leftCharging))
                    findViewById<TextView>(R.id.text_body_battery).text =
                        batteryText(device.leftBattery)
                }
            }
            null -> {
                inflater.inflate(R.layout.overlay_device_text_only, parent, false).apply {
                    findViewById<TextView>(R.id.device_model_name).text = device.modelName
                    val batteryStr = if (device.isSingle) {
                        batteryText(device.leftBattery)
                    } else {
                        "L:${batteryText(device.leftBattery)} " +
                            "R:${batteryText(device.rightBattery)} " +
                            "Case:${batteryText(device.caseBattery)}"
                    }
                    findViewById<TextView>(R.id.text_battery_summary).text = batteryStr
                }
            }
        }
    }
}
