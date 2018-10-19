@file:Suppress("DEPRECATION", "ConvertCallChainIntoSequence")

package co.infinum.goldeneye.config.camera1

import android.hardware.Camera
import co.infinum.goldeneye.config.BaseSizeConfig
import co.infinum.goldeneye.config.CameraInfo
import co.infinum.goldeneye.config.VideoConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.Size
import co.infinum.goldeneye.models.toInternalSize

internal class SizeConfigImpl(
    cameraInfo: CameraInfo,
    videoConfig: VideoConfig,
    onUpdateCallback: (CameraProperty) -> Unit
) : BaseSizeConfig<Camera.Parameters>(cameraInfo, videoConfig, onUpdateCallback) {

    override val supportedPreviewSizes: List<Size> by lazy {
        characteristics.supportedPreviewSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()
    }

    override val supportedPictureSizes: List<Size> by lazy {
        characteristics.supportedPictureSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()
    }
}