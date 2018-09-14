package co.infinum.goldeneye.config.camera2

import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.config.BaseZoomConfig
import co.infinum.goldeneye.config.ZoomConfig
import co.infinum.goldeneye.models.CameraProperty

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class ZoomConfigImpl(
    onUpdateListener: (CameraProperty) -> Unit
) : BaseZoomConfig<CameraCharacteristics>(onUpdateListener) {

    override val maxZoom: Int
        get() = ((characteristics?.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 1f) * 100).toInt()

    override val isZoomSupported: Boolean
        get() = maxZoom != 100
}