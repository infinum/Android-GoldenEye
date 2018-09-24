package co.infinum.goldeneye.config

interface CameraConfig :
    CameraInfo,
    VideoConfig,
    FeatureConfig,
    SizeConfig,
    ZoomConfig

internal abstract class CameraConfigImpl<T>(
    var cameraInfo: CameraInfo,
    var videoConfig: BaseVideoConfig<T>,
    var featureConfig: BaseFeatureConfig<T>,
    var sizeConfig: BaseSizeConfig<T>,
    var zoomConfig: BaseZoomConfig<T>
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