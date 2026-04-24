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
import kurou.androidpods.core.domain.OverlayPosition
import kurou.androidpods.core.domain.OverlayPositionRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

internal val Context.overlayPositionDataStore by preferencesDataStore(name = "overlay_position")

@Singleton
internal class OverlayPositionRepositoryImpl @Inject constructor(
    @Named("overlay_position") private val dataStore: DataStore<Preferences>,
) : OverlayPositionRepository {
    private val positionKey = stringPreferencesKey("overlay_position")

    override fun observe(): Flow<OverlayPosition> =
        dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { preferences ->
                preferences[positionKey]
                    ?.let { runCatching { OverlayPosition.valueOf(it) }.getOrNull() }
                    ?: OverlayPosition.BOTTOM
            }

    override suspend fun update(position: OverlayPosition) {
        dataStore.edit { preferences ->
            preferences[positionKey] = position.name
        }
    }
}
