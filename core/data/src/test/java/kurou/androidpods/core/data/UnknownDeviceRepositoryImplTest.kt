package kurou.androidpods.core.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UnknownDeviceRepositoryImplTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val repository = UnknownDeviceRepositoryImpl(context)

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
    fun `observeUnknownModelCodes - unknownとreportedが両方未保存のとき空セットを返す`() =
        runTest {
            val codes = repository.observeUnknownModelCodes().first()
            assertTrue(codes.isEmpty())
        }

    @Test
    fun `saveUnknownModelCode - 未知のモデルコードがhex文字列でDataStoreに保存される`() =
        runTest {
            repository.saveUnknownModelCode(0x1111)

            val codes = repository.observeUnknownModelCodes().first()
            assertTrue(codes.contains("0x1111"))
        }

    @Test
    fun `saveUnknownModelCode - 同じコードを複数回保存しても重複しない`() =
        runTest {
            repository.saveUnknownModelCode(0x2222)
            repository.saveUnknownModelCode(0x2222)

            val codes = repository.observeUnknownModelCodes().first()
            assertTrue(codes.count { it == "0x2222" } == 1)
        }

    @Test
    fun `reportWithDeviceName - Crashlyticsにカスタムキーと例外が記録される`() =
        runTest {
            justRun { mockCrashlytics.setCustomKey(any(), any<String>()) }
            justRun { mockCrashlytics.recordException(any()) }
            repository.saveUnknownModelCode(0x3333)

            repository.reportWithDeviceName(0x3333, "AirPods Max")

            verify { mockCrashlytics.setCustomKey(CRASHLYTICS_KEY_MODEL_CODE, "0x3333") }
            verify { mockCrashlytics.setCustomKey(CRASHLYTICS_KEY_DEVICE_NAME, "AirPods Max") }
            verify { mockCrashlytics.recordException(any<UnknownAppleDeviceException>()) }
        }

    @Test
    fun `reportWithDeviceName - 報告後はobserveから除外される`() =
        runTest {
            justRun { mockCrashlytics.setCustomKey(any(), any<String>()) }
            justRun { mockCrashlytics.recordException(any()) }
            repository.saveUnknownModelCode(0x4444)

            repository.reportWithDeviceName(0x4444, "AirPods Max")

            val codes = repository.observeUnknownModelCodes().first()
            assertFalse(codes.contains("0x4444"))
        }

    @Test
    fun `reportWithDeviceName - 報告後も別の未報告コードは引き続き返される`() =
        runTest {
            justRun { mockCrashlytics.setCustomKey(any(), any<String>()) }
            justRun { mockCrashlytics.recordException(any()) }
            repository.saveUnknownModelCode(0x5555)
            repository.saveUnknownModelCode(0x6666)

            repository.reportWithDeviceName(0x5555, "AirPods Max")

            val codes = repository.observeUnknownModelCodes().first()
            assertFalse(codes.contains("0x5555"))
            assertTrue(codes.contains("0x6666"))
        }
}
