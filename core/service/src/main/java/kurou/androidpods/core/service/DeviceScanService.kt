package kurou.androidpods.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.AppleDeviceRepository
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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification(getString(R.string.notification_scanning))
        val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, foregroundServiceType)
        appleDeviceRepository.startScan()
        observeDevices()
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
                val text = formatDevicesSummary(devices.values.toList())
                val notification = buildNotification(text)
                getSystemService(NotificationManager::class.java)
                    .notify(NOTIFICATION_ID, notification)
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

    private fun buildNotification(contentText: String): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(contentText)
            .setStyle(Notification.BigTextStyle().bigText(contentText))
            .setOngoing(true)
            .build()
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

private fun formatDevicesSummary(devices: List<AppleDevice>): String {
    if (devices.isEmpty()) return "No Apple devices found"
    return devices.joinToString("\n") { device ->
        val battery = if (device.isSingle) {
            batteryText(device.leftBattery, device.leftCharging)
        } else {
            "L:${batteryText(device.leftBattery, device.leftCharging)} " +
                "R:${batteryText(device.rightBattery, device.rightCharging)} " +
                "Case:${batteryText(device.caseBattery, device.caseCharging)}"
        }
        "${device.modelName} — $battery"
    }
}

private fun batteryText(level: Int?, charging: Boolean): String {
    val pct = when {
        level == null -> "--"
        level >= 10 -> "100%"
        else -> "${level * 10 + 5}%"
    }
    return if (charging) "$pct⚡" else pct
}
