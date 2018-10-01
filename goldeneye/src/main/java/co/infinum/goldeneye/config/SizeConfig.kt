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

    /**
     * Returns currently active preview size. Preview size depends on current [previewScale].
     * Preview size is not the size of the TextureView that is used for preview! Preview has
     * its own size that is then scaled inside of the TextureView via Matrix.
     *
     * @see PreviewScale
     */
    var previewSize: Size

    /**
     * List of supported preview sizes. Empty list is returned in case of error.
     */
    val supportedPreviewSizes: List<Size>

    /**
     * Defines the size of taken picture.
     *
     * Returns currently active picture size.
     */
    var pictureSize: Size

    /**
     * List of supported picture sizes. Empty list is returned in case of error.
     */
    val supportedPictureSizes: List<Size>

    /**
     * Returns current video size defined by [VideoConfig.videoQuality].
     */
    val videoSize: Size

    /**
     * @see PreviewScale
     */
    var previewScale: PreviewScale
}

internal abstract class BaseSizeConfig<T : Any>(
    private val cameraInfo: CameraInfo,
    private val videoConfig: VideoConfig,
    private val onUpdateCallback: (CameraProperty) -> Unit
) : SizeConfig {

    lateinit var characteristics: T

    override var previewSize = Size.UNKNOWN
        get() = when (previewScale) {
            PreviewScale.MANUAL,
            PreviewScale.MANUAL_FIT,
            PreviewScale.MANUAL_FILL -> field
            PreviewScale.AUTO_FIT,
            PreviewScale.AUTO_FILL ->
                if (BaseGoldenEyeImpl.state == CameraState.RECORDING_VIDEO) {
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
                Size(profile.videoFrameWidth, profile.videoFrameHeight)
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