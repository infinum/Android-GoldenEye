package co.infinum.goldeneye.config

import co.infinum.goldeneye.BaseGoldenEyeImpl
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.CameraState
import co.infinum.goldeneye.models.PreviewScale
import co.infinum.goldeneye.models.Size
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.LogDelegate

interface SizeConfig {
    var previewSize: Size
    val supportedPreviewSizes: List<Size>

    var pictureSize: Size
    val supportedPictureSizes: List<Size>

    var videoSize: Size
    val supportedVideoSizes: List<Size>

    var previewScale: PreviewScale
}

internal abstract class BaseSizeConfig<T>(
    private val onUpdateCallback: (CameraProperty) -> Unit
) : SizeConfig {

    var characteristics: T? = null

    override var previewSize = Size.UNKNOWN
        get() = when (previewScale) {
            PreviewScale.MANUAL,
            PreviewScale.MANUAL_FIT,
            PreviewScale.MANUAL_FILL -> field
            PreviewScale.AUTO_FIT,
            PreviewScale.AUTO_FILL ->
                if (BaseGoldenEyeImpl.state == CameraState.RECORDING) {
                    CameraUtils.findBestMatchingSize(videoSize, supportedPreviewSizes)
                } else {
                    CameraUtils.findBestMatchingSize(pictureSize, supportedPreviewSizes)
                }
        }
        set(value) {
            if (supportedPreviewSizes.contains(value)) {
                field = value
                onUpdateCallback(CameraProperty.PREVIEW_SIZE)
            } else {
                LogDelegate.log("Unsupported PreviewSize [$value]")
            }
        }

    override var pictureSize = Size.UNKNOWN
        get() = when {
            field != Size.UNKNOWN -> field
            supportedPictureSizes.isNotEmpty() -> supportedPictureSizes[0]
            else -> Size.UNKNOWN
        }
        set(value) {
            if (supportedPictureSizes.contains(value)) {
                field = value
            } else {
                LogDelegate.log("Unsupported PictureSize [$value]")
            }
        }

    override var videoSize = Size.UNKNOWN
        get() = when {
            field != Size.UNKNOWN -> field
            supportedVideoSizes.isNotEmpty() -> supportedVideoSizes[0]
            else -> Size.UNKNOWN
        }
        set(value) {
            if (supportedVideoSizes.contains(value)) {
                field = value
            } else {
                LogDelegate.log("Unsupported VideoSize [$value]")
            }
        }

    override var previewScale: PreviewScale = PreviewScale.AUTO_FIT
        set(value) {
            field = value
            onUpdateCallback(CameraProperty.PREVIEW_SCALE)
        }
}