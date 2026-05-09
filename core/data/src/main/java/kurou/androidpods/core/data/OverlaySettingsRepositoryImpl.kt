package kurou.androidpods.core.data

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.androidpods.core.domain.OverlaySettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class OverlaySettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : OverlaySettingsRepository {
    private val _enabled = MutableStateFlow(Settings.canDrawOverlays(context))

    override fun isEnabled(): Boolean = _enabled.value

    override fun observe(): Flow<Boolean> = _enabled

    override fun refresh() {
        _enabled.update { Settings.canDrawOverlays(context) }
    }
}
