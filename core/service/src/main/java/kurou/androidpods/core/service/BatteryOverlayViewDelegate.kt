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
import android.widget.ScrollView
import android.widget.TextView
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.DeviceImages

internal class BatteryOverlayViewDelegate(private val context: Context) : OverlayViewDelegate {

    companion object {
        private const val CARD_WIDTH_DP = 280f
        private const val CARD_SPACING_DP = 12f
        private const val CONTAINER_MARGIN_BOTTOM_DP = 32f
        private const val ANIM_DURATION_MS = 300L
    }

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null
    private var cardsContainer: LinearLayout? = null

    override val hasView: Boolean get() = overlayView != null
    override var onUserDismiss: (() -> Unit)? = null

    override fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(context)

    override fun addOverlayView() {
        val dm = context.resources.displayMetrics
        val cardWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CARD_WIDTH_DP, dm).toInt()
        val marginBottomPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CONTAINER_MARGIN_BOTTOM_DP, dm).toInt()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        cardsContainer = container

        val scrollView = ScrollView(context).apply {
            addView(container, FrameLayout.LayoutParams(cardWidthPx, FrameLayout.LayoutParams.WRAP_CONTENT))
            // スクロールバーを非表示
            isVerticalScrollBarEnabled = false
        }

        val wrapper = FrameLayout(context).apply {
            setBackgroundColor(0x80000000.toInt())
            setOnClickListener { hideWithAnimation() }
        }

        val scrollLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
        ).apply {
            bottomMargin = marginBottomPx
        }
        // スクロール部分のタップは背景に伝播させない
        scrollView.setOnClickListener { /* consume */ }
        wrapper.addView(scrollView, scrollLp)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        )
        wrapper.alpha = 0f
        windowManager.addView(wrapper, params)
        overlayView = wrapper

        // レイアウト確定後にアニメーション開始
        scrollView.post {
            val displayMetrics = context.resources.displayMetrics
            val slideDistance = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, CARD_WIDTH_DP + CONTAINER_MARGIN_BOTTOM_DP, displayMetrics,
            )
            scrollView.translationY = slideDistance

            val slideIn = ObjectAnimator.ofFloat(scrollView, View.TRANSLATION_Y, slideDistance, 0f)
            val fadeIn = ObjectAnimator.ofFloat(wrapper, View.ALPHA, 0f, 1f)
            AnimatorSet().apply {
                playTogether(slideIn, fadeIn)
                duration = ANIM_DURATION_MS
                interpolator = DecelerateInterpolator()
                start()
            }
        }
    }

    override fun removeOverlayView() {
        val wrapper = overlayView ?: return
        windowManager.removeViewImmediate(wrapper)
        overlayView = null
        cardsContainer = null
    }

    override fun animateHide(onComplete: () -> Unit) {
        val wrapper = overlayView ?: return
        val scrollView = (wrapper as? FrameLayout)?.getChildAt(0) ?: run {
            windowManager.removeViewImmediate(wrapper)
            overlayView = null
            cardsContainer = null
            return
        }

        val slideOut = ObjectAnimator.ofFloat(scrollView, View.TRANSLATION_Y, 0f, scrollView.height.toFloat())
        val fadeOut = ObjectAnimator.ofFloat(wrapper, View.ALPHA, 1f, 0f)
        AnimatorSet().apply {
            playTogether(slideOut, fadeOut)
            duration = ANIM_DURATION_MS
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (overlayView === wrapper) {
                        onComplete()
                    }
                }
            })
            start()
        }
    }

    override fun updateContent(devices: List<AppleDevice>) {
        val container = cardsContainer ?: return
        container.removeAllViews()

        val dm = context.resources.displayMetrics
        val spacingPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CARD_SPACING_DP, dm).toInt()
        val inflater = LayoutInflater.from(context)

        for ((index, device) in devices.withIndex()) {
            val card = inflater.inflate(R.layout.overlay_card, container, false) as LinearLayout
            val deviceView = buildDeviceView(inflater, card, device)
            card.addView(deviceView)
            // カード間にスペースを追加
            if (index > 0) {
                (card.layoutParams as? LinearLayout.LayoutParams)?.topMargin = spacingPx
            }
            card.setOnClickListener { /* consume */ }
            container.addView(card)
        }
    }

    private fun hideWithAnimation() {
        val wrapper = overlayView ?: return
        onUserDismiss?.invoke()
        val scrollView = (wrapper as? FrameLayout)?.getChildAt(0) ?: run {
            windowManager.removeViewImmediate(wrapper)
            overlayView = null
            cardsContainer = null
            return
        }

        val slideOut = ObjectAnimator.ofFloat(scrollView, View.TRANSLATION_Y, 0f, scrollView.height.toFloat())
        val fadeOut = ObjectAnimator.ofFloat(wrapper, View.ALPHA, 1f, 0f)
        AnimatorSet().apply {
            playTogether(slideOut, fadeOut)
            duration = ANIM_DURATION_MS
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (overlayView === wrapper) {
                        windowManager.removeViewImmediate(wrapper)
                        overlayView = null
                        cardsContainer = null
                    }
                }
            })
            start()
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
