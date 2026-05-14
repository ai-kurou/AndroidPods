package kurou.androidpods.core.data

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppleDeviceModelsTest {

    private lateinit var mockCrashlytics: FirebaseCrashlytics

    @Before
    fun setUp() {
        mockCrashlytics = mockk()
        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns mockCrashlytics
    }

    @After
    fun tearDown() {
        unmockkStatic(FirebaseCrashlytics::class)
    }

    @Test
    fun `appleModelName - 既知のモデルコードは正しい名前を返す`() {
        assertEquals("AirPods Pro (2nd Gen)", appleModelName(0x1420))
    }

    @Test
    fun `appleModelName - 未知のモデルコードはUnknown文字列を返しCrashlyticsに記録する`() {
        val slot = slot<Throwable>()
        justRun { mockCrashlytics.recordException(capture(slot)) }

        val result = appleModelName(0x9999)

        assertTrue(result.startsWith("Unknown Apple Device (0x9999)"))
        assertTrue(slot.captured is UnknownAppleDeviceException)
        assertEquals("Unknown Apple device model code: 0x9999", slot.captured.message)
    }
}
