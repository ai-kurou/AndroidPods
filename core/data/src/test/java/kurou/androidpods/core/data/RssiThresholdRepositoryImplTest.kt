package kurou.androidpods.core.data

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kurou.androidpods.core.domain.RssiThreshold
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.IOException
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RssiThresholdRepositoryImplTest {
    private lateinit var repository: RssiThresholdRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val dataStore = PreferenceDataStoreFactory.create {
            File(context.filesDir, "datastore/test_rssi_${UUID.randomUUID()}.preferences_pb")
        }
        repository = RssiThresholdRepositoryImpl(dataStore)
    }

    @Test
    fun `デフォルト値はVERY_NEAR`() =
        runTest {
            val result = repository.observe().first()

            assertEquals(RssiThreshold.VERY_NEAR, result)
        }

    @Test
    fun `updateで保存した値が取得できる`() =
        runTest {
            repository.update(RssiThreshold.NEAR)

            val result = repository.observe().first()

            assertEquals(RssiThreshold.NEAR, result)
        }

    @Test
    fun `IOExceptionが発生した場合はデフォルト値ALLを返す`() =
        runTest {
            val ioExceptionDataStore = object : DataStore<Preferences> {
                override val data: Flow<Preferences> = flow { throw IOException("Test") }
                override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                    throw IOException("Test")
            }
            val repo = RssiThresholdRepositoryImpl(ioExceptionDataStore)

            val result = repo.observe().first()

            assertEquals(RssiThreshold.VERY_NEAR, result)
        }

    @Test
    fun `IOException以外の例外は伝播する`() =
        runTest {
            val runtimeExceptionDataStore = object : DataStore<Preferences> {
                override val data: Flow<Preferences> = flow { throw RuntimeException("Test") }
                override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                    throw RuntimeException("Test")
            }
            val repo = RssiThresholdRepositoryImpl(runtimeExceptionDataStore)

            var thrownException: Throwable? = null
            try {
                repo.observe().first()
            } catch (e: RuntimeException) {
                thrownException = e
            }
            assertNotNull(thrownException)
            assertTrue(thrownException is RuntimeException)
        }
}
