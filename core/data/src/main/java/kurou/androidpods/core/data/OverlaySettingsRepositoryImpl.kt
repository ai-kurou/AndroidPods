package kurou.androidpods.core.data

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kurou.androidpods.core.domain.OverlaySettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class OverlaySettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : OverlaySettingsRepository {
    override fun isEnabled(): Boolean = Settings.canDrawOverlays(context)
}
