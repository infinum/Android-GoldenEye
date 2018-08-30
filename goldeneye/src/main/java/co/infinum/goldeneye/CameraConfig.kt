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
    var isTapToFocusEnabled: Boolean

    val supportedPreviewSizes: List<Size>
    val supportedPictureSizes: List<Size>
    val supportedVideoSizes: List<Size>
    val supportedFlashModes: List<FlashMode>
    val supportedFocusModes: List<FocusMode>
}