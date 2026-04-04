package kurou.androidpods.core.service

import kurou.androidpods.core.domain.AppleDevice

internal interface OverlayViewDelegate {
    fun canDrawOverlays(): Boolean
    fun addOverlayView()
    fun removeOverlayView()
    fun animateHide(onComplete: () -> Unit)
    fun updateContent(devices: List<AppleDevice>)
    val hasView: Boolean
}
