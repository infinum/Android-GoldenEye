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
    var resetFocusDelay: Long
    var pinchToZoomFriction: Float
    var sceneMode: SceneMode
    var colorEffect: ColorEffect
    var antibanding: Antibanding
    var zoomLevel: Int
    var zoomPercentage: Int
    var videoQuality: VideoQuality
    var exposureCompensation: Int

    val maxZoomLevel: Int
    val maxZoomPercentage: Int
    val supportedPreviewSizes: List<Size>
    val supportedPictureSizes: List<Size>
    val supportedVideoSizes: List<Size>
    val supportedFlashModes: List<FlashMode>
    val supportedFocusModes: List<FocusMode>
    val supportedWhiteBalance: List<WhiteBalance>
    val supportedZoomPercentages: List<Int>
    val supportedSceneModes: List<SceneMode>
    val supportedColorEffects: List<ColorEffect>
    val supportedAntibanding: List<Antibanding>
    val supportedVideoQualities: List<VideoQuality>
    val minExposureCompensation: Int
    val maxExposureCompensation: Int
    val isExposureCompensationSupported: Boolean
    val isVideoStabilizationSupported: Boolean
    val isZoomSupported: Boolean
}