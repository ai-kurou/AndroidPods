package kurou.androidpods.core.data

import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.AppleDeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val APPLE_COMPANY_ID = 0x004C
private const val PROXIMITY_PAIRING_TYPE: Byte = 0x07

@Singleton
class AppleDeviceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppleDeviceRepository {

    private val scanner =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)
            ?.adapter?.bluetoothLeScanner

    private val _devices = MutableStateFlow<Map<String, AppleDevice>>(emptyMap())

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            parseProximityPairing(result)?.let { device ->
                _devices.value += (device.address to device)
            }
        }
    }

    override fun observeDevices(): Flow<Map<String, AppleDevice>> = _devices.asStateFlow()

    override fun startScan() {
        val filter = ScanFilter.Builder()
            .setManufacturerData(
                APPLE_COMPANY_ID,
                byteArrayOf(PROXIMITY_PAIRING_TYPE),
                byteArrayOf(0xFF.toByte()),
            )
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setReportDelay(0)
            .build()

        try {
            scanner?.startScan(listOf(filter), settings, scanCallback)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    override fun stopScan() {
        try {
            scanner?.stopScan(scanCallback)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    private fun parseProximityPairing(result: ScanResult): AppleDevice? {
        val data = result.scanRecord?.getManufacturerSpecificData(APPLE_COMPANY_ID) ?: return null
        if (data.size < 10 || data[0] != PROXIMITY_PAIRING_TYPE) return null

        val modelCode = ((data[3].toInt() and 0xFF) shl 8) or (data[4].toInt() and 0xFF)

        val batteryByte = if (data.size > 6) data[6].toInt() and 0xFF else null
        val rightBattery = batteryByte?.let { (it shr 4) and 0x0F }?.takeIf { it != 0x0F }
        val leftBattery = batteryByte?.let { it and 0x0F }?.takeIf { it != 0x0F }

        val caseBatteryByte = if (data.size > 7) data[7].toInt() and 0xFF else null
        val caseBattery = caseBatteryByte?.let { (it shr 4) and 0x0F }?.takeIf { it != 0x0F }

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
