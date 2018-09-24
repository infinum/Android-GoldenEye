package co.infinum.goldeneye.config.camera2

import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.config.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class Camera2ConfigImpl(
    cameraInfo: CameraInfo,
    videoConfig: BaseVideoConfig<CameraCharacteristics>,
    featureConfig: BaseFeatureConfig<CameraCharacteristics>,
    sizeConfig: BaseSizeConfig<CameraCharacteristics>,
    zoomConfig: BaseZoomConfig<CameraCharacteristics>
) : CameraConfigImpl<CameraCharacteristics>(cameraInfo, videoConfig, featureConfig, sizeConfig, zoomConfig)