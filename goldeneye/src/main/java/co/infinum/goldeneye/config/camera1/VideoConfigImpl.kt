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
) : BaseVideoConfig<Camera.Parameters>(id, onUpdateCallback) {

    override val isVideoStabilizationSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
            && characteristics?.isVideoStabilizationSupported == true

    override val supportedVideoQualities: List<VideoQuality>
        get() =
            if (id.toIntOrNull() != null) {
                VideoQuality.values()
                    .filter { it.isCamera2Required().not() }
                    .filter { CamcorderProfile.hasProfile(id.toInt(), it.key) && it != VideoQuality.UNKNOWN }
            } else {
                listOf()
            }
}