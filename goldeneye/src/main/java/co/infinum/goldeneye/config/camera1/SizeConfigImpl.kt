@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.config.camera1

import android.hardware.Camera
import co.infinum.goldeneye.config.BaseSizeConfig
import co.infinum.goldeneye.config.CameraInfo
import co.infinum.goldeneye.config.SizeConfig
import co.infinum.goldeneye.config.VideoConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.PreviewScale
import co.infinum.goldeneye.models.Size
import co.infinum.goldeneye.models.toInternalSize
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.LogDelegate

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