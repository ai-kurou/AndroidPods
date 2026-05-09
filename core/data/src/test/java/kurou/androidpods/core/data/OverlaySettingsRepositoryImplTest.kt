package kurou.androidpods.core.data

import android.content.Context
import android.provider.Settings
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OverlaySettingsRepositoryImplTest {
    private val context = mockk<Context>()

    @Before
    fun setUp() {
        mockkStatic(Settings::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(Settings::class)
    }

    @Test
    fun `オーバーレイ権限が未付与の場合はisEnabledがfalseを返す`() {
        every { Settings.canDrawOverlays(context) } returns false
        val repository = OverlaySettingsRepositoryImpl(context)

        assertFalse(repository.isEnabled())
    }

    @Test
    fun `オーバーレイ権限が付与された場合はisEnabledがtrueを返す`() {
        every { Settings.canDrawOverlays(context) } returns true
        val repository = OverlaySettingsRepositoryImpl(context)

        assertTrue(repository.isEnabled())
    }

    @Test
    fun `observeは初期値としてcanDrawOverlaysの結果を返す`() =
        runTest {
            every { Settings.canDrawOverlays(context) } returns false
            val repository = OverlaySettingsRepositoryImpl(context)

            assertFalse(repository.observe().first())
        }

    @Test
    fun `refreshを呼ぶとobserveの値がcanDrawOverlaysの最新値に更新される`() =
        runTest {
            every { Settings.canDrawOverlays(context) } returns false
            val repository = OverlaySettingsRepositoryImpl(context)
            assertFalse(repository.observe().first())

            every { Settings.canDrawOverlays(context) } returns true
            repository.refresh()

            assertTrue(repository.observe().first())
        }
}
