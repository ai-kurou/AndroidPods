package kurou.androidpods.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kurou.androidpods.core.domain.RssiThreshold
import kurou.androidpods.core.domain.RssiThresholdRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

internal val Context.rssiThresholdDataStore by preferencesDataStore(name = "rssi_threshold")

@Singleton
internal class RssiThresholdRepositoryImpl @Inject constructor(
    @param:Named("rssi_threshold") private val dataStore: DataStore<Preferences>,
) : RssiThresholdRepository {
    private val thresholdKey = stringPreferencesKey("rssi_threshold")

    override fun observe(): Flow<RssiThreshold> =
        dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { preferences ->
                preferences[thresholdKey]
                    ?.let { try { RssiThreshold.valueOf(it) } catch (_: IllegalArgumentException) { null } }
                    ?: RssiThreshold.VERY_NEAR
            }

    override suspend fun update(threshold: RssiThreshold) {
        dataStore.edit { it[thresholdKey] = threshold.name }
    }
}
