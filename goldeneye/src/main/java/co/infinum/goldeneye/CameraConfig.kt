package co.infinum.goldeneye

import android.hardware.Camera
import co.infinum.goldeneye.LogDelegate.log


interface CameraConfig {
    val id: Int
    val orientation: Int
    val facing: Facing
    val previewSize: Size
    var pictureSize: Size
    var videoSize: Size
    var flashMode: FlashMode
    var focusMode: FocusMode
    var previewType: PreviewType
    var previewScale: PreviewScale

    val supportedPictureSizes: List<Size>
    val supportedVideoSizes: List<Size>
    val supportedFlashModes: List<FlashMode>
    val supportedFocusModes: List<FocusMode>
}

internal class CameraConfigImpl(
    override val id: Int,
    override val orientation: Int,
    override val facing: Facing
) : CameraConfig {

    internal var cameraParameters: Camera.Parameters? = null

    internal fun toCameraInfo() = CameraInfo(id, orientation, facing)

    override val previewSize: Size
        get() {
            val referenceSize = if (previewType == PreviewType.PICTURE) pictureSize else videoSize
            return cameraParameters?.supportedPreviewSizes
                ?.map { it.toInternalSize() }
                ?.sorted()
                ?.find { it.aspectRatio == referenceSize.aspectRatio }
                ?: Size.UNKNOWN
        }

    override var flashMode: FlashMode = FlashMode.OFF
        set(value) {
            if (supportedFlashModes.contains(value)) {
                field = value
            } else {
                log("Unsupported FlashMode [$value]")
            }
        }

    override var focusMode: FocusMode = FocusMode.AUTO
        set(value) {
            if (supportedFocusModes.contains(value)) {
                field = value
            } else {
                log("Unsupported FocusMode [$value]")
            }
        }

    override var pictureSize: Size = Size.UNKNOWN
        get() = if (field == Size.UNKNOWN) supportedPictureSizes[0] else field
        set(value) {
            if (supportedPictureSizes.contains(value)) {
                field = value
            } else {
                log("Unsupported ImageSize [$value]")
            }
        }

    override var videoSize: Size = Size.UNKNOWN
        get() = if (field == Size.UNKNOWN) supportedVideoSizes[0] else field
        set(value) {
            if (supportedVideoSizes.contains(value)) {
                field = value
            } else {
                log("Unsupported VideoSize [$value]")
            }
        }

    override var previewType = PreviewType.PICTURE
        set(value) {
            field = value
            //todo react
        }

    override var previewScale = PreviewScale.FIT
        set(value) {
            field = value
            //todo react
        }

    override val supportedFlashModes
        get() = cameraParameters?.supportedFlashModes?.map { FlashMode.fromString(it) }?.distinct() ?: listOf()

    override val supportedFocusModes
        get() = cameraParameters?.supportedFocusModes?.map { FocusMode.fromString(it) }?.distinct() ?: listOf()

    override val supportedPictureSizes
        get() = cameraParameters?.supportedPictureSizes?.map { it.toInternalSize() }?.sorted() ?: listOf()

    override val supportedVideoSizes
        get() = cameraParameters?.supportedVideoSizes?.map { it.toInternalSize() }?.sorted() ?: listOf()
}