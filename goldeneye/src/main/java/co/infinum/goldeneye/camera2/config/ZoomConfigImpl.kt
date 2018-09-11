package co.infinum.goldeneye.camera2.config

import co.infinum.goldeneye.config.ZoomConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.Zoom

internal class ZoomConfigImpl(
    private val onUpdateCallback: (CameraProperty) -> Unit
) : ZoomConfig {

    override var zoom: Zoom
        get() = TODO("not implemented")
        set(value) {}
    override val maxZoom: Zoom
        get() = TODO("not implemented")
    override val supportedZooms: List<Zoom>
        get() = TODO("not implemented")
    override val isZoomSupported: Boolean
        get() = TODO("not implemented")
    override var pinchToZoomEnabled: Boolean
        get() = TODO("not implemented")
        set(value) {}
    override var pinchToZoomFriction: Float
        get() = TODO("not implemented")
        set(value) {}
}