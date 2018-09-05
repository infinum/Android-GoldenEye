package co.infinum.goldeneye

import android.hardware.Camera
import android.media.CamcorderProfile
import android.os.Build
import co.infinum.goldeneye.models.*
import co.infinum.goldeneye.utils.LogDelegate

internal class CameraConfigImpl(
    override val id: Int,
    override val orientation: Int,
    override val facing: Facing,
    private val onUpdateListener: (CameraProperty) -> Unit
) : CameraConfig {

    override var tapToFocusEnabled = true

    override var previewSize: Size = Size.UNKNOWN
        get() = when {
            field != Size.UNKNOWN -> field
            supportedPreviewSizes.isEmpty() -> Size.UNKNOWN
            else -> supportedPreviewSizes[0]
        }
        set(value) {
            if (supportedPreviewSizes.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.PREVIEW_SIZE)
            } else {
                LogDelegate.log("Unsupported PreviewSize [$value]")
            }
        }

    override var flashMode: FlashMode = FlashMode.OFF
        set(value) {
            if (supportedFlashModes.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.FLASH)
            } else {
                LogDelegate.log("Unsupported FlashMode [$value]")
            }
        }

    override var focusMode: FocusMode = FocusMode.AUTO
        set(value) {
            if (supportedFocusModes.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.FOCUS)
            } else {
                LogDelegate.log("Unsupported FocusMode [$value]")
            }
        }

    override var pictureSize: Size = Size.UNKNOWN
        get() = when {
            field != Size.UNKNOWN -> field
            supportedPictureSizes.isEmpty() -> Size.UNKNOWN
            else -> supportedPictureSizes[0]
        }
        set(value) {
            if (supportedPictureSizes.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.PICTURE_SIZE)
            } else {
                LogDelegate.log("Unsupported ImageSize [$value]")
            }
        }

    override var videoSize: Size = Size.UNKNOWN
        get() = when {
            field != Size.UNKNOWN -> field
            supportedVideoSizes.isEmpty() -> Size.UNKNOWN
            else -> supportedVideoSizes[0]
        }
        set(value) {
            if (supportedVideoSizes.contains(value)) {
                field = value
            } else {
                LogDelegate.log("Unsupported VideoSize [$value]")
            }
        }

    override var previewScale = PreviewScale.NO_SCALE
        set(value) {
            field = value
            onUpdateListener(CameraProperty.SCALE)
        }

    override var whiteBalance = WhiteBalance.AUTO
        set(value) {
            if (supportedWhiteBalance.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.WHITE_BALANCE)
            } else {
                LogDelegate.log("Unsupported WhiteBalance [$value]")
            }
        }

    override var zoomLevel = 0
        set(value) {
            if (isZoomSupported && value in 0..maxZoomLevel) {
                field = value
                onUpdateListener(CameraProperty.ZOOM)
            } else {
                LogDelegate.log("Unsupported ZoomLevel [$value]. Must be in [0, $maxZoomLevel]")
            }
        }

    override var zoomPercentage: Int
        get() = cameraParameters?.zoomRatios?.getOrNull(zoomLevel) ?: 100
        set(value) {
            val zoomLevel = cameraParameters?.zoomRatios?.indexOfFirst { it == value } ?: -1
            if (zoomLevel != -1) {
                this.zoomLevel = zoomLevel
            } else {
                LogDelegate.log("Unsupported ZoomPercentage [$value].")
            }
        }

    override val maxZoomLevel: Int
        get() = cameraParameters?.maxZoom ?: 0

    override val maxZoomPercentage: Int
        get() = cameraParameters?.zoomRatios?.sorted()?.last() ?: 100

    override var videoStabilizationEnabled = false
        set(value) {
            if (isVideoStabilizationSupported) {
                field = value
                onUpdateListener(CameraProperty.VIDEO_STABILIZATION)
            } else {
                LogDelegate.log("VideoStabilization not supported.")
            }
        }

    override var pinchToZoomEnabled = false
        set(value) {
            if (isZoomSupported) {
                field = value
            } else {
                LogDelegate.log("Zoom not supported.")
            }
        }

    override var resetFocusDelay = 7_500L
        set(value) {
            if (value > 0) {
                field = value
            } else {
                LogDelegate.log("Focus delay must be positive.")
            }
        }

    override var pinchToZoomFriction = 1f
        set(value) {
            if (value > 0) {
                field = value
            } else {
                LogDelegate.log("Pinch to zoom friction must be positive.")
            }
        }

    override var sceneMode = SceneMode.UNKNOWN
        set(value) {
            if (supportedSceneModes.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.SCENE_MODE)
            } else {
                LogDelegate.log("Unsupported SceneMode [$value]")
            }
        }

    override var colorEffect = ColorEffect.UNKNOWN
        set(value) {
            if (supportedColorEffects.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.COLOR_EFFECT)
            } else {
                LogDelegate.log("Unsupported ColorEffect [$value]")
            }
        }

    override var videoQuality = VideoQuality.UNKNOWN
        set(value) {
            if (supportedVideoQualities.contains(value)) {
                field = value
            } else {
                LogDelegate.log("Unsupported VideoQuality [$value]")
            }
        }

    override var antibanding = Antibanding.UNKNOWN
        set(value) {
            if (supportedAntibanding.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.ANTIBANDING)
            } else {
                LogDelegate.log("Unsupported Antibanding [$value]")
            }
        }

    override var exposureCompensation = 0
        set(value) {
            cameraParameters?.autoWhiteBalanceLock
            if (isExposureCompensationSupported && value in minExposureCompensation..maxExposureCompensation) {
                field = value
                onUpdateListener(CameraProperty.EXPOSURE_COMPENSATION)
            } else {
                LogDelegate.log("Unsupported ExposureCompensation [$value]")
            }
        }

    override val isVideoStabilizationSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
            && cameraParameters?.isVideoStabilizationSupported == true

    override val isZoomSupported: Boolean
        get() = cameraParameters?.isZoomSupported == true

    override val supportedWhiteBalance: List<WhiteBalance>
        get() = cameraParameters?.supportedWhiteBalance
            ?.map { WhiteBalance.fromString(it) }
            ?.distinct()
            ?.filter { it != WhiteBalance.UNKNOWN }
            ?: emptyList()

    override val supportedSceneModes: List<SceneMode>
        get() = cameraParameters?.supportedSceneModes
            ?.map { SceneMode.fromString(it) }
            ?.distinct()
            ?.filter { it != SceneMode.UNKNOWN }
            ?: emptyList()

    override val supportedColorEffects: List<ColorEffect>
        get() = cameraParameters?.supportedColorEffects
            ?.map { ColorEffect.fromString(it) }
            ?.distinct()
            ?.filter { it != ColorEffect.UNKNOWN }
            ?: emptyList()

    override val supportedAntibanding: List<Antibanding>
        get() = cameraParameters?.supportedAntibanding
            ?.map { Antibanding.fromString(it) }
            ?.distinct()
            ?.filter { it != Antibanding.UNKNOWN }
            ?: emptyList()

    override val supportedFlashModes
        get() = cameraParameters?.supportedFlashModes
            ?.map { FlashMode.fromString(it) }
            ?.distinct()
            ?.filter { it != FlashMode.UNKNOWN }
            ?: emptyList()

    override val supportedFocusModes
        get() = cameraParameters?.supportedFocusModes
            ?.map { FocusMode.fromString(it) }
            ?.distinct()
            ?.filter { it != FocusMode.UNKNOWN }
            ?: emptyList()

    override val supportedZoomPercentages: List<Int>
        get() = cameraParameters?.zoomRatios ?: listOf(100)

    override val supportedPictureSizes
        get() = cameraParameters?.supportedPictureSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()

    override val supportedVideoSizes
        get() = cameraParameters?.supportedVideoSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()

    override val supportedPreviewSizes: List<Size>
        get() = cameraParameters?.supportedPreviewSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()

    override val supportedVideoQualities: List<VideoQuality>
        get() = VideoQuality.values()
            .filter { CamcorderProfile.hasProfile(id, it.key) && it != VideoQuality.UNKNOWN }

    override val minExposureCompensation: Int
        get() = cameraParameters?.minExposureCompensation ?: 0

    override val maxExposureCompensation: Int
        get() = cameraParameters?.maxExposureCompensation ?: 0

    override val isExposureCompensationSupported
        get() = minExposureCompensation != 0 && maxExposureCompensation != 0

    internal var locked = false

    internal var zoomInProgress = false

    internal val smoothZoomEnabled
        get() = cameraParameters?.isSmoothZoomSupported == true

    internal var cameraParameters: Camera.Parameters? = null
        set(value) {
            field = value
            if (value != null && initialized.not()) {
                initialized = true
                setInitialValues(value)
            }
        }

    internal fun toCameraInfo() = CameraInfo(id, orientation, facing)

    private var initialized = false

    private fun setInitialValues(params: Camera.Parameters) {
        this.previewSize = params.previewSize?.toInternalSize() ?: Size.UNKNOWN
        this.pictureSize = params.pictureSize?.toInternalSize() ?: Size.UNKNOWN
        this.antibanding = Antibanding.fromString(params.antibanding)
        this.whiteBalance = WhiteBalance.fromString(params.whiteBalance)
        this.colorEffect = ColorEffect.fromString(params.colorEffect)
        this.flashMode = FlashMode.fromString(params.flashMode)
        this.pinchToZoomEnabled = isZoomSupported
        this.sceneMode = SceneMode.fromString(params.sceneMode)
        this.focusMode = FocusMode.fromString(params.focusMode)
        this.videoQuality = when {
            supportedVideoQualities.contains(VideoQuality.HIGH) -> VideoQuality.HIGH
            supportedVideoQualities.contains(VideoQuality.LOW) -> VideoQuality.LOW
            else -> VideoQuality.UNKNOWN
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            videoStabilizationEnabled = params.videoStabilization
        }
    }
}
