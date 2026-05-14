package kurou.androidpods.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.androidpods.core.domain.UnknownDeviceRepository
import javax.inject.Inject
import javax.inject.Singleton

private const val DATASTORE_NAME = "unknown_device"
private const val KEY_UNKNOWN_MODEL_CODES = "unknown_model_codes"
private const val KEY_REPORTED_MODEL_CODES = "reported_model_codes"
internal const val CRASHLYTICS_KEY_MODEL_CODE = "unknown_model_code"
internal const val CRASHLYTICS_KEY_DEVICE_NAME = "user_reported_device_name"

private val Context.unknownDeviceDataStore by preferencesDataStore(name = DATASTORE_NAME)

@Singleton
internal class UnknownDeviceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : UnknownDeviceRepository {

    private val unknownModelCodesKey = stringSetPreferencesKey(KEY_UNKNOWN_MODEL_CODES)
    private val reportedModelCodesKey = stringSetPreferencesKey(KEY_REPORTED_MODEL_CODES)

    override fun observeUnknownModelCodes(): Flow<Set<String>> =
        context.unknownDeviceDataStore.data.map { preferences ->
            val unknown = preferences[unknownModelCodesKey] ?: emptySet()
            val reported = preferences[reportedModelCodesKey] ?: emptySet()
            unknown - reported
        }

    override suspend fun saveUnknownModelCode(modelCode: Int) {
        val hexCode = modelCode.toHexString()
        context.unknownDeviceDataStore.edit { preferences ->
            val current = preferences[unknownModelCodesKey] ?: emptySet()
            preferences[unknownModelCodesKey] = current + hexCode
        }
    }

    override suspend fun reportWithDeviceName(modelCode: Int, deviceName: String) {
        val hexCode = modelCode.toHexString()
        try {
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey(CRASHLYTICS_KEY_MODEL_CODE, hexCode)
                setCustomKey(CRASHLYTICS_KEY_DEVICE_NAME, deviceName)
                recordException(UnknownAppleDeviceException(hexCode))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
        }
        context.unknownDeviceDataStore.edit { preferences ->
            val current = preferences[reportedModelCodesKey] ?: emptySet()
            preferences[reportedModelCodesKey] = current + hexCode
        }
    }

    private fun Int.toHexString(): String = "0x${toString(16).padStart(4, '0')}"
}
