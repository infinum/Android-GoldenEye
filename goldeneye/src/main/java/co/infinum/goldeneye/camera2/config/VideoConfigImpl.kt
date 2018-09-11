package co.infinum.goldeneye.camera2.config

import android.hardware.camera2.CameraCharacteristics
import android.media.CamcorderProfile
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.config.VideoConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.VideoQuality
import co.infinum.goldeneye.utils.LogDelegate

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class VideoConfigImpl(
    private var id: Int,
    private var onUpdateListener: (CameraProperty) -> Unit
) : VideoConfig {

    var characteristics: CameraCharacteristics? = null

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
            .filter { CamcorderProfile.hasProfile(id, it.key) && it != VideoQuality.UNKNOWN }

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
        get() = supportedVideoStabilizationModes.size > 1

    private val supportedVideoStabilizationModes: List<Int>
        get() = characteristics?.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)?.toList() ?: emptyList()
}