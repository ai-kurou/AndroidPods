package kurou.androidpods.core.data

import android.app.Application
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.androidpods.core.domain.AppleDevice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
class WidgetBatteryRepositoryImplTest {

    private lateinit var repository: WidgetBatteryRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val dataStore = PreferenceDataStoreFactory.create {
            File(context.filesDir, "datastore/test_widget_battery_${UUID.randomUUID()}.preferences_pb")
        }
        repository = WidgetBatteryRepositoryImpl(dataStore)
    }

    @Test
    fun `未保存のときobserveはnullを返す`() =
        runTest {
            val result = repository.observe().first()
            assertNull(result)
        }

    @Test
    fun `saveした内容がobserveで取得できる`() =
        runTest {
            val device = AppleDevice(
                address = "AA:BB:CC:DD:EE:FF",
                modelName = "AirPods Pro",
                modelCode = 0x2002,
                rssi = -60,
                leftBattery = 7,
                rightBattery = 8,
                caseBattery = 9,
                isSingle = false,
            )

            repository.save(device)

            val result = repository.observe().first()!!
            assertEquals("AirPods Pro", result.deviceName)
            assertEquals(7, result.leftBattery)
            assertEquals(8, result.rightBattery)
            assertEquals(9, result.caseBattery)
            assertEquals(false, result.isSingle)
            assertTrue(result.recordedAt > 0L)
        }

    @Test
    fun `nullバッテリーのデバイスを保存するとobserveでnullが返る`() =
        runTest {
            val device = AppleDevice(
                address = "AA:BB:CC:DD:EE:FF",
                modelName = "AirPods Max",
                modelCode = 0x200A,
                rssi = -55,
                leftBattery = null,
                rightBattery = null,
                caseBattery = null,
                isSingle = true,
            )

            repository.save(device)

            val result = repository.observe().first()!!
            assertNull(result.leftBattery)
            assertNull(result.rightBattery)
            assertNull(result.caseBattery)
            assertEquals(true, result.isSingle)
        }

    @Test
    fun `saveを2回呼ぶと最新のデバイス情報で上書きされる`() =
        runTest {
            val first = AppleDevice(
                address = "AA:BB:CC:DD:EE:FF",
                modelName = "AirPods Pro",
                modelCode = 0x2002,
                rssi = -60,
                leftBattery = 5,
                rightBattery = 5,
                caseBattery = 5,
            )
            val second = AppleDevice(
                address = "11:22:33:44:55:66",
                modelName = "AirPods Max",
                modelCode = 0x200A,
                rssi = -55,
                leftBattery = 10,
                rightBattery = null,
                caseBattery = null,
                isSingle = true,
            )

            repository.save(first)
            repository.save(second)

            val result = repository.observe().first()!!
            assertEquals("AirPods Max", result.deviceName)
            assertEquals(10, result.leftBattery)
        }
}
