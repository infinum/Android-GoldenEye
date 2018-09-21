@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.config.camera1

import android.hardware.Camera
import co.infinum.goldeneye.config.BaseZoomConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.utils.LogDelegate
import kotlin.math.abs

internal class ZoomConfigImpl(
    onUpdateCallback: (CameraProperty) -> Unit
) : BaseZoomConfig<Camera.Parameters>(onUpdateCallback) {

    override var zoom = 100
        set(value) {
            if (isZoomSupported) {
                field = characteristics?.zoomRatios?.minBy { abs(it - value) } ?: 100
                onUpdateCallback(CameraProperty.ZOOM)
            } else {
                LogDelegate.log("Unsupported ZoomLevel [$value]")
            }
        }

    override val maxZoom
        get() = characteristics?.zoomRatios?.getOrNull(characteristics?.maxZoom ?: -1) ?: 100

    override val isZoomSupported
        get() = characteristics?.isZoomSupported == true
}