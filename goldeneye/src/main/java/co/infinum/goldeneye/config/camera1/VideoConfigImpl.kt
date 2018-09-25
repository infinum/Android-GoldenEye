@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.config.camera1

import android.hardware.Camera
import android.media.CamcorderProfile
import android.os.Build
import co.infinum.goldeneye.config.BaseVideoConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.VideoQuality

internal class VideoConfigImpl(
    private val id: String,
    onUpdateCallback: (CameraProperty) -> Unit
) : BaseVideoConfig<Camera.Parameters>(onUpdateCallback) {

    override val isVideoStabilizationSupported: Boolean by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 && characteristics.isVideoStabilizationSupported
    }

    override val supportedVideoQualities: List<VideoQuality> by lazy {
        val id = id.toIntOrNull() ?: return@lazy emptyList<VideoQuality>()
        VideoQuality.values()
            .filter { it.isCamera2Required().not() }
            .filter { CamcorderProfile.hasProfile(id, it.key) && it != VideoQuality.UNKNOWN }
    }
}