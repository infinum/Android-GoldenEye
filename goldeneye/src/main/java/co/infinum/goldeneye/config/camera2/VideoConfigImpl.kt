package co.infinum.goldeneye.config.camera2

import android.hardware.camera2.CameraCharacteristics
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.config.BaseVideoConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.Size
import co.infinum.goldeneye.models.VideoQuality
import co.infinum.goldeneye.models.toInternalSize

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class VideoConfigImpl(
    private val id: String,
    onUpdateCallback: (CameraProperty) -> Unit
) : BaseVideoConfig<CameraCharacteristics>(id, onUpdateCallback) {

    override val isVideoStabilizationSupported: Boolean
        get() = supportedVideoStabilizationModes.size > 1

    private val supportedVideoStabilizationModes: List<Int>
        get() = characteristics?.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)?.toList() ?: emptyList()

    override val supportedVideoQualities: List<VideoQuality>
        get() {
            val id = this.id.toIntOrNull() ?: return emptyList()

            val supportedQualities = VideoQuality.values().filter { CamcorderProfile.hasProfile(id, it.key) && it != VideoQuality.UNKNOWN }
            return if (characteristics?.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY
            ) {
                val outputSizes = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?.getOutputSizes(MediaRecorder::class.java)
                    ?.map { it.toInternalSize() }
                    ?: emptyList()
                supportedQualities.filter {
                    val profile = CamcorderProfile.get(id, it.key)
                    outputSizes.contains(Size(profile.videoFrameWidth, profile.videoFrameHeight))
                }
            } else {
                supportedQualities
            }
        }
}