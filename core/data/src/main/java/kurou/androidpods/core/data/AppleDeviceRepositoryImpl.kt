package kurou.androidpods.core.data

import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.SystemClock
import dagger.hilt.android.qualifiers.ApplicationContext
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.AppleDeviceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private const val APPLE_COMPANY_ID = 0x004C
private const val PROXIMITY_PAIRING_TYPE: Byte = 0x07
private const val PROXIMITY_PAIRING_LENGTH: Byte = 0x19
private const val AIRPODS_DATA_LENGTH = 27
private const val DEVICE_TIMEOUT_MS = 5_000L
private const val CLEANUP_INTERVAL_MS = 1_000L

@Singleton
class AppleDeviceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppleDeviceRepository {

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    private var scanner: BluetoothLeScanner? = null
    private val _devices = MutableStateFlow<Map<String, AppleDevice>>(emptyMap())
    private val lastSeenAt = ConcurrentHashMap<String, Long>()
    private val scope = CoroutineScope(Dispatchers.Default)
    private var cleanupJob: Job? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            handleResult(result)
        }

        // reportDelay > 0 ではこちらが呼ばれる
        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            for (result in results) {
                handleResult(result)
            }
        }
    }

    private fun handleResult(result: ScanResult) {
        parseProximityPairing(result)?.let { device ->
            val key = device.modelCode.toString()
            val now = SystemClock.elapsedRealtime()
            val existing = _devices.value[key]
            // 同一モデルは最もRSSIが強いビーコンを採用（OpenPodsと同様）
            if (existing == null || device.rssi >= existing.rssi) {
                lastSeenAt[key] = now
                _devices.value += (key to device)
            } else {
                lastSeenAt[key] = now
            }
        }
    }

    override fun observeDevices(): Flow<Map<String, AppleDevice>> = _devices.asStateFlow()

    override fun startScan() {
        // 既存スキャンを停止（二重登録防止）
        try {
            scanner?.stopScan(scanCallback)
        } catch (_: SecurityException) {
        }

        // BT OFF時はnullになるためキャッシュせず毎回取得
        val btScanner = bluetoothManager?.adapter?.bluetoothLeScanner ?: return
        scanner = btScanner

        // OpenPodsと同じフィルタ: 27バイト配列の先頭2バイト(type=0x07, length=0x19)をマスク
        val manufacturerData = ByteArray(AIRPODS_DATA_LENGTH).apply {
            this[0] = PROXIMITY_PAIRING_TYPE
            this[1] = PROXIMITY_PAIRING_LENGTH
        }
        val manufacturerDataMask = ByteArray(AIRPODS_DATA_LENGTH).apply {
            this[0] = 0xFF.toByte()
            this[1] = 0xFF.toByte()
        }
        val filter = ScanFilter.Builder()
            .setManufacturerData(APPLE_COMPANY_ID, manufacturerData, manufacturerDataMask)
            .build()

        // OpenPods: "DON'T USE 0" — reportDelay=0 だと一部端末でビーコンを取りこぼす
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(1)
            .build()

        try {
            btScanner.startScan(listOf(filter), settings, scanCallback)
        } catch (_: SecurityException) {
        }

        cleanupJob?.cancel()
        cleanupJob = scope.launch {
            while (isActive) {
                delay(CLEANUP_INTERVAL_MS)
                removeStaleDevices()
            }
        }
    }

    override fun stopScan() {
        cleanupJob?.cancel()
        try {
            scanner?.stopScan(scanCallback)
        } catch (_: SecurityException) {
        }
        scanner = null
        _devices.value = emptyMap()
        lastSeenAt.clear()
    }

    private fun removeStaleDevices() {
        val now = SystemClock.elapsedRealtime()
        val staleAddresses = lastSeenAt.filter { now - it.value > DEVICE_TIMEOUT_MS }.keys
        if (staleAddresses.isNotEmpty()) {
            staleAddresses.forEach { lastSeenAt.remove(it) }
            _devices.value -= staleAddresses
        }
    }

    private fun parseProximityPairing(result: ScanResult): AppleDevice? {
        val data = result.scanRecord?.getManufacturerSpecificData(APPLE_COMPANY_ID) ?: return null
        if (data.size != AIRPODS_DATA_LENGTH || data[0] != PROXIMITY_PAIRING_TYPE) return null

        val modelCode = ((data[3].toInt() and 0xFF) shl 8) or (data[4].toInt() and 0xFF)

        // data[5]のbit 1でL/Rが入れ替わる（OpenPods: isFlipped）
        val isFlipped = (data[5].toInt() and 0x02) == 0

        val batteryByte = data[6].toInt() and 0xFF
        val upperNibble = (batteryByte shr 4) and 0x0F
        val lowerNibble = batteryByte and 0x0F
        val leftBattery = (if (isFlipped) upperNibble else lowerNibble).takeUnless { it == 0x0F }
        val rightBattery = (if (isFlipped) lowerNibble else upperNibble).takeUnless { it == 0x0F }

        // data[7]: 上位ニブル=充電ステータス、下位ニブル=ケースバッテリー
        val caseBattery = (data[7].toInt() and 0x0F).takeUnless { it == 0x0F }

        return AppleDevice(
            address = result.device.address,
            modelName = appleModelName(modelCode),
            modelCode = modelCode,
            rssi = result.rssi,
            leftBattery = leftBattery,
            rightBattery = rightBattery,
            caseBattery = caseBattery,
        )
    }
}
