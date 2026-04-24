package kurou.androidpods.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kurou.androidpods.core.domain.AppleDeviceRepository
import kurou.androidpods.core.domain.BluetoothAdapterRepository
import kurou.androidpods.core.domain.CompatibleDeviceRepository
import kurou.androidpods.core.domain.FirstLaunchRepository
import kurou.androidpods.core.domain.OverlayPositionRepository
import kurou.androidpods.core.domain.OverlaySettingsRepository
import kurou.androidpods.core.domain.ThemeSettingsRepository
import kurou.androidpods.core.domain.UpdateRepository
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    companion object {
        @Provides
        @Singleton
        fun provideThemeDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> = context.themeDataStore

        @Provides
        @Singleton
        @Named("overlay_position")
        fun provideOverlayPositionDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> = context.overlayPositionDataStore
    }

    @Binds
    internal abstract fun bindBluetoothAdapterRepository(
        impl: BluetoothAdapterRepositoryImpl,
    ): BluetoothAdapterRepository

    @Binds
    internal abstract fun bindFirstLaunchRepository(impl: FirstLaunchRepositoryImpl): FirstLaunchRepository

    @Binds
    internal abstract fun bindAppleDeviceRepository(impl: AppleDeviceRepositoryImpl): AppleDeviceRepository

    @Binds
    internal abstract fun bindOverlayPositionRepository(impl: OverlayPositionRepositoryImpl): OverlayPositionRepository

    @Binds
    internal abstract fun bindOverlaySettingsRepository(impl: OverlaySettingsRepositoryImpl): OverlaySettingsRepository

    @Binds
    internal abstract fun bindCompatibleDeviceRepository(
        impl: CompatibleDeviceRepositoryImpl,
    ): CompatibleDeviceRepository

    @Binds
    internal abstract fun bindUpdateRepository(impl: UpdateRepositoryImpl): UpdateRepository

    @Binds
    internal abstract fun bindThemeSettingsRepository(impl: ThemeSettingsRepositoryImpl): ThemeSettingsRepository
}
