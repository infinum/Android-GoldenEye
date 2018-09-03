@file:Suppress("DEPRECATION")

package co.infinum.goldeneye

import co.infinum.goldeneye.models.*

interface CameraConfig {
    val id: Int
    val orientation: Int
    val facing: Facing

    var previewSize: Size
    var pictureSize: Size
    var videoSize: Size
    var flashMode: FlashMode
    var focusMode: FocusMode
    var previewScale: PreviewScale
    var whiteBalance: WhiteBalance
    var videoStabilizationEnabled: Boolean
    var tapToFocusEnabled: Boolean
    var pinchToZoomEnabled: Boolean
    var zoomLevel: Int
    var zoomPercentage: Int

    val maxZoomLevel: Int
    val maxZoomPercentage: Int
    val supportedPreviewSizes: List<Size>
    val supportedPictureSizes: List<Size>
    val supportedVideoSizes: List<Size>
    val supportedFlashModes: List<FlashMode>
    val supportedFocusModes: List<FocusMode>
    val supportedWhiteBalance: List<WhiteBalance>
}