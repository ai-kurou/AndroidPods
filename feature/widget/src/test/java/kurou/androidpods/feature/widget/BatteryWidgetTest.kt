package kurou.androidpods.feature.widget

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import kurou.androidpods.core.domain.WidgetBatteryState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BatteryWidgetTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()

    private fun twsState(left: Int? = 7, right: Int? = 8, case: Int? = 9) = WidgetBatteryState(
        deviceName = "AirPods Pro",
        leftBattery = left,
        rightBattery = right,
        caseBattery = case,
        isSingle = false,
        recordedAt = 0L,
    )

    private fun singleState(battery: Int? = 5) = WidgetBatteryState(
        deviceName = "AirPods Max",
        leftBattery = battery,
        rightBattery = null,
        caseBattery = null,
        isSingle = true,
        recordedAt = 0L,
    )

    @Test
    fun `TWS端末はL_R_Caseのフォーマットで表示される`() {
        val result = batteryLevelText(twsState(left = 7, right = 8, case = 9))
        assertEquals("L: 75%  R: 85%  Case: 95%", result)
    }

    @Test
    fun `シングル端末は単一のバッテリーパーセントで表示される`() {
        val result = batteryLevelText(singleState(battery = 5))
        assertEquals("55%", result)
    }

    @Test
    fun `バッテリーレベル10以上は100パーセントと表示される`() {
        assertEquals("100%", batteryLevelText(singleState(battery = 10)))
        assertEquals("100%", batteryLevelText(singleState(battery = 14)))
    }

    @Test
    fun `nullバッテリーはダッシュで表示される`() {
        assertEquals("--", batteryLevelText(singleState(battery = null)))
    }

    @Test
    fun `1分未満の場合はjust_nowが表示される`() {
        val now = 100_000L
        val result = recordedAtText(context, recordedAt = now - 30_000L, now = now)
        assertEquals(context.getString(R.string.widget_recorded_just_now), result)
    }

    @Test
    fun `1時間未満の場合は分前が表示される`() {
        val now = 10_000_000L
        val result = recordedAtText(context, recordedAt = now - 5 * 60_000L, now = now)
        assertEquals(context.getString(R.string.widget_recorded_minutes_ago, 5L), result)
    }

    @Test
    fun `1時間以上の場合は時間前が表示される`() {
        val now = 10_000_000L
        val result = recordedAtText(context, recordedAt = now - 2 * 3_600_000L, now = now)
        assertEquals(context.getString(R.string.widget_recorded_hours_ago, 2L), result)
    }
}
