package kurou.androidpods.core.service

import android.content.Context
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.DeviceImages

internal class BatteryOverlayManager(private val context: Context) {

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
        overlayView?.let {
            windowManager.removeViewImmediate(it)
            overlayView = null
        }
    }

    val isShowing: Boolean get() = overlayView != null

    private fun addOverlayView() {
        val view = LayoutInflater.from(context).inflate(R.layout.overlay_container, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }
        windowManager.addView(view, params)
        overlayView = view
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
