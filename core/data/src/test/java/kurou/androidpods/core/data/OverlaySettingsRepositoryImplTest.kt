package kurou.androidpods.core.data

import android.app.Application
import android.provider.Settings
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OverlaySettingsRepositoryImplTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val context = ApplicationProvider.getApplicationContext<Application>()

    private fun createRepository(canDrawOverlays: Boolean): OverlaySettingsRepositoryImpl {
        val dataStore = PreferenceDataStoreFactory.create(scope = testScope) {
            File(context.filesDir, "datastore/test_overlay_settings_${UUID.randomUUID()}.preferences_pb")
        }
        every { Settings.canDrawOverlays(any()) } returns canDrawOverlays
        return OverlaySettingsRepositoryImpl(context, dataStore, testScope)
    }

    @Before
    fun setUp() {
        mockkStatic(Settings::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(Settings::class)
    }

    @Test
    fun `権限未付与の場合はisEnabledがfalseを返す`() = runTest(testDispatcher) {
        val repository = createRepository(canDrawOverlays = false)

        assertFalse(repository.isEnabled())
    }

    @Test
    fun `権限付与済みでもsetEnabled前はisEnabledがfalseを返す`() = runTest(testDispatcher) {
        val repository = createRepository(canDrawOverlays = true)

        assertFalse(repository.isEnabled())
    }

    @Test
    fun `権限付与済みでsetEnabledをtrueにするとisEnabledがtrueを返す`() = runTest(testDispatcher) {
        val repository = createRepository(canDrawOverlays = true)

        repository.setEnabled(true)

        assertTrue(repository.isEnabled())
    }

    @Test
    fun `refreshを呼ぶとhasPermissionが最新値に更新される`() = runTest(testDispatcher) {
        val repository = createRepository(canDrawOverlays = false)
        assertFalse(repository.hasPermission())

        every { Settings.canDrawOverlays(any()) } returns true
        repository.refresh()

        assertTrue(repository.hasPermission())
    }
}
