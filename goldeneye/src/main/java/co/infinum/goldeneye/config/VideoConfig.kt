package co.infinum.goldeneye.config

import android.hardware.Camera
import android.media.CamcorderProfile
import android.os.Build
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.VideoQuality
import co.infinum.goldeneye.utils.LogDelegate

interface VideoConfig {
    var videoQuality: VideoQuality
    var videoStabilizationEnabled: Boolean
    val supportedVideoQualities: List<VideoQuality>
    val isVideoStabilizationSupported: Boolean
}

internal class VideoConfigImpl(
    private val id: Int,
    private val onUpdateListener: (CameraProperty) -> Unit
) : VideoConfig {

    var params: Camera.Parameters? = null

    override var videoQuality = VideoQuality.UNKNOWN
        set(value) {
            if (supportedVideoQualities.contains(value)) {
                field = value
            } else {
                LogDelegate.log("Unsupported VideoQuality [$value]")
            }
        }

    override var videoStabilizationEnabled = false
        set(value) {
            if (isVideoStabilizationSupported) {
                field = value
                onUpdateListener(CameraProperty.VIDEO_STABILIZATION)
            } else {
                LogDelegate.log("VideoStabilization not supported.")
            }
        }

    override val supportedVideoQualities
        get() = VideoQuality.values()
            .filter { CamcorderProfile.hasProfile(id, it.key) && it != VideoQuality.UNKNOWN }

    override val isVideoStabilizationSupported
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
            && params?.isVideoStabilizationSupported == true
}