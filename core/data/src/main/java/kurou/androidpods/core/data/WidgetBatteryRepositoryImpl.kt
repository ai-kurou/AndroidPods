package kurou.androidpods.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.WidgetBatteryRepository
import kurou.androidpods.core.domain.WidgetBatteryState
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

internal val Context.widgetBatteryDataStore by preferencesDataStore(name = "widget_battery")

@Singleton
internal class WidgetBatteryRepositoryImpl @Inject constructor(
    @Named("widget_battery") private val dataStore: DataStore<Preferences>,
) : WidgetBatteryRepository {

    private val keyDeviceName = stringPreferencesKey("device_name")
    private val keyModelCode = intPreferencesKey("model_code")
    private val keyLeftBattery = intPreferencesKey("left_battery")
    private val keyRightBattery = intPreferencesKey("right_battery")
    private val keyCaseBattery = intPreferencesKey("case_battery")
    private val keyLeftCharging = booleanPreferencesKey("left_charging")
    private val keyRightCharging = booleanPreferencesKey("right_charging")
    private val keyCaseCharging = booleanPreferencesKey("case_charging")
    private val keyIsSingle = booleanPreferencesKey("is_single")
    private val keyRecordedAt = longPreferencesKey("recorded_at")

    override fun observe(): Flow<WidgetBatteryState?> =
        dataStore.data.map { preferences ->
            val deviceName = preferences[keyDeviceName] ?: return@map null
            WidgetBatteryState(
                deviceName = deviceName,
                modelCode = preferences[keyModelCode] ?: 0,
                leftBattery = preferences[keyLeftBattery]?.takeIf { it >= 0 },
                rightBattery = preferences[keyRightBattery]?.takeIf { it >= 0 },
                caseBattery = preferences[keyCaseBattery]?.takeIf { it >= 0 },
                leftCharging = preferences[keyLeftCharging] ?: false,
                rightCharging = preferences[keyRightCharging] ?: false,
                caseCharging = preferences[keyCaseCharging] ?: false,
                isSingle = preferences[keyIsSingle] ?: false,
                recordedAt = preferences[keyRecordedAt] ?: 0L,
            )
        }

    override suspend fun save(device: AppleDevice) {
        dataStore.edit { preferences ->
            preferences[keyDeviceName] = device.modelName
            preferences[keyModelCode] = device.modelCode
            preferences[keyLeftBattery] = device.leftBattery ?: -1
            preferences[keyRightBattery] = device.rightBattery ?: -1
            preferences[keyCaseBattery] = device.caseBattery ?: -1
            preferences[keyLeftCharging] = device.leftCharging
            preferences[keyRightCharging] = device.rightCharging
            preferences[keyCaseCharging] = device.caseCharging
            preferences[keyIsSingle] = device.isSingle
            preferences[keyRecordedAt] = System.currentTimeMillis()
        }
    }
}
