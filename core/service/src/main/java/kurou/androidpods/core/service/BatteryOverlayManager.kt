package kurou.androidpods.core.service

import kurou.androidpods.core.domain.AppleDevice

internal class BatteryOverlayManager(private val delegate: OverlayViewDelegate) {

    /** ユーザーが明示的に閉じたデバイスのキー（modelCode）。デバイスが圏外になるまで再表示しない */
    private val dismissedDeviceKeys = mutableSetOf<String>()
    /** 現在オーバーレイに表示中のデバイスキー */
    private var currentDeviceKeys = emptySet<String>()

    init {
        delegate.onUserDismiss = {
            dismissedDeviceKeys.addAll(currentDeviceKeys)
        }
    }

    fun show(devices: List<AppleDevice>) {
        if (!delegate.canDrawOverlays()) return

        // 現在検出中のデバイスキー
        val incomingKeys = devices.map { it.modelCode.toString() }.toSet()
        // 圏外になったデバイスのdismissed状態をクリア（次回検出時に再表示可能にする）
        dismissedDeviceKeys.retainAll(incomingKeys)

        if (devices.isEmpty()) {
            hide()
            return
        }

        // dismissed済みデバイスを除外
        val visibleDevices = devices.filter { it.modelCode.toString() !in dismissedDeviceKeys }
        if (visibleDevices.isEmpty()) {
            hide()
            return
        }

        currentDeviceKeys = visibleDevices.map { it.modelCode.toString() }.toSet()
        if (!delegate.hasView) {
            delegate.addOverlayView()
        }
        delegate.updateContent(visibleDevices)
    }

    fun hide() {
        currentDeviceKeys = emptySet()
        if (!delegate.hasView) return
        delegate.animateHide {
            delegate.removeOverlayView()
        }
    }
}
