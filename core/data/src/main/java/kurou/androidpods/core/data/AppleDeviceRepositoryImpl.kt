package kurou.androidpods.core.data

import android.annotation.SuppressLint
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

// --- BLE Manufacturer Specific Data の定数 ---
// Apple の Bluetooth SIG 登録企業ID
private const val APPLE_COMPANY_ID = 0x004C
// AirPods等が発信する「Proximity Pairing」プロトコルの識別子とデータ長
private const val PROXIMITY_PAIRING_TYPE: Byte = 0x07
private const val PROXIMITY_PAIRING_LENGTH: Byte = 0x19
private const val AIRPODS_DATA_LENGTH = 27

// --- デバイス管理の定数 ---
// この時間(ms)ビーコンを受信しなかったデバイスは「範囲外」として一覧から除去する
private const val DEVICE_TIMEOUT_MS = 5_000L
// 範囲外デバイスの除去処理を実行する間隔(ms)
private const val CLEANUP_INTERVAL_MS = 1_000L

/**
 * BLE スキャンで周囲の Apple デバイス（AirPods 等）を検出し、バッテリー残量などの情報を提供する。
 *
 * 動作の流れ:
 * 1. [startScan] で BLE スキャンを開始し、Apple の Proximity Pairing ビーコンだけをフィルタして受信
 * 2. 受信したビーコンのバイト列をパースして [AppleDevice] に変換し、[_devices] に蓄積
 * 3. 同一モデルのデバイスが複数ビーコンを飛ばす場合、電波強度(RSSI)が最も強いものを採用
 * 4. 一定時間ビーコンを受信しなかったデバイスは自動的に一覧から除去（[removeStaleDevices]）
 * 5. [stopScan] でスキャンを停止し、蓄積データをクリア
 */
@Singleton
class AppleDeviceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppleDeviceRepository {

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    private var scanner: BluetoothLeScanner? = null
    /** 検出中のデバイス一覧。キーはモデルコードの文字列表現 */
    private val _devices = MutableStateFlow<Map<String, AppleDevice>>(emptyMap())
    /** 各デバイスのビーコンを最後に受信した時刻（SystemClock.elapsedRealtime ベース） */
    private val lastSeenAt = ConcurrentHashMap<String, Long>()
    /** 範囲外デバイスの定期クリーンアップ用スコープ */
    private val scope = CoroutineScope(Dispatchers.Default)
    private var cleanupJob: Job? = null

    /**
     * BLE スキャン結果のコールバック。
     * reportDelay > 0 に設定しているため通常は [onBatchScanResults] が呼ばれるが、
     * 端末によっては [onScanResult] が呼ばれるケースもあるため両方をハンドルする。
     */
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            handleResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            for (result in results) {
                handleResult(result)
            }
        }
    }

    /**
     * スキャン結果1件を処理する。
     * ビーコンのバイト列をパースし、Apple デバイスとして認識できた場合にデバイス一覧を更新する。
     * 同一モデルコードのデバイスが既に存在する場合は、RSSI（電波強度）が強い方を保持する。
     */
    private fun handleResult(result: ScanResult) {
        parseProximityPairing(result)?.let { device ->
            val key = device.modelCode.toString()
            val now = SystemClock.elapsedRealtime()
            val existing = _devices.value[key]
            if (existing == null || device.rssi >= existing.rssi) {
                lastSeenAt[key] = now
                _devices.value += (key to device)
            } else {
                // RSSIが弱くても「まだ範囲内にいる」ことを記録して除去を防ぐ
                lastSeenAt[key] = now
            }
        }
    }

    override fun observeDevices(): Flow<Map<String, AppleDevice>> = _devices.asStateFlow()

    /**
     * BLE スキャンを開始して周囲の Apple デバイスの検出を始める。
     *
     * スキャンフィルタにより Apple の Proximity Pairing ビーコンのみを受信対象にしている。
     * 同時に、範囲外デバイスの定期クリーンアップを開始する。
     */
    @SuppressLint("MissingPermission")
    override fun startScan() {
        // 既にスキャン中の場合は先に停止する（コールバックの二重登録を防止）
        try {
            scanner?.stopScan(scanCallback)
        } catch (_: SecurityException) {
        }

        // Bluetooth OFF 時は bluetoothLeScanner が null になるため、毎回取得する
        val btScanner = bluetoothManager?.adapter?.bluetoothLeScanner ?: return
        scanner = btScanner

        // --- スキャンフィルタの構築 ---
        // 27バイトの Manufacturer Specific Data のうち先頭2バイト（type と length）だけを
        // マスクで比較し、Proximity Pairing プロトコルのビーコンのみを通す
        val manufacturerData = ByteArray(AIRPODS_DATA_LENGTH).apply {
            this[0] = PROXIMITY_PAIRING_TYPE  // 0x07
            this[1] = PROXIMITY_PAIRING_LENGTH // 0x19
        }
        val manufacturerDataMask = ByteArray(AIRPODS_DATA_LENGTH).apply {
            this[0] = 0xFF.toByte() // 先頭2バイトのみ完全一致で比較
            this[1] = 0xFF.toByte()
        }
        val filter = ScanFilter.Builder()
            .setManufacturerData(APPLE_COMPANY_ID, manufacturerData, manufacturerDataMask)
            .build()

        // reportDelay=1ms に設定。0 だと一部端末でビーコンを取りこぼす問題がある
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(1)
            .build()

        try {
            btScanner.startScan(listOf(filter), settings, scanCallback)
        } catch (_: SecurityException) {
        }

        // 一定間隔で、ビーコンが途絶えたデバイスを一覧から除去するジョブを起動
        cleanupJob?.cancel()
        cleanupJob = scope.launch {
            while (isActive) {
                delay(CLEANUP_INTERVAL_MS)
                removeStaleDevices()
            }
        }
    }

    /** BLE スキャンを停止し、検出済みデバイスの一覧をクリアする。 */
    @SuppressLint("MissingPermission")
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

    /**
     * 最終受信時刻から [DEVICE_TIMEOUT_MS] 以上経過したデバイスを一覧から除去する。
     * ユーザーが離れた・電源を切った等でビーコンが届かなくなったデバイスを反映するための処理。
     */
    private fun removeStaleDevices() {
        val now = SystemClock.elapsedRealtime()
        val staleAddresses = lastSeenAt.filter { now - it.value > DEVICE_TIMEOUT_MS }.keys
        if (staleAddresses.isNotEmpty()) {
            staleAddresses.forEach { lastSeenAt.remove(it) }
            _devices.value -= staleAddresses
        }
    }

    /** ScanResult から Apple の Manufacturer Specific Data を取り出してパースする。 */
    private fun parseProximityPairing(result: ScanResult): AppleDevice? {
        val data = result.scanRecord?.getManufacturerSpecificData(APPLE_COMPANY_ID) ?: return null
        return parseProximityPairingData(data, result.device.address, result.rssi)
    }
}

