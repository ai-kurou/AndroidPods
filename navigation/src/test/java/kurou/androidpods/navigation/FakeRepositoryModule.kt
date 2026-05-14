package kurou.androidpods.navigation

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.AppleDeviceRepository
import kurou.androidpods.core.domain.BluetoothAdapterRepository
import kurou.androidpods.core.domain.CompatibleDevice
import kurou.androidpods.core.domain.CompatibleDeviceRepository
import kurou.androidpods.core.domain.OverlayPosition
import kurou.androidpods.core.domain.OverlayPositionRepository
import kurou.androidpods.core.domain.OverlaySettingsRepository
import kurou.androidpods.core.domain.ThemeSettings
import kurou.androidpods.core.domain.ThemeSettingsRepository
import kurou.androidpods.core.domain.UnknownDeviceRepository
import kurou.androidpods.core.domain.UpdateRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FakeRepositoryModule {
    @Provides
    @Singleton
    fun provideBluetoothAdapterRepository(): BluetoothAdapterRepository =
        object : BluetoothAdapterRepository {
            override fun observeAdapterState(): Flow<Int?> = emptyFlow()

            override fun getCurrentState(): Int? = null
        }

    @Provides
    @Singleton
    fun provideAppleDeviceRepository(): AppleDeviceRepository =
        object : AppleDeviceRepository {
            override fun observeDevices(): Flow<Map<String, AppleDevice>> = emptyFlow()

            override fun startScan() {}

            override fun stopScan() {}
        }

    @Provides
    @Singleton
    fun provideOverlaySettingsRepository(): OverlaySettingsRepository =
        object : OverlaySettingsRepository {
            override fun isEnabled(): Boolean = false
            override fun observe(): Flow<Boolean> = flowOf(false)
            override fun refresh() {}
        }

    @Provides
    @Singleton
    fun provideCompatibleDeviceRepository(): CompatibleDeviceRepository =
        object : CompatibleDeviceRepository {
            override fun getCompatibleDevices(): Flow<List<CompatibleDevice>> = flowOf(emptyList())
        }

    @Provides
    @Singleton
    fun provideUpdateRepository(): UpdateRepository =
        object : UpdateRepository {
            override suspend fun fetchLatestTagName(): Result<String> = Result.success("v0.0.0")
        }

    @Provides
    @Singleton
    fun provideThemeSettingsRepository(): ThemeSettingsRepository =
        object : ThemeSettingsRepository {
            override fun observe(): Flow<ThemeSettings> = flowOf(ThemeSettings())

            override suspend fun update(settings: ThemeSettings) {}
        }

    @Provides
    @Singleton
    fun provideOverlayPositionRepository(): OverlayPositionRepository =
        object : OverlayPositionRepository {
            override fun observe(): Flow<OverlayPosition> = emptyFlow()

            override suspend fun update(position: OverlayPosition) {}
        }

    @Provides
    @Singleton
    fun provideUnknownDeviceRepository(): UnknownDeviceRepository =
        object : UnknownDeviceRepository {
            override fun observeUnknownModelCodes(): Flow<Set<String>> = flowOf(emptySet())

            override suspend fun saveUnknownModelCode(modelCode: Int) {}

            override suspend fun reportWithDeviceName(modelCode: Int, deviceName: String) {}
        }
}
