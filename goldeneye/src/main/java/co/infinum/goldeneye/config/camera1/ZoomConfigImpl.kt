@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.config.camera1

import android.hardware.Camera
import co.infinum.goldeneye.config.BaseZoomConfig
import co.infinum.goldeneye.config.ZoomConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.utils.LogDelegate

internal class ZoomConfigImpl(
    onUpdateListener: (CameraProperty) -> Unit
) : BaseZoomConfig<Camera.Parameters>(onUpdateListener) {

    override val maxZoom
        get() = characteristics?.zoomRatios?.getOrNull(characteristics?.maxZoom ?: -1) ?: 100

    override val isZoomSupported
        get() = characteristics?.isZoomSupported == true
}