/**
 * Apple Proximity Pairing プロトコルのバイト列をパースして [AppleDevice] を生成する。
 *
 * バイト列のレイアウト（27バイト）:
 * ```
 * [0]    : type (0x07 = Proximity Pairing)
 * [1]    : length (0x19 = 25)
 * [2]    : (未使用)
 * [3..4] : モデルコード（上位バイト先行）— デバイスの機種を識別する
 * [5]    : ステータスバイト — 上位ニブルの bit1 で左右のバッテリー値が入れ替わる
 * [6]    : バッテリーバイト — 上位ニブル=片耳、下位ニブル=もう片耳（0x0F は不明を意味する）
 * [7]    : 上位ニブル=充電ステータス(bit0=左, bit1=右, bit2=ケース)、下位ニブル=ケースのバッテリー
 * [8..26]: (その他のデータ)
 * ```
 *
 * @return パース成功時は [AppleDevice]、データが Proximity Pairing 形式でなければ null
 */
internal fun parseProximityPairingData(data: ByteArray, address: String, rssi: Int): AppleDevice? {
    if (data.size != AIRPODS_DATA_LENGTH || data[0] != PROXIMITY_PAIRING_TYPE) return null

    // data[3..4] からモデルコード（16bit）を組み立てる
    val modelCode = ((data[3].toInt() and 0xFF) shl 8) or (data[4].toInt() and 0xFF)
    // AirPods Max のようにバッテリーが1つだけのモデルかどうか
    val isSingle = modelCode in SINGLE_BATTERY_MODELS

    val batteryByte = data[6].toInt() and 0xFF
    // data[7] の上位ニブル: 各ビットが充電中かどうかを示す
    val chargingBits = (data[7].toInt() shr 4) and 0x0F
    val leftBattery: Int?
    val rightBattery: Int?
    val caseBattery: Int?
    val leftCharging: Boolean
    val rightCharging: Boolean
    val caseCharging: Boolean

    if (isSingle) {
        // シングルデバイス: 下位ニブル（0〜14）のみがバッテリー値。0x0F は「不明」
        val singleBattery = (batteryByte and 0x0F).takeUnless { it == 0x0F }
        leftBattery = singleBattery
        rightBattery = null
        caseBattery = null
        leftCharging = (chargingBits and 0x01) != 0
        rightCharging = false
        caseCharging = false
    } else {
        // TWS（左右独立）デバイスの場合:
        // data[5] の上位ニブルの bit1 が 0 のとき、上位/下位ニブルの左右が入れ替わる
        val isFlipped = ((data[5].toInt() shr 4) and 0x02) == 0
        val upperNibble = (batteryByte shr 4) and 0x0F
        val lowerNibble = batteryByte and 0x0F
        // 0x0F は「バッテリー情報なし」を意味するため null に変換
        leftBattery = (if (isFlipped) upperNibble else lowerNibble).takeUnless { it == 0x0F }
        rightBattery = (if (isFlipped) lowerNibble else upperNibble).takeUnless { it == 0x0F }
        caseBattery = (data[7].toInt() and 0x0F).takeUnless { it == 0x0F }
        // 充電ビットもバッテリーと同様に左右の入れ替えが適用される
        val upperCharging = (chargingBits and 0x01) != 0
        val lowerCharging = (chargingBits and 0x02) != 0
        leftCharging = if (isFlipped) upperCharging else lowerCharging
        rightCharging = if (isFlipped) lowerCharging else upperCharging
        caseCharging = (chargingBits and 0x04) != 0
    }

    return AppleDevice(
        address = address,
        modelName = appleModelName(modelCode),
        modelCode = modelCode,
        rssi = rssi,
        leftBattery = leftBattery,
        rightBattery = rightBattery,
        caseBattery = caseBattery,
        isSingle = isSingle,
        leftCharging = leftCharging,
        rightCharging = rightCharging,
        caseCharging = caseCharging,
    )
}
