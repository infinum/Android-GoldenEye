package co.infinum.goldeneye.camera2.config

import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.camera1.config.CameraInfo
import co.infinum.goldeneye.config.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class CameraConfigImpl(
    private val cameraInfo: CameraInfo,
    private val videoConfig: VideoConfigImpl,
    private val featureConfig: FeatureConfigImpl,
    private val sizeConfig: SizeConfigImpl,
    private val zoomConfig: ZoomConfigImpl
) : CameraConfig,
    CameraInfo by cameraInfo,
    VideoConfig by videoConfig,
    FeatureConfig by featureConfig,
    SizeConfig by sizeConfig,
    ZoomConfig by zoomConfig {

    private var initialized = false

    var characteristics: CameraCharacteristics? = null
        set(value) {
            field = value
            sizeConfig.characteristics = value

            if (initialized.not()) {
                sizeConfig.initialize()
            }
        }

}