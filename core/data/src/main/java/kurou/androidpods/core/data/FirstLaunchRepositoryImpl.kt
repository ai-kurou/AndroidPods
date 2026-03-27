package kurou.androidpods.core.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kurou.androidpods.core.domain.FirstLaunchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "first_launch")

@Singleton
internal class FirstLaunchRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : FirstLaunchRepository {

    private val isLaunchedKey = booleanPreferencesKey("is_launched")

    override fun observeIsFirstLaunch(): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            !(preferences[isLaunchedKey] ?: false)
        }

    override suspend fun markAsLaunched() {
        context.dataStore.edit { preferences ->
            preferences[isLaunchedKey] = true
        }
    }
}
