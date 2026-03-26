package kurou.androidpods.core.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kurou.androidpods.core.domain.BluetoothAdapterRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindBluetoothAdapterRepository(
        impl: BluetoothAdapterRepositoryImpl,
    ): BluetoothAdapterRepository
}
