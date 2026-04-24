package kurou.androidpods.core.service

import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.OverlayPosition

internal class BatteryOverlayManager(private val delegate: OverlayViewDelegate) {
    /** dismiss時点の各デバイスの lidOpenCounter。同じカウンター値の間は再表示しない */
    private val dismissedLidCounters = mutableMapOf<String, Int>()

    /** 現在オーバーレイに表示中のデバイスキーと lidOpenCounter */
    private var currentDevices = emptyMap<String, Int>()

    init {
        delegate.onUserDismiss = {
            dismissedLidCounters.putAll(currentDevices)
        }
    }

    fun show(devices: List<AppleDevice>) {
        if (!delegate.canDrawOverlays()) return

        // 圏外になったデバイスのdismissed状態をクリア
        val incomingKeys = devices.map { it.modelCode.toString() }.toSet()
        dismissedLidCounters.keys.retainAll(incomingKeys)

        if (devices.isEmpty()) {
            hide()
            return
        }

        // dismissed済み かつ lidOpenCounter が変わっていないデバイスを除外
        val visibleDevices =
            devices.filter { device ->
                val key = device.modelCode.toString()
                val dismissedCounter = dismissedLidCounters[key]
                // dismissedされていない、または蓋が新たに開かれた（カウンターが変化した）場合は表示
                dismissedCounter == null || device.lidOpenCounter != dismissedCounter
            }

        if (visibleDevices.isEmpty()) {
            hide()
            return
        }

        // lidOpenCounter が変化したデバイスはdismissed状態をクリア
        for (device in visibleDevices) {
            val key = device.modelCode.toString()
            if (key in dismissedLidCounters) {
                dismissedLidCounters.remove(key)
            }
        }

        currentDevices = visibleDevices.associate { it.modelCode.toString() to it.lidOpenCounter }
        if (!delegate.hasView) {
            delegate.addOverlayView()
        }
        delegate.updateContent(visibleDevices)
    }

    fun updatePosition(position: OverlayPosition) {
        delegate.updatePosition(position)
    }

    fun hide() {
        currentDevices = emptyMap()
        if (!delegate.hasView) return
        delegate.animateHide {
            delegate.removeOverlayView()
        }
    }
}
