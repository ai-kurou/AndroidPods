package kurou.androidpods.core.data

import android.content.Context
import android.provider.Settings
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OverlaySettingsRepositoryImplTest {
    private val context = mockk<Context>()
    private val repository = OverlaySettingsRepositoryImpl(context)

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

        assertFalse(repository.isEnabled())
    }

    @Test
    fun `オーバーレイ権限が付与された場合はisEnabledがtrueを返す`() {
        every { Settings.canDrawOverlays(context) } returns true

        assertTrue(repository.isEnabled())
    }
}
