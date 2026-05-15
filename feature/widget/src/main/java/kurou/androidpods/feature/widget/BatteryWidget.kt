package kurou.androidpods.feature.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kurou.androidpods.core.domain.WidgetBatteryState

class BatteryWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        )
        val state = entryPoint.widgetBatteryUseCase().observe().first()
        provideContent {
            GlanceTheme {
                BatteryWidgetContent(context, state)
            }
        }
    }
}

@Composable
internal fun BatteryWidgetContent(context: Context, state: WidgetBatteryState?) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        if (state == null) {
            Text(
                text = context.getString(R.string.widget_not_detected),
                style = TextStyle(color = GlanceTheme.colors.onSurface),
            )
        } else {
            Text(
                text = state.deviceName,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                ),
            )
            Text(
                text = batteryLevelText(state),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 13.sp,
                ),
            )
            Text(
                text = recordedAtText(context, state.recordedAt),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 11.sp,
                ),
            )
        }
    }
}

internal fun batteryLevelText(state: WidgetBatteryState): String {
    fun levelToText(level: Int?) = when {
        level == null -> "--"
        level >= 10 -> "100%"
        else -> "${level * 10 + 5}%"
    }
    return if (state.isSingle) {
        levelToText(state.leftBattery)
    } else {
        "L: ${levelToText(state.leftBattery)}" +
            "  R: ${levelToText(state.rightBattery)}" +
            "  Case: ${levelToText(state.caseBattery)}"
    }
}

internal fun recordedAtText(context: Context, recordedAt: Long, now: Long = System.currentTimeMillis()): String {
    val minutes = (now - recordedAt) / 60_000
    return when {
        minutes < 1 -> context.getString(R.string.widget_recorded_just_now)
        minutes < 60 -> context.getString(R.string.widget_recorded_minutes_ago, minutes)
        else -> context.getString(R.string.widget_recorded_hours_ago, minutes / 60)
    }
}
