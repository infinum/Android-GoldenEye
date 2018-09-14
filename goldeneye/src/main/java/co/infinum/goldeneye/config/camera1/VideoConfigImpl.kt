@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.config.camera1

import android.hardware.Camera
import android.media.CamcorderProfile
import android.os.Build
import co.infinum.goldeneye.config.BaseVideoConfig
import co.infinum.goldeneye.config.VideoConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.VideoQuality
import co.infinum.goldeneye.utils.LogDelegate

internal class VideoConfigImpl(
    id: Int,
    onUpdateListener: (CameraProperty) -> Unit
) : BaseVideoConfig<Camera.Parameters>(id, onUpdateListener) {

    override val isVideoStabilizationSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
            && characteristics?.isVideoStabilizationSupported == true
}