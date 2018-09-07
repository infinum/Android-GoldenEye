package co.infinum.goldeneye.config

import android.hardware.Camera

interface CameraConfig :
    GoldenEyeConfig,
    CameraInfo,
    VideoConfig,
    FeatureConfig,
    SizeConfig,
    ZoomConfig

internal class CameraConfigImpl(
    private val goldenEyeConfig: GoldenEyeConfig,
    private val cameraInfo: CameraInfo,
    private val videoConfig: VideoConfigImpl,
    private val featureConfig: FeatureConfigImpl,
    private val sizeConfig: SizeConfigImpl,
    private val zoomConfig: ZoomConfigImpl
) : CameraConfig,
    GoldenEyeConfig by goldenEyeConfig,
    CameraInfo by cameraInfo,
    VideoConfig by videoConfig,
    FeatureConfig by featureConfig,
    SizeConfig by sizeConfig,
    ZoomConfig by zoomConfig {

    var locked = false

    var initialized = false

    var params: Camera.Parameters? = null
        set(value) {
            field = value
            videoConfig.params = value
            featureConfig.params = value
            sizeConfig.params = value
            zoomConfig.params

            if (initialized.not()) {
                initialized = true

            }
        }
}