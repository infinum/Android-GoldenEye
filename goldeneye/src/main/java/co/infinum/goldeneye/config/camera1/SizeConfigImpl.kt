@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.config.camera1

import android.hardware.Camera
import co.infinum.goldeneye.config.BaseSizeConfig
import co.infinum.goldeneye.config.SizeConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.PreviewScale
import co.infinum.goldeneye.models.Size
import co.infinum.goldeneye.models.toInternalSize
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.LogDelegate

internal class SizeConfigImpl(
    onUpdateCallback: (CameraProperty) -> Unit
) : BaseSizeConfig<Camera.Parameters>(onUpdateCallback) {

    override val supportedPreviewSizes
        get() = characteristics?.supportedPreviewSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()

    override val supportedPictureSizes
        get() = characteristics?.supportedPictureSizes?.map { it.toInternalSize() }?.sorted() ?: emptyList()

    override val supportedVideoSizes
        get() = characteristics?.supportedVideoSizes
            ?.map { it.toInternalSize() }
            ?.sorted()
            ?: emptyList()
}