package co.infinum.goldeneye.config

import android.hardware.Camera
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.PreviewScale
import co.infinum.goldeneye.models.Size
import co.infinum.goldeneye.models.toInternalSize
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

internal class SizeConfigImpl(
    private val onUpdateListener: (CameraProperty) -> Unit
) : SizeConfig {

    var params: Camera.Parameters? = null

    fun initialize() {
        this.pictureSize = params?.pictureSize?.toInternalSize() ?: Size.UNKNOWN
        this.videoSize = params?.supportedVideoSizes?.getOrNull(0)?.toInternalSize() ?: Size.UNKNOWN
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
        get() = params?.supportedPreviewSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()

    override var pictureSize = Size.UNKNOWN
        set(value) {
            if (supportedPictureSizes.contains(value)) {
                field = value
            } else {
                LogDelegate.log("Unsupported PictureSize [$value]")
            }
        }

    override val supportedPictureSizes
        get() = params?.supportedPictureSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()

    override var videoSize = Size.UNKNOWN
        set(value) {
            if (supportedVideoSizes.contains(value)) {
                field = value
            } else {
                LogDelegate.log("Unsupported VideoSize [$value]")
            }
        }

    override val supportedVideoSizes
        get() = params?.supportedVideoSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()

    override var previewScale: PreviewScale = PreviewScale.SCALE_TO_FIT
        set(value) {
            field = value
            onUpdateListener(CameraProperty.PREVIEW_SCALE)
        }
}