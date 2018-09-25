package co.infinum.goldeneye.config

import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.VideoQuality
import co.infinum.goldeneye.utils.LogDelegate

interface VideoConfig {
    var videoQuality: VideoQuality
    val supportedVideoQualities: List<VideoQuality>
    var videoStabilizationEnabled: Boolean
    val isVideoStabilizationSupported: Boolean
}

internal abstract class BaseVideoConfig<T: Any>(
    private val onUpdateCallback: (CameraProperty) -> Unit
) : VideoConfig {

    lateinit var characteristics: T

    override var videoQuality = VideoQuality.UNKNOWN
        get() = when {
            field != VideoQuality.UNKNOWN -> field
            supportedVideoQualities.contains(VideoQuality.RESOLUTION_2160P) -> VideoQuality.RESOLUTION_2160P
            supportedVideoQualities.contains(VideoQuality.RESOLUTION_1080P) -> VideoQuality.RESOLUTION_1080P
            supportedVideoQualities.contains(VideoQuality.RESOLUTION_720P) -> VideoQuality.RESOLUTION_720P
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
