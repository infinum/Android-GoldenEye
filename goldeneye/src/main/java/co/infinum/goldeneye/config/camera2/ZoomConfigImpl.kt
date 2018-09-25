package co.infinum.goldeneye.config.camera2

import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.config.BaseZoomConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.utils.LogDelegate

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class ZoomConfigImpl(
    onUpdateCallback: (CameraProperty) -> Unit
) : BaseZoomConfig<CameraCharacteristics>(onUpdateCallback) {

    override var zoom = 100
        set(value) {
            if (isZoomSupported) {
                field = value.coerceIn(100, maxZoom)
                onUpdateCallback(CameraProperty.ZOOM)
            } else {
                LogDelegate.log("Unsupported ZoomLevel [$value]")
            }
        }

    override val maxZoom: Int by lazy {
        ((characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 1f) * 100).toInt()
    }

    override val isZoomSupported: Boolean by lazy {
        maxZoom != 100
    }
}