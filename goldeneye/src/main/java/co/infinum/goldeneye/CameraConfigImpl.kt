package co.infinum.goldeneye

import android.hardware.Camera
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.models.*
import co.infinum.goldeneye.utils.LogDelegate

internal class CameraConfigImpl(
    override val id: Int,
    override val orientation: Int,
    override val facing: Facing,
    private val onUpdateListener: (CameraProperty) -> Unit
) : CameraConfig {

    private var initialized = false
    internal var locked = false
    internal var zoomInProgress = false
    internal val smoothZoomEnabled: Boolean
        get() = cameraParameters?.isSmoothZoomSupported ?: false

    internal var cameraParameters: Camera.Parameters? = null
        set(value) {
            field = value
            if (value != null && initialized.not()) {
                initialized = true
                setInitialValues(value)
            }
        }

    private fun setInitialValues(params: Camera.Parameters) {
        this.previewSize = params.previewSize?.toInternalSize() ?: Size.UNKNOWN
        this.pictureSize = params.pictureSize?.toInternalSize() ?: Size.UNKNOWN
        this.flashMode = FlashMode.fromString(params.flashMode)
        this.focusMode = FocusMode.fromString(params.focusMode)
        this.pinchToZoomEnabled = params.isZoomSupported
    }

    override var tapToFocusEnabled = true
    override var pinchToZoomEnabled = false
        set(value) {
            if (cameraParameters?.isZoomSupported == true && value) {
                field = value
            } else {
                LogDelegate.log("Zoom not supported.")
            }
        }

    internal fun toCameraInfo() = CameraInfo(id, orientation, facing)

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
                onUpdateListener(CameraProperty.VIDEO_SIZE)
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
            if (value in 0..maxZoomLevel) {
                field = value
                onUpdateListener(CameraProperty.ZOOM)
            } else {
                LogDelegate.log("Unsupported zoom level [$value]. Must be in [0, $maxZoomLevel]")
            }
        }

    override var zoomPercentage: Int
        get() = cameraParameters?.zoomRatios?.getOrNull(zoomLevel) ?: 100
        set(value) {
            val zoomLevel = cameraParameters?.zoomRatios?.indexOfFirst { it == value } ?: -1
            if (zoomLevel != -1) {
                this.zoomLevel = zoomLevel
            } else {
                LogDelegate.log("Unsupported zoom percentage [$value].")
            }
        }

    override val maxZoomLevel: Int
        get() = cameraParameters?.maxZoom ?: 0

    override val maxZoomPercentage: Int
        get() = cameraParameters?.zoomRatios?.sorted()?.last() ?: 100

    override var videoStabilizationEnabled = false
        @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
        set(value) {
            cameraParameters?.zoom
            if (cameraParameters?.isVideoStabilizationSupported == true && value) {
                field = value
            } else {
                LogDelegate.log("Video stabilization not supported.")
            }
        }

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

    override val supportedWhiteBalance: List<WhiteBalance>
        get() = cameraParameters?.supportedWhiteBalance
            ?.map { WhiteBalance.fromString(it) }
            ?.distinct()
            ?.filter { it != WhiteBalance.UNKNOWN }
            ?: emptyList()

    override val supportedPictureSizes
        get() = cameraParameters?.supportedPictureSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()

    override val supportedVideoSizes
        get() = cameraParameters?.supportedVideoSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()

    override val supportedPreviewSizes: List<Size>
        get() = cameraParameters?.supportedPreviewSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()
}
