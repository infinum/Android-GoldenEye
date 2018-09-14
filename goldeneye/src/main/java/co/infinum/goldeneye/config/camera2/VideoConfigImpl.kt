package co.infinum.goldeneye.config.camera2

import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.config.BaseVideoConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.VideoQuality

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class VideoConfigImpl(
    id: Int,
    onUpdateListener: (CameraProperty) -> Unit
) : BaseVideoConfig<CameraCharacteristics>(id, onUpdateListener) {

    override val isVideoStabilizationSupported: Boolean
        get() = supportedVideoStabilizationModes.size > 1

    private val supportedVideoStabilizationModes: List<Int>
        get() = characteristics?.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)?.toList() ?: emptyList()
}