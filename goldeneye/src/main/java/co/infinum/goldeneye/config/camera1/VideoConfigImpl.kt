@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.config.camera1

import android.hardware.Camera
import android.media.CamcorderProfile
import co.infinum.goldeneye.config.BaseVideoConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.VideoQuality

internal class VideoConfigImpl(
    private val id: String,
    onUpdateCallback: (CameraProperty) -> Unit
) : BaseVideoConfig<Camera.Parameters>(onUpdateCallback) {

    override val isVideoStabilizationSupported: Boolean by lazy {
        characteristics.isVideoStabilizationSupported
    }

    override val supportedVideoQualities: List<VideoQuality> by lazy {
        val id = id.toIntOrNull() ?: return@lazy emptyList<VideoQuality>()
        VideoQuality.values()
            /* This check is must have! Otherwise Camera1 would have false positive VideoQualities that crash the camera. */
            .filter { it.isCamera2Required().not() }
            .filter { CamcorderProfile.hasProfile(id, it.key) && it != VideoQuality.UNKNOWN }
    }
}