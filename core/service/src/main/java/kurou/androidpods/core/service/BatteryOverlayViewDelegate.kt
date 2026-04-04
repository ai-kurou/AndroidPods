package kurou.androidpods.core.service

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
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
        private const val CLOSE_BUTTON_SIZE_DP = 36f
        private const val CLOSE_BUTTON_MARGIN_DP = 8f
        private const val ANIM_DURATION_MS = 300L
        private const val DRAG_THRESHOLD = 10f
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
        val closeSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CLOSE_BUTTON_SIZE_DP, dm).toInt()
        val closeMarginPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CLOSE_BUTTON_MARGIN_DP, dm).toInt()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        cardsContainer = container

        val scrollView = ScrollView(context).apply {
            addView(container, FrameLayout.LayoutParams(cardWidthPx, FrameLayout.LayoutParams.WRAP_CONTENT))
            isVerticalScrollBarEnabled = false
        }

        val closeButton = ImageView(context).apply {
            setImageResource(R.drawable.ic_close)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setOnClickListener { hideWithAnimation() }
        }

        val root = FrameLayout(context).apply {
            addView(scrollView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            ))
            addView(closeButton, FrameLayout.LayoutParams(closeSizePx, closeSizePx, Gravity.TOP or Gravity.END).apply {
                topMargin = closeMarginPx
                marginEnd = closeMarginPx
            })
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }

        setupDragListener(root, params)

        root.alpha = 0f
        windowManager.addView(root, params)
        overlayView = root

        // レイアウト確定後にアニメーション開始
        root.post {
            val slideDistance = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, CARD_WIDTH_DP + CONTAINER_MARGIN_BOTTOM_DP, dm,
            )
            root.translationY = slideDistance

            val slideIn = ObjectAnimator.ofFloat(root, View.TRANSLATION_Y, slideDistance, 0f)
            val fadeIn = ObjectAnimator.ofFloat(root, View.ALPHA, 0f, 1f)
            AnimatorSet().apply {
                playTogether(slideIn, fadeIn)
                duration = ANIM_DURATION_MS
                interpolator = DecelerateInterpolator()
                start()
            }
        }
    }

    @Suppress("ClickableViewAccessibility")
    private fun setupDragListener(view: View, params: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (!isDragging && (dx * dx + dy * dy) > DRAG_THRESHOLD * DRAG_THRESHOLD) {
                        isDragging = true
                    }
                    if (isDragging) {
                        params.x = initialX + dx.toInt()
                        // gravity が BOTTOM なので Y は上方向が正
                        params.y = initialY - (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(view, params)
                    }
                    isDragging
                }
                MotionEvent.ACTION_UP -> isDragging
                else -> false
            }
        }
    }

    override fun removeOverlayView() {
        val view = overlayView ?: return
        windowManager.removeViewImmediate(view)
        overlayView = null
        cardsContainer = null
    }

    override fun animateHide(onComplete: () -> Unit) {
        val view = overlayView ?: return

        val slideOut = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, view.height.toFloat())
        val fadeOut = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f)
        AnimatorSet().apply {
            playTogether(slideOut, fadeOut)
            duration = ANIM_DURATION_MS
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (overlayView === view) {
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
        val view = overlayView ?: return
        onUserDismiss?.invoke()

        val slideOut = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, view.height.toFloat())
        val fadeOut = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f)
        AnimatorSet().apply {
            playTogether(slideOut, fadeOut)
            duration = ANIM_DURATION_MS
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (overlayView === view) {
                        windowManager.removeViewImmediate(view)
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
