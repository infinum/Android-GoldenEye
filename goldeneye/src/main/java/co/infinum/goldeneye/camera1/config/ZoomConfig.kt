package co.infinum.goldeneye.camera1.config

import android.hardware.Camera
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.Zoom
import co.infinum.goldeneye.utils.LogDelegate

interface ZoomConfig {
    var zoom: Zoom
    val maxZoom: Zoom
    val supportedZooms: List<Zoom>
    val isZoomSupported: Boolean
    var pinchToZoomEnabled: Boolean
    var pinchToZoomFriction: Float
}

internal class ZoomConfigImpl(
    private val onUpdateListener: (CameraProperty) -> Unit
) : ZoomConfig {

    var params: Camera.Parameters? = null

    fun initialize() {
        if (isZoomSupported) {
            val zoomLevel = params?.zoom ?: 0
            val zoomRatio = params?.zoomRatios?.getOrNull(zoomLevel) ?: 100
            this.zoom = Zoom(zoomLevel, zoomRatio)
        } else {
            zoom = Zoom(-1, 0)
        }

        this.pinchToZoomEnabled = isZoomSupported
    }

    override var zoom = Zoom(0, 100)
        set(value) {
            if (isZoomSupported && value.level in 0..maxZoom.level) {
                field = value
                onUpdateListener(CameraProperty.ZOOM)
            } else {
                LogDelegate.log("Unsupported ZoomLevel [$value]")
            }
        }

    override val maxZoom
        get() = Zoom(
            level = params?.maxZoom ?: 0,
            ratio = params?.zoomRatios?.getOrNull(params?.maxZoom ?: -1) ?: 100
        )

    override val supportedZooms
        get() = params?.zoomRatios
            ?.mapIndexed { index, ratio -> Zoom(index, ratio) }
            ?: emptyList()

    override val isZoomSupported
        get() = params?.isZoomSupported == true

    override var pinchToZoomEnabled = true
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