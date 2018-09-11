package co.infinum.goldeneye.config

import co.infinum.goldeneye.models.PreviewScale
import co.infinum.goldeneye.models.Size

interface SizeConfig {
    var previewSize: Size
    val supportedPreviewSizes: List<Size>

    var pictureSize: Size
    val supportedPictureSizes: List<Size>

    var videoSize: Size
    val supportedVideoSizes: List<Size>

    var previewScale: PreviewScale
}