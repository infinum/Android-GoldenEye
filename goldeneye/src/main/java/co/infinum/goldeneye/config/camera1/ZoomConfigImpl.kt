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
                /* Camera1 has random zoom ratios. Find zoom ratio that is closest to given value */
                field = characteristics.zoomRatios?.minBy { abs(it - value) } ?: 100
                onUpdateCallback(CameraProperty.ZOOM)
            } else {
                LogDelegate.log("Unsupported ZoomLevel [$value]")
            }
        }

    override val maxZoom by lazy {
        characteristics.zoomRatios?.getOrNull(characteristics.maxZoom) ?: 100
    }

    override val isZoomSupported by lazy {
        characteristics.isZoomSupported
    }
}