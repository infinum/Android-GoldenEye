package co.infinum.goldeneye.config

import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.utils.LogDelegate

interface ZoomConfig {
    /**
     * Defines current zoom ratio. [zoom] value is equal to zoom ratio * 100.
     * Value must be integer between 100 and [maxZoom].
     *
     * Example - if zoom ratio is 2.5x, then [zoom] value is 250.
     *
     * Default value is 100.
     */
    var zoom: Int

    /**
     * Defines max supported digital zoom.
     */
    val maxZoom: Int

    /**
     * Returns whether zoom is supported. Some cameras do not support zoom.
     */
    val isZoomSupported: Boolean

    /**
     * Pinch to zoom toggle. [co.infinum.goldeneye.OnZoomChangedCallback] is triggered
     * every time zoom change happens.
     *
     * Default value is true if zoom is supported, otherwise false.
     *
     * @see co.infinum.goldeneye.OnZoomChangedCallback
     */
    var pinchToZoomEnabled: Boolean

    /**
     * Defines how sensitive pinch to zoom is.
     *
     * Value must be positive. Lower friction means bigger sensitivity.
     *
     * Default friction is 1f.
     */
    var pinchToZoomFriction: Float
}

internal abstract class BaseZoomConfig<T : Any>(
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