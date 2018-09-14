package co.infinum.goldeneye.config

interface CameraConfig :
    CameraInfo,
    VideoConfig,
    FeatureConfig,
    SizeConfig,
    ZoomConfig

internal abstract class CameraConfigImpl<T>(
    private val cameraInfo: CameraInfo,
    private val videoConfig: BaseVideoConfig<T>,
    private val featureConfig: BaseFeatureConfig<T>,
    private val sizeConfig: BaseSizeConfig<T>,
    private val zoomConfig: BaseZoomConfig<T>
) : CameraConfig,
    CameraInfo by cameraInfo,
    VideoConfig by videoConfig,
    FeatureConfig by featureConfig,
    SizeConfig by sizeConfig,
    ZoomConfig by zoomConfig {

    var characteristics: T? = null
        set(value) {
        field = value
            sizeConfig.characteristics = value
            videoConfig.characteristics = value
            featureConfig.characteristics = value
            zoomConfig.characteristics = value
    }
}