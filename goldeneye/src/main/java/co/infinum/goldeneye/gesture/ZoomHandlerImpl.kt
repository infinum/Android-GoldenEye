package co.infinum.goldeneye.gesture

import android.app.Activity
import android.hardware.camera2.CaptureRequest
import android.util.TypedValue
import co.infinum.goldeneye.config.CameraConfig

internal class ZoomHandlerImpl(
    activity: Activity,
    private val config: CameraConfig
): ZoomHandler {

    private val zoomPinchDelta: Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            1f,
            activity.resources.displayMetrics
        ).toInt()
    private var pinchDelta = 0f

    override fun onPinchStarted(pinchDelta: Float) {
        if (config.pinchToZoomEnabled.not()) {
            return
        }

        this.pinchDelta += pinchDelta
        val zoomDelta = (this.pinchDelta / (zoomPinchDelta * config.pinchToZoomFriction)).toInt()

        if (zoomDelta != 0) {
            config.zoom = (config.zoom + zoomDelta).coerceIn(100, config.maxZoom)
        }
        this.pinchDelta %= zoomPinchDelta
    }

    override fun onPinchEnded() {
        pinchDelta = 0f
    }
}