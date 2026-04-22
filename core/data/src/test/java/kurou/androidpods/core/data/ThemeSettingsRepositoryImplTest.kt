package kurou.androidpods.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kurou.androidpods.core.domain.ThemeMode
import kurou.androidpods.core.domain.ThemeSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ThemeSettingsRepositoryImplTest {

    private val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    private val repository = ThemeSettingsRepositoryImpl(context, context.themeDataStore)

    @Test
    fun `デフォルト値はSYSTEMテーマとDynamicColorON`() = runTest {
        val settings = repository.observe().first()

        assertEquals(ThemeSettings(ThemeMode.SYSTEM, useDynamicColor = true), settings)
    }

    @Test
    fun `IOExceptionが発生した場合はデフォルト値を返す`() = runTest {
        val ioExceptionDataStore = object : DataStore<Preferences> {
            override val data: Flow<Preferences> = flow { throw IOException("Test IOException") }
            override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                throw IOException("Test IOException")
        }
        val repositoryWithError = ThemeSettingsRepositoryImpl(context, ioExceptionDataStore)

        val settings = repositoryWithError.observe().first()

        assertEquals(ThemeSettings(ThemeMode.SYSTEM, useDynamicColor = true), settings)
    }

    @Test
    fun `IOException以外の例外が発生した場合は例外が伝播する`() = runTest {
        val runtimeExceptionDataStore = object : DataStore<Preferences> {
            override val data: Flow<Preferences> = flow { throw RuntimeException("Test RuntimeException") }
            override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                throw RuntimeException("Test RuntimeException")
        }
        val repositoryWithError = ThemeSettingsRepositoryImpl(context, runtimeExceptionDataStore)

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
    fun `updateで保存した値が取得できる`() = runTest {
        val expected = ThemeSettings(ThemeMode.DARK, useDynamicColor = false)

        repository.update(expected)

        val result = repository.observe().first()
        assertEquals(expected, result)
    }
}
