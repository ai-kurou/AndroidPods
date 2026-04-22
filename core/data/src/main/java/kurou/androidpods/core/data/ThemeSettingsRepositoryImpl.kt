package kurou.androidpods.core.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kurou.androidpods.core.domain.ThemeMode
import kurou.androidpods.core.domain.ThemeSettings
import kurou.androidpods.core.domain.ThemeSettingsRepository
import androidx.datastore.preferences.core.emptyPreferences
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore(name = "theme_settings")

@Singleton
internal class ThemeSettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ThemeSettingsRepository {

    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val dynamicColorKey = booleanPreferencesKey("dynamic_color")

    override fun observe(): Flow<ThemeSettings> =
        context.themeDataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { preferences ->
                val mode = preferences[themeModeKey]
                    ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                    ?: ThemeMode.SYSTEM
                val dynamicColor = preferences[dynamicColorKey] ?: true
                ThemeSettings(themeMode = mode, useDynamicColor = dynamicColor)
            }

    override suspend fun update(settings: ThemeSettings) {
        context.themeDataStore.edit { preferences ->
            preferences[themeModeKey] = settings.themeMode.name
            preferences[dynamicColorKey] = settings.useDynamicColor
        }
    }
}
