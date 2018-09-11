package co.infinum.goldeneye.camera2.config

import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.config.SizeConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.PreviewScale
import co.infinum.goldeneye.models.Size
import co.infinum.goldeneye.models.toInternalSize
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.LogDelegate

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class SizeConfigImpl(
    private val onUpdateListener: (CameraProperty) -> Unit
) : SizeConfig {

    var characteristics: CameraCharacteristics? = null

    fun initialize() {
        this.pictureSize = supportedPictureSizes.firstOrNull() ?: Size.UNKNOWN
        this.videoSize = supportedVideoSizes.firstOrNull() ?: Size.UNKNOWN
        this.previewSize = CameraUtils.findBestMatchingSize(pictureSize, supportedPreviewSizes)
    }

    override var previewSize = Size.UNKNOWN
        set(value) {
            if (supportedPreviewSizes.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.PREVIEW_SIZE)
            } else {
                LogDelegate.log("Unsupported PreviewSize [$value]")
            }
        }

    override val supportedPreviewSizes
        get() = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(SurfaceTexture::class.java)
            ?.map { it.toInternalSize() }
            ?.sorted()
            ?: emptyList()

    override var pictureSize = Size.UNKNOWN
        set(value) {
            if (supportedPictureSizes.contains(value)) {
                field = value
            } else {
                LogDelegate.log("Unsupported PictureSize [$value]")
            }
        }

    override val supportedPictureSizes
        get() = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(ImageReader::class.java)
            ?.map { it.toInternalSize() }
            ?.sorted()
            ?: emptyList()

    override var videoSize = Size.UNKNOWN
        set(value) {
            if (supportedVideoSizes.contains(value)) {
                field = value
            } else {
                LogDelegate.log("Unsupported VideoSize [$value]")
            }
        }

    override val supportedVideoSizes
        get() = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(MediaRecorder::class.java)
            ?.map { it.toInternalSize() }
            ?.sorted()
            ?.filter { if (it.width > it.height) it.height <= 1080 else it.width <= 1080 }
            ?: emptyList()

    override var previewScale: PreviewScale = PreviewScale.SCALE_TO_FIT
        set(value) {
            field = value
            onUpdateListener(CameraProperty.PREVIEW_SCALE)
        }
}