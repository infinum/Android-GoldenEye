package co.infinum.goldeneye.config.camera2

import android.hardware.camera2.CameraCharacteristics
import android.media.CamcorderProfile
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.config.BaseVideoConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.VideoQuality

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class VideoConfigImpl(
    private val id: String,
    onUpdateCallback: (CameraProperty) -> Unit
) : BaseVideoConfig<CameraCharacteristics>(onUpdateCallback) {

    override val isVideoStabilizationSupported: Boolean by lazy {
        supportedVideoStabilizationModes.size > 1
    }

    private val supportedVideoStabilizationModes: List<Int> by lazy {
        characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)?.toList() ?: emptyList()
    }

    override val supportedVideoQualities: List<VideoQuality> by lazy {
        val id = this.id.toIntOrNull() ?: return@lazy emptyList<VideoQuality>()
        VideoQuality.values().filter { CamcorderProfile.hasProfile(id, it.key) && it != VideoQuality.UNKNOWN }
    }
}