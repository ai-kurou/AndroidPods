package kurou.androidpods.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kurou.androidpods.core.domain.ThemeMode
import kurou.androidpods.core.domain.ThemeSettings
import kurou.androidpods.core.domain.ThemeSettingsRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

internal val Context.themeDataStore by preferencesDataStore(name = "theme_settings")

@Singleton
internal class ThemeSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ThemeSettingsRepository {
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val dynamicColorKey = booleanPreferencesKey("dynamic_color")

    override fun observe(): Flow<ThemeSettings> =
        dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { preferences ->
                val mode =
                    preferences[themeModeKey]
                        ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                        ?: ThemeMode.SYSTEM
                val dynamicColor = preferences[dynamicColorKey] ?: true
                ThemeSettings(themeMode = mode, useDynamicColor = dynamicColor)
            }

    override suspend fun update(settings: ThemeSettings) {
        dataStore.edit { preferences ->
            preferences[themeModeKey] = settings.themeMode.name
            preferences[dynamicColorKey] = settings.useDynamicColor
        }
    }
}
