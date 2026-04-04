package kurou.androidpods.core.service

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.AppleDeviceRepository
import kurou.androidpods.core.domain.BluetoothAdapterRepository
import kurou.androidpods.core.domain.FirstLaunchRepository
import kurou.androidpods.core.domain.OverlaySettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Singleton

val fakeDevicesFlow = MutableSharedFlow<Map<String, AppleDevice>>(extraBufferCapacity = 1)
val fakeBluetoothStateFlow = MutableSharedFlow<Int?>(extraBufferCapacity = 1)
var startScanCalled = false
var stopScanCalled = false
var startScanCount = 0

fun resetFakeRepository() {
    startScanCalled = false
    stopScanCalled = false
    startScanCount = 0
}

@Module
@InstallIn(SingletonComponent::class)
object FakeRepositoryModule {

    @Provides
    @Singleton
    fun provideAppleDeviceRepository(): AppleDeviceRepository =
        object : AppleDeviceRepository {
            override fun observeDevices(): Flow<Map<String, AppleDevice>> = fakeDevicesFlow
            override fun startScan() { startScanCalled = true; startScanCount++ }
            override fun stopScan() { stopScanCalled = true }
        }

    @Provides
    @Singleton
    fun provideBluetoothAdapterRepository(): BluetoothAdapterRepository =
        object : BluetoothAdapterRepository {
            override fun observeAdapterState(): Flow<Int?> = fakeBluetoothStateFlow
            override fun getCurrentState(): Int? = null
        }

    @Provides
    @Singleton
    fun provideFirstLaunchRepository(): FirstLaunchRepository =
        object : FirstLaunchRepository {
            override fun observeIsFirstLaunch(): Flow<Boolean> = emptyFlow()
            override suspend fun markAsLaunched() {}
        }

    @Provides
    @Singleton
    fun provideOverlaySettingsRepository(): OverlaySettingsRepository =
        object : OverlaySettingsRepository {
            override fun isEnabled(): Boolean = false
        }
}
