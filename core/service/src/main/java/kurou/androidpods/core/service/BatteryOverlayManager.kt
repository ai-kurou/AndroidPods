package kurou.androidpods.core.service

import kurou.androidpods.core.domain.AppleDevice

internal class BatteryOverlayManager(private val delegate: OverlayViewDelegate) {

    fun show(devices: List<AppleDevice>) {
        if (!delegate.canDrawOverlays()) return
        if (devices.isEmpty()) {
            hide()
            return
        }

        if (!delegate.hasView) {
            delegate.addOverlayView()
        }
        delegate.updateContent(devices)
    }

    fun hide() {
        if (!delegate.hasView) return
        delegate.animateHide {
            delegate.removeOverlayView()
        }
    }
}
