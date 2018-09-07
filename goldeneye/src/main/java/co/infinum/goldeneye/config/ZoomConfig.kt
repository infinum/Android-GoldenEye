package co.infinum.goldeneye.config

import android.hardware.Camera
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.Zoom
import co.infinum.goldeneye.utils.LogDelegate

interface ZoomConfig {
    var zoom: Zoom
    val maxZoom: Zoom
    val supportedZooms: List<Zoom>
    val isZoomSupported: Boolean
}

internal class ZoomConfigImpl(
    private val onUpdateListener: (CameraProperty) -> Unit
) : ZoomConfig {

    var params: Camera.Parameters? = null

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
            percentage = params?.zoomRatios?.getOrNull(params?.maxZoom ?: -1) ?: 100
        )

    override val supportedZooms
        get() = params?.zoomRatios
            ?.mapIndexed { index, ratio -> Zoom(index, ratio) }
            ?: emptyList()

    override val isZoomSupported
        get() = params?.isZoomSupported == true
}