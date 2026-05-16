package kurou.androidpods.feature.widget

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import kurou.androidpods.core.domain.WidgetBatteryState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kurou.androidpods.core.designsystem.R as DesignSystemR

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BatteryWidgetTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()

    private fun twsState(left: Int? = 7, right: Int? = 8, case: Int? = 9) = WidgetBatteryState(
        deviceName = "AirPods Pro",
        modelCode = 0x2402,
        leftBattery = left,
        rightBattery = right,
        caseBattery = case,
        leftCharging = false,
        rightCharging = false,
        caseCharging = false,
        isSingle = false,
        recordedAt = 0L,
    )

    private fun singleState(battery: Int? = 5) = WidgetBatteryState(
        deviceName = "AirPods Max",
        modelCode = 0x0A20,
        leftBattery = battery,
        rightBattery = null,
        caseBattery = null,
        leftCharging = false,
        rightCharging = false,
        caseCharging = false,
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

    @Test
    fun `levelToText_nullはダッシュを返す`() {
        assertEquals("--", levelToText(null))
    }

    @Test
    fun `levelToText_10以上は100パーセントを返す`() {
        assertEquals("100%", levelToText(10))
        assertEquals("100%", levelToText(15))
    }

    @Test
    fun `levelToText_0から9はレベルを変換したパーセントを返す`() {
        assertEquals("5%", levelToText(0))
        assertEquals("55%", levelToText(5))
        assertEquals("95%", levelToText(9))
    }

    @Test
    fun `widgetBatteryIconRes_nullはnullアイコンを返す`() {
        assertEquals(DesignSystemR.drawable.icon_battery_null, widgetBatteryIconRes(null, false))
        assertEquals(DesignSystemR.drawable.icon_battery_null, widgetBatteryIconRes(null, true))
    }

    @Test
    fun `widgetBatteryIconRes_レベル10以上は充電状態に応じた100パーセントアイコンを返す`() {
        assertEquals(DesignSystemR.drawable.icon_battery_charging_100, widgetBatteryIconRes(10, true))
        assertEquals(DesignSystemR.drawable.icon_battery_95_100, widgetBatteryIconRes(10, false))
    }

    @Test
    fun `widgetBatteryIconRes_充電中は各レベルに対応するアイコンを返す`() {
        assertEquals(DesignSystemR.drawable.icon_battery_charging_0_19, widgetBatteryIconRes(0, true))
        assertEquals(DesignSystemR.drawable.icon_battery_charging_20_39, widgetBatteryIconRes(2, true))
        assertEquals(DesignSystemR.drawable.icon_battery_charging_40_59, widgetBatteryIconRes(4, true))
        assertEquals(DesignSystemR.drawable.icon_battery_charging_60_79, widgetBatteryIconRes(6, true))
        assertEquals(DesignSystemR.drawable.icon_battery_charging_80_94, widgetBatteryIconRes(8, true))
        assertEquals(DesignSystemR.drawable.icon_battery_charging_95_99, widgetBatteryIconRes(9, true))
    }

    @Test
    fun `widgetBatteryIconRes_非充電中は各レベルに対応するアイコンを返す`() {
        assertEquals(DesignSystemR.drawable.icon_battery_5_19, widgetBatteryIconRes(0, false))
        assertEquals(DesignSystemR.drawable.icon_battery_20_39, widgetBatteryIconRes(2, false))
        assertEquals(DesignSystemR.drawable.icon_battery_40_59, widgetBatteryIconRes(4, false))
        assertEquals(DesignSystemR.drawable.icon_battery_60_79, widgetBatteryIconRes(6, false))
        assertEquals(DesignSystemR.drawable.icon_battery_80_94, widgetBatteryIconRes(8, false))
        assertEquals(DesignSystemR.drawable.icon_battery_95_100, widgetBatteryIconRes(9, false))
    }
}
