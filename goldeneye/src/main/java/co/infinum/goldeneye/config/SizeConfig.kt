package co.infinum.goldeneye.config

import android.hardware.Camera
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.Size
import co.infinum.goldeneye.models.toInternalSize
import co.infinum.goldeneye.utils.LogDelegate

interface SizeConfig {
    var previewSize: Size
    val supportedPreviewSizes: List<Size>

    var pictureSize: Size
    val supportedPictureSizes: List<Size>

    var videoSize: Size
    val supportedVideoSizes: List<Size>
}

internal class SizeConfigImpl(
    private val onUpdateListener: (CameraProperty) -> Unit
) : SizeConfig {

    var params: Camera.Parameters? = null

    override var previewSize = Size.UNKNOWN
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

    override val supportedPreviewSizes
        get() = params?.supportedPreviewSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()

    override var pictureSize = Size.UNKNOWN
        get() = when {
            field != Size.UNKNOWN -> field
            supportedPictureSizes.isEmpty() -> Size.UNKNOWN
            else -> supportedPictureSizes[0]
        }
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

    override val supportedVideoSizes
        get() = params?.supportedVideoSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()
}