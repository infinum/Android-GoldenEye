package co.infinum.goldeneye.config

import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.utils.LogDelegate

interface ZoomConfig {
    var zoom: Int
    val maxZoom: Int
    val isZoomSupported: Boolean
    var pinchToZoomEnabled: Boolean
    var pinchToZoomFriction: Float
}

internal abstract class BaseZoomConfig<T: Any>(
    protected val onUpdateCallback: (CameraProperty) -> Unit
) : ZoomConfig {

    lateinit var characteristics: T

    override var pinchToZoomEnabled = true
        get() = field && isZoomSupported
        set(value) {
            field = isZoomSupported && value
        }

    override var pinchToZoomFriction = 1f
        set(value) {
            if (value > 0) {
                field = value
            } else {
                LogDelegate.log("Pinch to zoom friction must be bigger than 0.")
            }
        }
}