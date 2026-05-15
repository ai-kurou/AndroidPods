package kurou.androidpods.feature.widget

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import dagger.hilt.android.EntryPointAccessors
import kurou.androidpods.core.domain.DeviceImages
import kurou.androidpods.core.domain.WidgetBatteryState
import kurou.androidpods.core.designsystem.R as DesignSystemR

class BatteryWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        )
        val stateFlow = entryPoint.widgetBatteryUseCase().observe()
        provideContent {
            val state by stateFlow.collectAsState(initial = null)
            GlanceTheme {
                BatteryWidgetContent(context, state)
            }
        }
    }
}

@Composable
internal fun BatteryWidgetContent(context: Context, state: WidgetBatteryState?) {
    val launchComponent = context.packageManager
        .getLaunchIntentForPackage(context.packageName)?.component
    val baseModifier = GlanceModifier
        .fillMaxSize()
        .background(ImageProvider(R.drawable.widget_background))
        .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
    Column(
        modifier = if (launchComponent != null) {
            baseModifier.clickable(actionStartActivity(launchComponent))
        } else {
            baseModifier
        },
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        if (state == null) {
            Text(
                text = context.getString(R.string.widget_not_detected),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    textAlign = TextAlign.Center,
                ),
            )
        } else {
            Text(
                text = state.deviceName,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                ),
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            val deviceImages = widgetDeviceImages(state.modelCode)
            if (deviceImages != null) {
                if (state.isSingle) {
                    val bodyRes = when (deviceImages) {
                        is DeviceImages.Single -> deviceImages.body
                        is DeviceImages.Tws -> deviceImages.left
                    }
                    Column(
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    ) {
                        Image(
                            provider = ImageProvider(bodyRes),
                            contentDescription = null,
                            modifier = GlanceModifier.size(60.dp),
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.Vertical.CenterVertically,
                        ) {
                            Image(
                                provider = ImageProvider(
                                    widgetBatteryIconRes(state.leftBattery, state.leftCharging),
                                ),
                                contentDescription = null,
                                modifier = GlanceModifier.size(20.dp),
                            )
                            Text(
                                text = levelToText(state.leftBattery),
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurface,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                ),
                            )
                        }
                    }
                } else {
                    val twsImages = deviceImages as? DeviceImages.Tws
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().wrapContentHeight(),
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    ) {
                        if (twsImages != null) {
                            BatteryColumn(
                                label = context.getString(R.string.widget_label_left),
                                level = state.leftBattery,
                                imageRes = twsImages.left,
                                charging = state.leftCharging,
                            )
                            BatteryColumn(
                                label = context.getString(R.string.widget_label_right),
                                level = state.rightBattery,
                                imageRes = twsImages.right,
                                charging = state.rightCharging,
                                modifier = GlanceModifier.padding(horizontal = 24.dp),
                            )
                            BatteryColumn(
                                label = context.getString(R.string.widget_label_case),
                                level = state.caseBattery,
                                imageRes = twsImages.case,
                                charging = state.caseCharging,
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = batteryLevelText(state),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = recordedAtText(context, state.recordedAt),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

@Composable
private fun BatteryColumn(
    label: String,
    level: Int?,
    @DrawableRes imageRes: Int?,
    charging: Boolean,
    modifier: GlanceModifier = GlanceModifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            ),
        )
        if (imageRes != null) {
            Image(
                provider = ImageProvider(imageRes),
                contentDescription = null,
                modifier = GlanceModifier.size(40.dp),
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
        }
        Row(
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(widgetBatteryIconRes(level, charging)),
                contentDescription = null,
                modifier = GlanceModifier.size(20.dp),
            )
            Text(
                text = levelToText(level),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

internal fun widgetDeviceImages(modelCode: Int): DeviceImages? = WIDGET_DEVICE_IMAGES[modelCode]

private val WIDGET_DEVICE_IMAGES: Map<Int, DeviceImages> =
    mapOf(
        0x0220 to
            DeviceImages.Tws(
                left = DesignSystemR.drawable.airpods_1st_gen_left,
                right = DesignSystemR.drawable.airpods_1st_gen_right,
                case = DesignSystemR.drawable.airpods_1st_gen_case,
            ),
        0x0F20 to
            DeviceImages.Tws(
                left = DesignSystemR.drawable.airpods_2nd_gen_left,
                right = DesignSystemR.drawable.airpods_2nd_gen_right,
                case = DesignSystemR.drawable.airpods_2nd_gen_case,
            ),
        0x1320 to
            DeviceImages.Tws(
                left = DesignSystemR.drawable.airpods_3rd_gen_left,
                right = DesignSystemR.drawable.airpods_3rd_gen_right,
                case = DesignSystemR.drawable.airpods_3rd_gen_case,
            ),
        0x1920 to
            DeviceImages.Tws(
                left = DesignSystemR.drawable.airpods_4th_gen_left,
                right = DesignSystemR.drawable.airpods_4th_gen_right,
                case = DesignSystemR.drawable.airpods_4th_gen_case,
            ),
        0x1B20 to
            DeviceImages.Tws(
                left = DesignSystemR.drawable.airpods_4th_gen_anc_left,
                right = DesignSystemR.drawable.airpods_4th_gen_anc_right,
                case = DesignSystemR.drawable.airpods_4th_gen_anc_case,
            ),
        0x0A20 to
            DeviceImages.Single(
                body = DesignSystemR.drawable.airpods_max_1st_gen,
            ),
        0x1F20 to
            DeviceImages.Single(
                body = DesignSystemR.drawable.airpods_max_1st_gen_usb_c,
            ),
        0x2D20 to
            DeviceImages.Single(
                body = DesignSystemR.drawable.airpods_max_2nd_gen,
            ),
        0x0E20 to
            DeviceImages.Tws(
                left = DesignSystemR.drawable.airpods_pro_1st_gen_left,
                right = DesignSystemR.drawable.airpods_pro_1st_gen_right,
                case = DesignSystemR.drawable.airpods_pro_1st_gen_case,
            ),
        0x1420 to
            DeviceImages.Tws(
                left = DesignSystemR.drawable.airpods_pro_2nd_gen_left,
                right = DesignSystemR.drawable.airpods_pro_2nd_gen_right,
                case = DesignSystemR.drawable.airpods_pro_2nd_gen_case,
            ),
        0x2420 to
            DeviceImages.Tws(
                left = DesignSystemR.drawable.airpods_pro_2nd_gen_usb_c_left,
                right = DesignSystemR.drawable.airpods_pro_2nd_gen_usb_c_right,
                case = DesignSystemR.drawable.airpods_pro_2nd_gen_usb_c_case,
            ),
        0x2720 to
            DeviceImages.Tws(
                left = DesignSystemR.drawable.airpods_pro_3rd_gen_left,
                right = DesignSystemR.drawable.airpods_pro_3rd_gen_right,
                case = DesignSystemR.drawable.airpods_pro_3rd_gen_case,
            ),
        0x0520 to
            DeviceImages.Single(
                body = DesignSystemR.drawable.beats_x,
            ),
        0x0620 to
            DeviceImages.Single(
                body = DesignSystemR.drawable.beats_solo_3,
            ),
        0x0920 to
            DeviceImages.Single(
                body = DesignSystemR.drawable.beats_studio_3,
            ),
        0x1720 to
            DeviceImages.Single(
                body = DesignSystemR.drawable.beats_studio_pro,
            ),
        0x0320 to
            DeviceImages.Single(
                body = DesignSystemR.drawable.power_beats_3,
            ),
        0x0D20 to
            DeviceImages.Single(
                body = DesignSystemR.drawable.power_beats_4,
            ),
        0x0C20 to
            DeviceImages.Single(
                body = DesignSystemR.drawable.beats_solo_pro,
            ),
        0x1020 to
            DeviceImages.Single(
                body = DesignSystemR.drawable.beats_flex,
            ),
        0x1120 to
            DeviceImages.Tws(
                left = DesignSystemR.drawable.beats_studio_buds_left,
                right = DesignSystemR.drawable.beats_studio_buds_right,
                case = DesignSystemR.drawable.beats_studio_buds_case,
            ),
        0x1620 to
            DeviceImages.Tws(
                left = DesignSystemR.drawable.beats_studio_buds_plus_left,
                right = DesignSystemR.drawable.beats_studio_buds_plus_right,
                case = DesignSystemR.drawable.beats_studio_buds_plus_case,
            ),
        0x1220 to
            DeviceImages.Tws(
                left = DesignSystemR.drawable.beats_fit_pro_left,
                right = DesignSystemR.drawable.beats_fit_pro_right,
                case = DesignSystemR.drawable.beats_fit_pro_case,
            ),
        0x0B20 to
            DeviceImages.Tws(
                left = DesignSystemR.drawable.power_beats_pro_left,
                right = DesignSystemR.drawable.power_beats_pro_right,
                case = DesignSystemR.drawable.power_beats_pro_case,
            ),
    )

internal fun widgetBatteryIconRes(level: Int?, charging: Boolean): Int {
    if (level == null) return DesignSystemR.drawable.icon_battery_null
    if (level >= 10) return if (charging) {
        DesignSystemR.drawable.icon_battery_charging_100
    } else {
        DesignSystemR.drawable.icon_battery_95_100
    }
    val pct = level * 10 + 5
    return if (charging) widgetChargingBatteryIconRes(pct) else widgetDischargingBatteryIconRes(pct)
}

private fun widgetChargingBatteryIconRes(pct: Int): Int = when {
    pct < 20 -> DesignSystemR.drawable.icon_battery_charging_0_19
    pct < 40 -> DesignSystemR.drawable.icon_battery_charging_20_39
    pct < 60 -> DesignSystemR.drawable.icon_battery_charging_40_59
    pct < 80 -> DesignSystemR.drawable.icon_battery_charging_60_79
    pct < 95 -> DesignSystemR.drawable.icon_battery_charging_80_94
    else -> DesignSystemR.drawable.icon_battery_charging_95_99
}

private fun widgetDischargingBatteryIconRes(pct: Int): Int = when {
    pct < 5 -> DesignSystemR.drawable.icon_battery_0_4
    pct < 20 -> DesignSystemR.drawable.icon_battery_5_19
    pct < 40 -> DesignSystemR.drawable.icon_battery_20_39
    pct < 60 -> DesignSystemR.drawable.icon_battery_40_59
    pct < 80 -> DesignSystemR.drawable.icon_battery_60_79
    pct < 95 -> DesignSystemR.drawable.icon_battery_80_94
    else -> DesignSystemR.drawable.icon_battery_95_100
}

internal fun levelToText(level: Int?) = when {
    level == null -> "--"
    level >= 10 -> "100%"
    else -> "${level * 10 + 5}%"
}

internal fun batteryLevelText(state: WidgetBatteryState): String =
    if (state.isSingle) {
        levelToText(state.leftBattery)
    } else {
        "L: ${levelToText(state.leftBattery)}" +
            "  R: ${levelToText(state.rightBattery)}" +
            "  Case: ${levelToText(state.caseBattery)}"
    }

internal fun recordedAtText(context: Context, recordedAt: Long, now: Long = System.currentTimeMillis()): String {
    val minutes = (now - recordedAt) / 60_000
    return when {
        minutes < 1 -> context.getString(R.string.widget_recorded_just_now)
        minutes < 60 -> context.getString(R.string.widget_recorded_minutes_ago, minutes)
        else -> context.getString(R.string.widget_recorded_hours_ago, minutes / 60)
    }
}
