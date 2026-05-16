package kurou.androidpods.core.data

import android.content.Context
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kurou.androidpods.core.domain.OverlaySettingsRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
internal class OverlaySettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("overlay_position") private val dataStore: DataStore<Preferences>,
    @ApplicationScope private val scope: CoroutineScope,
) : OverlaySettingsRepository {
    private val enabledKey = booleanPreferencesKey("overlay_enabled")

    private val _userEnabled = MutableStateFlow(false)
    private val _hasPermission = MutableStateFlow(Settings.canDrawOverlays(context))

    init {
        scope.launch {
            dataStore.data
                .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
                .map { it[enabledKey] ?: false }
                .collect { _userEnabled.value = it }
        }
    }

    override fun isEnabled(): Boolean = _userEnabled.value && _hasPermission.value

    override fun hasPermission(): Boolean = _hasPermission.value

    override fun observe(): Flow<Boolean> = combine(_userEnabled, _hasPermission) { u, p -> u && p }

    override suspend fun setEnabled(enabled: Boolean) {
        dataStore.edit { it[enabledKey] = enabled }
    }

    override fun refresh() {
        _hasPermission.value = Settings.canDrawOverlays(context)
    }
}
