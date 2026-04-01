package kurou.androidpods.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.AppleDeviceRepository
import kurou.androidpods.core.domain.BluetoothAdapterRepository
import kurou.androidpods.core.domain.DeviceImages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DeviceScanService : Service() {

    @Inject
    lateinit var appleDeviceRepository: AppleDeviceRepository

    @Inject
    lateinit var bluetoothAdapterRepository: BluetoothAdapterRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isForeground = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isForeground) {
            isForeground = true
            val notification = buildNotification(getString(R.string.notification_scanning), emptyList())
            val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            else
                0
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, foregroundServiceType)
            observeDevices()
            observeBluetoothState()
        }
        appleDeviceRepository.startScan()
        return START_STICKY
    }

    override fun onDestroy() {
        appleDeviceRepository.stopScan()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun observeDevices() {
        scope.launch {
            appleDeviceRepository.observeDevices().collect { devices ->
                val deviceList = devices.values.toList()
                val text = formatDevicesSummary(deviceList)
                val notification = buildNotification(text, deviceList)
                getSystemService(NotificationManager::class.java)
                    .notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun observeBluetoothState() {
        scope.launch {
            bluetoothAdapterRepository.observeAdapterState().collect { state ->
                if (state == BluetoothAdapter.STATE_ON) {
                    appleDeviceRepository.startScan()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(contentText: String, devices: List<AppleDevice>): Notification {
        val builder = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(contentText)
            .setOngoing(true)

        val contentIntent = createLaunchPendingIntent(this)
        if (contentIntent != null) {
            builder.setContentIntent(contentIntent)
        }

        if (devices.isNotEmpty()) {
            builder.setCustomContentView(buildCollapsedRemoteViews(packageName, devices))

            val expandedView = buildExpandedRemoteViews(packageName, devices)
            builder.setCustomBigContentView(expandedView)
            builder.setStyle(Notification.DecoratedCustomViewStyle())
        }

        return builder.build()
    }

    companion object {
        private const val CHANNEL_ID = "device_scan"
        private const val NOTIFICATION_ID = 1
        fun start(context: Context) {
            context.startForegroundService(Intent(context, DeviceScanService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, DeviceScanService::class.java))
        }
    }
}

internal fun createLaunchPendingIntent(context: Context): PendingIntent? {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        ?: return null
    return PendingIntent.getActivity(
        context,
        0,
        launchIntent,
        PendingIntent.FLAG_IMMUTABLE,
    )
}

internal fun buildCollapsedRemoteViews(
    packageName: String,
    devices: List<AppleDevice>,
): RemoteViews {
    val collapsedText = formatDevicesSummary(listOf(devices.first()), showCharging = true)
    val collapsedView = RemoteViews(packageName, R.layout.notification_collapsed)
    collapsedView.setTextViewText(R.id.notification_text, collapsedText)
    return collapsedView
}

internal fun buildExpandedRemoteViews(
    packageName: String,
    devices: List<AppleDevice>,
): RemoteViews {
    val expandedView = RemoteViews(packageName, R.layout.notification_expanded)
    expandedView.removeAllViews(R.id.notification_device_container)
    for (device in devices) {
        expandedView.addView(
            R.id.notification_device_container,
            buildDeviceRemoteViews(packageName, device),
        )
    }
    return expandedView
}

internal fun buildDeviceRemoteViews(packageName: String, device: AppleDevice): RemoteViews {
    return when (val images = device.images) {
        is DeviceImages.Tws -> {
            RemoteViews(packageName, R.layout.notification_device_tws).apply {
                setTextViewText(R.id.device_model_name, device.modelName)
                setImageViewResource(R.id.img_left, images.left)
                setImageViewResource(R.id.img_right, images.right)
                setImageViewResource(R.id.img_case, images.case)
                setImageViewResource(
                    R.id.img_left_battery,
                    batteryIconRes(device.leftBattery, device.leftCharging),
                )
                setTextViewText(
                    R.id.text_left_battery,
                    batteryText(device.leftBattery),
                )
                setImageViewResource(
                    R.id.img_right_battery,
                    batteryIconRes(device.rightBattery, device.rightCharging),
                )
                setTextViewText(
                    R.id.text_right_battery,
                    batteryText(device.rightBattery),
                )
                setImageViewResource(
                    R.id.img_case_battery,
                    batteryIconRes(device.caseBattery, device.caseCharging),
                )
                setTextViewText(
                    R.id.text_case_battery,
                    batteryText(device.caseBattery),
                )
            }
        }
        is DeviceImages.Single -> {
            RemoteViews(packageName, R.layout.notification_device_single).apply {
                setTextViewText(R.id.device_model_name, device.modelName)
                setImageViewResource(R.id.img_body, images.body)
                setImageViewResource(
                    R.id.img_body_battery,
                    batteryIconRes(device.leftBattery, device.leftCharging),
                )
                setTextViewText(
                    R.id.text_body_battery,
                    batteryText(device.leftBattery),
                )
            }
        }
        null -> {
            RemoteViews(packageName, R.layout.notification_device_text_only).apply {
                setTextViewText(R.id.device_model_name, device.modelName)
                val batteryStr = if (device.isSingle) {
                    batteryText(device.leftBattery)
                } else {
                    "L:${batteryText(device.leftBattery)} " +
                        "R:${batteryText(device.rightBattery)} " +
                        "Case:${batteryText(device.caseBattery)}"
                }
                setTextViewText(R.id.text_battery_summary, batteryStr)
            }
        }
    }
}

internal fun formatDevicesSummary(
    devices: List<AppleDevice>,
    showCharging: Boolean = false,
): String {
    if (devices.isEmpty()) return "No Apple devices found"
    return devices.joinToString("\n") { device ->
        val battery = if (device.isSingle) {
            batteryText(device.leftBattery, showCharging && device.leftCharging)
        } else {
            "L:${batteryText(device.leftBattery, showCharging && device.leftCharging)} " +
                "R:${batteryText(device.rightBattery, showCharging && device.rightCharging)} " +
                "Case:${batteryText(device.caseBattery, showCharging && device.caseCharging)}"
        }
        "${device.modelName} — $battery"
    }
}

internal fun batteryIconRes(level: Int?, charging: Boolean): Int {
    if (level == null) return R.drawable.icon_battery_null
    if (level >= 10) {
        return if (charging) R.drawable.icon_battery_charging_100
        else R.drawable.icon_battery_95_100
    }
    val pct = level * 10 + 5
    return if (charging) {
        when {
            pct < 20 -> R.drawable.icon_battery_charging_0_19
            pct < 40 -> R.drawable.icon_battery_charging_20_39
            pct < 60 -> R.drawable.icon_battery_charging_40_59
            pct < 80 -> R.drawable.icon_battery_charging_60_79
            pct < 95 -> R.drawable.icon_battery_charging_80_94
            else -> R.drawable.icon_battery_charging_95_99
        }
    } else {
        when {
            pct < 5 -> R.drawable.icon_battery_0_4
            pct < 20 -> R.drawable.icon_battery_5_19
            pct < 40 -> R.drawable.icon_battery_20_39
            pct < 60 -> R.drawable.icon_battery_40_59
            pct < 80 -> R.drawable.icon_battery_60_79
            pct < 95 -> R.drawable.icon_battery_80_94
            else -> R.drawable.icon_battery_95_100
        }
    }
}

internal fun batteryText(level: Int?, charging: Boolean = false): String {
    val pct = when {
        level == null -> "--"
        level >= 10 -> "100%"
        else -> "${level * 10 + 5}%"
    }
    return if (charging) "$pct⚡" else pct
}
