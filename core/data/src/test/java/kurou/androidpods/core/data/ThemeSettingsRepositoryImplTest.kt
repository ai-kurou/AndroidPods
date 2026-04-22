package kurou.androidpods.core.data

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kurou.androidpods.core.domain.ThemeMode
import kurou.androidpods.core.domain.ThemeSettings
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ThemeSettingsRepositoryImplTest {

    private val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    private val repository = ThemeSettingsRepositoryImpl(context)

    @Before
    fun setUp() = runBlocking {
        repository.update(ThemeSettings(ThemeMode.SYSTEM, useDynamicColor = true))
    }

    @Test
    fun `デフォルト値はSYSTEMテーマとDynamicColorON`() = runTest {
        val settings = repository.observe().first()

        assertEquals(ThemeSettings(ThemeMode.SYSTEM, useDynamicColor = true), settings)
    }

    @Test
    fun `updateで保存した値が取得できる`() = runTest {
        val expected = ThemeSettings(ThemeMode.DARK, useDynamicColor = false)

        repository.update(expected)

        val result = repository.observe().first()
        assertEquals(expected, result)
    }
}
