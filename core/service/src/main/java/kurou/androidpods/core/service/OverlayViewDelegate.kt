package kurou.androidpods.core.service

import kurou.androidpods.core.domain.AppleDevice
import kurou.androidpods.core.domain.OverlayPosition

internal interface OverlayViewDelegate {
    fun canDrawOverlays(): Boolean

    fun addOverlayView()

    fun removeOverlayView()

    fun animateHide(onComplete: () -> Unit)

    fun updateContent(devices: List<AppleDevice>)

    fun updatePosition(position: OverlayPosition)

    val hasView: Boolean
    var onUserDismiss: (() -> Unit)?
}
