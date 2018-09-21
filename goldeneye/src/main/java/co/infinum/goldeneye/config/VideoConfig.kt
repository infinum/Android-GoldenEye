package co.infinum.goldeneye.config

import android.media.CamcorderProfile
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.VideoQuality
import co.infinum.goldeneye.utils.LogDelegate

interface VideoConfig {
    var videoQuality: VideoQuality
    val supportedVideoQualities: List<VideoQuality>
    var videoStabilizationEnabled: Boolean
    val isVideoStabilizationSupported: Boolean
}

internal abstract class BaseVideoConfig<T>(
    private val id: String,
    private val onUpdateCallback: (CameraProperty) -> Unit
) : VideoConfig {

    var characteristics: T? = null

    override var videoQuality = VideoQuality.UNKNOWN
        get() = when {
            field != VideoQuality.UNKNOWN -> field
            supportedVideoQualities.contains(VideoQuality.HIGH) -> VideoQuality.HIGH
            supportedVideoQualities.contains(VideoQuality.LOW) -> VideoQuality.LOW
            else -> VideoQuality.UNKNOWN
        }
        set(value) {
            if (supportedVideoQualities.contains(value)) {
                field = value
            } else {
                LogDelegate.log("Unsupported VideoQuality [$value]")
            }
        }

    override val supportedVideoQualities: List<VideoQuality>
        get() =
            if (id.toIntOrNull() != null) {
                VideoQuality.values()
                    .filter { CamcorderProfile.hasProfile(id.toInt(), it.key) && it != VideoQuality.UNKNOWN }
            } else {
                listOf()
            }

    override var videoStabilizationEnabled = false
        get() = isVideoStabilizationSupported && field
        set(value) {
            if (isVideoStabilizationSupported) {
                field = value
                onUpdateCallback(CameraProperty.VIDEO_STABILIZATION)
            } else {
                LogDelegate.log("VideoStabilization not supported.")
            }
        }
}
