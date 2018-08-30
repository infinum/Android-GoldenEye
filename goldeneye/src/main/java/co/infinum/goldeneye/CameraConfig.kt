@file:Suppress("DEPRECATION")

package co.infinum.goldeneye

import android.hardware.Camera
import co.infinum.goldeneye.LogDelegate.log

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

internal class CameraConfigImpl(
    override val id: Int,
    override val orientation: Int,
    override val facing: Facing,
    private val onUpdateListener: (CameraProperty) -> Unit
) : CameraConfig {

    private var initialized = false
    internal var locked = false

    internal var cameraParameters: Camera.Parameters? = null
        set(value) {
            field = value
            if (value != null && initialized.not()) {
                initialized = true
                setInitialValues(value)
            }
        }

    override var isTapToFocusEnabled = false

    private fun setInitialValues(params: Camera.Parameters) {
        this.previewSize = params.previewSize?.toInternalSize() ?: Size.UNKNOWN
        this.pictureSize = params.pictureSize?.toInternalSize() ?: Size.UNKNOWN
        this.flashMode = FlashMode.fromString(params.flashMode)
        this.focusMode = FocusMode.fromString(params.focusMode)
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
                log("Unsupported PreviewSize [$value]")
            }
        }

    override var flashMode: FlashMode = FlashMode.UNKNOWN
        set(value) {
            if (supportedFlashModes.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.FLASH)
            } else {
                log("Unsupported FlashMode [$value]")
            }
        }

    override var focusMode: FocusMode = FocusMode.AUTO
        set(value) {
            if (supportedFocusModes.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.FOCUS)
            } else {
                log("Unsupported FocusMode [$value]")
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
                log("Unsupported ImageSize [$value]")
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
                log("Unsupported VideoSize [$value]")
            }
        }

    override var previewScale = PreviewScale.NO_SCALE
        set(value) {
            field = value
            onUpdateListener(CameraProperty.SCALE)
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

    override val supportedPictureSizes
        get() = cameraParameters?.supportedPictureSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()

    override val supportedVideoSizes
        get() = cameraParameters?.supportedVideoSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()

    override val supportedPreviewSizes: List<Size>
        get() = cameraParameters?.supportedPreviewSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()
}