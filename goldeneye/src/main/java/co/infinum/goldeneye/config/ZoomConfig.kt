package co.infinum.goldeneye.config

import co.infinum.goldeneye.models.Zoom

interface ZoomConfig {
    var zoom: Zoom
    val maxZoom: Zoom
    val supportedZooms: List<Zoom>
    val isZoomSupported: Boolean
    var pinchToZoomEnabled: Boolean
    var pinchToZoomFriction: Float
}