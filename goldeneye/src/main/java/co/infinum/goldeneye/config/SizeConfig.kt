package co.infinum.goldeneye.config

import android.media.CamcorderProfile
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

    val videoSize: Size

    var previewScale: PreviewScale
}

internal abstract class BaseSizeConfig<T>(
    private val cameraInfo: CameraInfo,
    private val videoConfig: VideoConfig,
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
                onUpdateCallback(CameraProperty.PICTURE_SIZE)
            } else {
                LogDelegate.log("Unsupported PictureSize [$value]")
            }
        }

    override val videoSize: Size
        get() {
            return if (cameraInfo.id.toIntOrNull() != null) {
                val profile = CamcorderProfile.get(cameraInfo.id.toInt(), videoConfig.videoQuality.key)
                Size(
                    if (cameraInfo.orientation % 180 == 0) profile.videoFrameWidth else profile.videoFrameHeight,
                    if (cameraInfo.orientation % 180 == 0) profile.videoFrameHeight else profile.videoFrameWidth
                )
            } else {
                Size.UNKNOWN
            }
        }

    override var previewScale: PreviewScale = PreviewScale.AUTO_FIT
        set(value) {
            field = value
            onUpdateCallback(CameraProperty.PREVIEW_SCALE)
        }
}