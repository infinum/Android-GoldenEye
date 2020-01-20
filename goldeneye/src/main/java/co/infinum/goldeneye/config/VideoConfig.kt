package co.infinum.goldeneye.config

import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.VideoQuality
import co.infinum.goldeneye.utils.LogDelegate

interface VideoConfig {
    /**
     * Defines the quality of recorded video.
     *
     * Default value is the first supported of [VideoQuality.RESOLUTION_2160P],
     * [VideoQuality.RESOLUTION_1080P], [VideoQuality.RESOLUTION_720P], [VideoQuality.RESOLUTION_480P],
     * [VideoQuality.RESOLUTION_QVGA], [VideoQuality.HIGH],
     * [VideoQuality.LOW]. If none is supported, [VideoQuality.UNKNOWN] is used.
     *
     * @see VideoQuality
     */
    var videoQuality: VideoQuality

    /**
     * Returns list of supported video qualities.
     *
     * Empty list is returned in case of error or for external cameras.
     *
     * IMPORTANT: For now, video recording via external cameras is disabled because
     * it is impossible to fetch available video qualities via current API.
     */
    val supportedVideoQualities: List<VideoQuality>

    /**
     * Video stabilization toggle.
     *
     * Default value is false.
     */
    var videoStabilizationEnabled: Boolean

    /**
     * Returns whether video stabilization is supported.
     */
    val isVideoStabilizationSupported: Boolean
}

internal abstract class BaseVideoConfig<T : Any>(
    private val onUpdateCallback: (CameraProperty) -> Unit
) : VideoConfig {

    lateinit var characteristics: T

    override var videoQuality = VideoQuality.UNKNOWN
        get() = when {
            field != VideoQuality.UNKNOWN -> field
            supportedVideoQualities.contains(VideoQuality.RESOLUTION_2160P) -> VideoQuality.RESOLUTION_2160P
            supportedVideoQualities.contains(VideoQuality.RESOLUTION_1080P) -> VideoQuality.RESOLUTION_1080P
            supportedVideoQualities.contains(VideoQuality.RESOLUTION_720P) -> VideoQuality.RESOLUTION_720P
            supportedVideoQualities.contains(VideoQuality.RESOLUTION_480P) -> VideoQuality.RESOLUTION_480P
            supportedVideoQualities.contains(VideoQuality.RESOLUTION_QVGA) -> VideoQuality.RESOLUTION_QVGA
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
