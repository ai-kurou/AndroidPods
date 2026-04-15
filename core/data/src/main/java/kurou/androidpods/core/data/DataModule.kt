package kurou.androidpods.core.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kurou.androidpods.core.domain.BluetoothAdapterRepository
import kurou.androidpods.core.domain.CompatibleDeviceRepository
import kurou.androidpods.core.domain.FirstLaunchRepository
import kurou.androidpods.core.domain.AppleDeviceRepository
import kurou.androidpods.core.domain.OverlaySettingsRepository
import kurou.androidpods.core.domain.UpdateRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    internal abstract fun bindBluetoothAdapterRepository(
        impl: BluetoothAdapterRepositoryImpl,
    ): BluetoothAdapterRepository

    @Binds
    internal abstract fun bindFirstLaunchRepository(
        impl: FirstLaunchRepositoryImpl,
    ): FirstLaunchRepository

    @Binds
    internal abstract fun bindAppleDeviceRepository(
        impl: AppleDeviceRepositoryImpl,
    ): AppleDeviceRepository

    @Binds
    internal abstract fun bindOverlaySettingsRepository(
        impl: OverlaySettingsRepositoryImpl,
    ): OverlaySettingsRepository

    @Binds
    internal abstract fun bindCompatibleDeviceRepository(
        impl: CompatibleDeviceRepositoryImpl,
    ): CompatibleDeviceRepository

    @Binds
    internal abstract fun bindUpdateRepository(
        impl: UpdateRepositoryImpl,
    ): UpdateRepository
}
