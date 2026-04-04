package kurou.androidpods.navigation

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.AppleDeviceRepository
import kurou.androidpods.core.domain.BluetoothAdapterRepository
import kurou.androidpods.core.domain.OverlaySettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
        }
}
