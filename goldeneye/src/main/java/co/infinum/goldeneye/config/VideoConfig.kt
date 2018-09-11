package co.infinum.goldeneye.config

import co.infinum.goldeneye.models.VideoQuality

interface VideoConfig {
    var videoQuality: VideoQuality
    val supportedVideoQualities: List<VideoQuality>
    var videoStabilizationEnabled: Boolean
    val isVideoStabilizationSupported: Boolean
}