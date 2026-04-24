package kurou.androidpods.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kurou.androidpods.core.domain.OverlayPosition
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
class OverlayPositionRepositoryImplTest {
    private lateinit var repository: OverlayPositionRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val dataStore =
            PreferenceDataStoreFactory.create {
                File(context.filesDir, "datastore/test_overlay_position_${UUID.randomUUID()}.preferences_pb")
            }
        repository = OverlayPositionRepositoryImpl(dataStore)
    }

    @Test
    fun `デフォルト値はBOTTOM`() =
        runTest {
            val result = repository.observe().first()

            assertEquals(OverlayPosition.BOTTOM, result)
        }

    @Test
    fun `IOExceptionが発生した場合はデフォルト値を返す`() =
        runTest {
            val ioExceptionDataStore =
                object : DataStore<Preferences> {
                    override val data: Flow<Preferences> = flow { throw IOException("Test IOException") }

                    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                        throw IOException("Test IOException")
                }
            val repositoryWithError = OverlayPositionRepositoryImpl(ioExceptionDataStore)

            val result = repositoryWithError.observe().first()

            assertEquals(OverlayPosition.BOTTOM, result)
        }

    @Test
    fun `IOException以外の例外が発生した場合は例外が伝播する`() =
        runTest {
            val runtimeExceptionDataStore =
                object : DataStore<Preferences> {
                    override val data: Flow<Preferences> = flow { throw RuntimeException("Test RuntimeException") }

                    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                        throw RuntimeException("Test RuntimeException")
                }
            val repositoryWithError = OverlayPositionRepositoryImpl(runtimeExceptionDataStore)

            var thrownException: Throwable? = null
            try {
                repositoryWithError.observe().first()
            } catch (e: RuntimeException) {
                thrownException = e
            }
            assertNotNull(thrownException)
            assertTrue(thrownException is RuntimeException)
        }

    @Test
    fun `updateで保存した値が取得できる`() =
        runTest {
            repository.update(OverlayPosition.TOP)

            val result = repository.observe().first()
            assertEquals(OverlayPosition.TOP, result)
        }
}
