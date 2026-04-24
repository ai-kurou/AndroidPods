package kurou.androidpods.core.service

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.DeviceImages
import kurou.androidpods.core.domain.OverlayPosition

internal class BatteryOverlayViewDelegate(
    private val context: Context,
    private val windowOps: OverlayWindowOperations = DefaultOverlayWindowOperations(context),
) : OverlayViewDelegate {
    companion object {
        private const val CARD_WIDTH_DP = 280f
        private const val CARD_SPACING_DP = 12f
        private const val CONTAINER_MARGIN_BOTTOM_DP = 32f
        private const val CLOSE_BUTTON_SIZE_DP = 36f
        private const val CLOSE_BUTTON_MARGIN_DP = 8f
        private const val DRAG_HANDLE_WIDTH_DP = 36f
        private const val DRAG_HANDLE_HEIGHT_DP = 4f
        private const val DRAG_HANDLE_MARGIN_TOP_DP = 14f
        private const val ANIM_DURATION_MS = 300L
        private const val ANIM_SCALE_FROM = 0.85f
        private const val ANIM_SCALE_TO = 1.0f
        private const val DRAG_THRESHOLD = 10f
    }

    private var overlayPosition: OverlayPosition = OverlayPosition.BOTTOM
        private set

    private var overlayView: View? = null
    private var cardsContainer: LinearLayout? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var hideAnimator: AnimatorSet? = null

    override val hasView: Boolean get() = overlayView != null
    override var onUserDismiss: (() -> Unit)? = null

    override fun canDrawOverlays(): Boolean = windowOps.canDrawOverlays()

    override fun addOverlayView() {
        val dm = context.resources.displayMetrics
        val cardWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CARD_WIDTH_DP, dm).toInt()
        val closeSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CLOSE_BUTTON_SIZE_DP, dm).toInt()
        val closeMarginPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CLOSE_BUTTON_MARGIN_DP, dm).toInt()

        val container =
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
            }
        cardsContainer = container

        val scrollView =
            ScrollView(context).apply {
                addView(container, FrameLayout.LayoutParams(cardWidthPx, FrameLayout.LayoutParams.WRAP_CONTENT))
                isVerticalScrollBarEnabled = false
            }

        val handleWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DRAG_HANDLE_WIDTH_DP, dm).toInt()
        val handleHeightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DRAG_HANDLE_HEIGHT_DP, dm).toInt()
        val handleMarginTopPx =
            TypedValue
                .applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    DRAG_HANDLE_MARGIN_TOP_DP,
                    dm,
                ).toInt()

        val dragHandle =
            View(context).apply {
                setBackgroundResource(R.drawable.overlay_drag_handle)
            }

        val closeButton =
            ImageView(context).apply {
                setImageResource(R.drawable.ic_close)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setOnClickListener { hideWithAnimation() }
            }

        val root =
            DraggableFrameLayout(context, this).apply {
                addView(
                    scrollView,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                    ),
                )
                addView(
                    dragHandle,
                    FrameLayout
                        .LayoutParams(
                            handleWidthPx,
                            handleHeightPx,
                            Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                        ).apply {
                            topMargin = handleMarginTopPx
                        },
                )
                addView(
                    closeButton,
                    FrameLayout.LayoutParams(closeSizePx, closeSizePx, Gravity.TOP or Gravity.END).apply {
                        topMargin = closeMarginPx
                        marginEnd = closeMarginPx
                    },
                )
            }

        val params =
            WindowManager
                .LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT,
                ).apply {
                    gravity = overlayPosition.toGravity()
                }

        root.alpha = 0f
        root.scaleX = ANIM_SCALE_FROM
        root.scaleY = ANIM_SCALE_FROM
        windowOps.addView(root, params)
        overlayView = root
        layoutParams = params

        val scaleX = ObjectAnimator.ofFloat(root, View.SCALE_X, ANIM_SCALE_FROM, ANIM_SCALE_TO)
        val scaleY = ObjectAnimator.ofFloat(root, View.SCALE_Y, ANIM_SCALE_FROM, ANIM_SCALE_TO)
        val fadeIn = ObjectAnimator.ofFloat(root, View.ALPHA, 0f, 1f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY, fadeIn)
            duration = ANIM_DURATION_MS
            interpolator = OvershootInterpolator(1.2f)
            start()
        }
    }

    private class DraggableFrameLayout(
        context: Context,
        private val delegate: BatteryOverlayViewDelegate,
    ) : FrameLayout(context) {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        private var isDragging = false

        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    val params = delegate.layoutParams ?: return false
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = ev.rawX
                    initialTouchY = ev.rawY
                    isDragging = false
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = ev.rawX - initialTouchX
                    val dy = ev.rawY - initialTouchY
                    if (!isDragging && (dx * dx + dy * dy) > DRAG_THRESHOLD * DRAG_THRESHOLD) {
                        isDragging = true
                    }
                    if (isDragging) return true
                }
            }
            return false
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val params = delegate.layoutParams ?: return false
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    val yDelta = (event.rawY - initialTouchY).toInt()
                    params.y = if (delegate.overlayPosition == OverlayPosition.TOP) {
                        initialY + yDelta
                    } else {
                        initialY - yDelta
                    }
                    delegate.windowOps.updateViewLayout(this, params)
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    isDragging = false
                    return true
                }
            }
            return super.onTouchEvent(event)
        }
    }

    override fun updatePosition(position: OverlayPosition) {
        overlayPosition = position
        val params = layoutParams ?: return
        params.gravity = position.toGravity()
        val view = overlayView ?: return
        windowOps.updateViewLayout(view, params)
    }

    override fun removeOverlayView() {
        hideAnimator?.removeAllListeners()
        hideAnimator?.cancel()
        hideAnimator = null
        val view = overlayView ?: return
        windowOps.removeViewImmediate(view)
        overlayView = null
        cardsContainer = null
        layoutParams = null
    }

    override fun animateHide(onComplete: () -> Unit) {
        val view = overlayView ?: return

        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, ANIM_SCALE_TO, ANIM_SCALE_FROM)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, ANIM_SCALE_TO, ANIM_SCALE_FROM)
        val fadeOut = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f)
        hideAnimator =
            AnimatorSet().apply {
                playTogether(scaleX, scaleY, fadeOut)
                duration = ANIM_DURATION_MS
                interpolator = DecelerateInterpolator()
                addListener(
                    object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            hideAnimator = null
                            if (overlayView === view) {
                                onComplete()
                            }
                        }
                    },
                )
                start()
            }
    }

    override fun updateContent(devices: List<AppleDevice>) {
        // hide アニメーション中に show が呼ばれた場合、アニメーションをキャンセルしてviewを再利用
        // cancel() は onAnimationEnd を発火させるので、先にリスナーを除去する
        hideAnimator?.let {
            it.removeAllListeners()
            it.cancel()
            hideAnimator = null
            overlayView?.apply {
                alpha = 1f
                scaleX = ANIM_SCALE_TO
                scaleY = ANIM_SCALE_TO
            }
        }

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

        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, ANIM_SCALE_TO, ANIM_SCALE_FROM)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, ANIM_SCALE_TO, ANIM_SCALE_FROM)
        val fadeOut = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY, fadeOut)
            duration = ANIM_DURATION_MS
            interpolator = DecelerateInterpolator()
            addListener(
                object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        if (overlayView === view) {
                            windowOps.removeViewImmediate(view)
                            overlayView = null
                            cardsContainer = null
                            layoutParams = null
                        }
                    }
                },
            )
            start()
        }
    }

    private fun buildDeviceView(
        inflater: LayoutInflater,
        parent: LinearLayout,
        device: AppleDevice,
    ): View =
        when (val images = device.images) {
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
                    val batteryStr =
                        if (device.isSingle) {
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

private fun OverlayPosition.toGravity(): Int =
    if (this == OverlayPosition.TOP) {
        Gravity.TOP or Gravity.CENTER_HORIZONTAL
    } else {
        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
    }
