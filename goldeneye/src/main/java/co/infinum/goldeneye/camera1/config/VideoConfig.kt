package co.infinum.goldeneye.camera1.config

import android.hardware.Camera
import android.media.CamcorderProfile
import android.os.Build
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.VideoQuality
import co.infinum.goldeneye.utils.LogDelegate

interface VideoConfig {
    var videoQuality: VideoQuality
    val supportedVideoQualities: List<VideoQuality>
    var videoStabilizationEnabled: Boolean
    val isVideoStabilizationSupported: Boolean
}

internal class VideoConfigImpl(
    private val id: String,
    private val onUpdateListener: (CameraProperty) -> Unit
) : VideoConfig {

    var params: Camera.Parameters? = null

    fun initialize() {
        if (isVideoStabilizationSupported) {
            this.videoStabilizationEnabled = true
        }
        this.videoQuality = when {
            supportedVideoQualities.contains(VideoQuality.HIGH) -> VideoQuality.HIGH
            supportedVideoQualities.contains(VideoQuality.LOW) -> VideoQuality.LOW
            supportedVideoQualities.isNotEmpty() -> supportedVideoQualities[0]
            else -> VideoQuality.UNKNOWN
        }
    }

    override var videoQuality = VideoQuality.UNKNOWN
        set(value) {
            if (supportedVideoQualities.contains(value)) {
                field = value
            } else {
                LogDelegate.log("Unsupported VideoQuality [$value]")
            }
        }

    override val supportedVideoQualities: List<VideoQuality>
        get() = VideoQuality.values()
            .filter { CamcorderProfile.hasProfile(id.toInt(), it.key) && it != VideoQuality.UNKNOWN }

    override var videoStabilizationEnabled = false
        set(value) {
            if (isVideoStabilizationSupported) {
                field = value
                onUpdateListener(CameraProperty.VIDEO_STABILIZATION)
            } else {
                LogDelegate.log("VideoStabilization not supported.")
            }
        }

    override val isVideoStabilizationSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
            && params?.isVideoStabilizationSupported == true
}