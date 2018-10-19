@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.config.camera1

import android.hardware.Camera
import co.infinum.goldeneye.config.*

internal class Camera1ConfigImpl(
    cameraInfo: CameraInfo,
    videoConfig: BaseVideoConfig<Camera.Parameters>,
    basicFeatureConfig: BaseBasicFeatureConfig<Camera.Parameters>,
    sizeConfig: BaseSizeConfig<Camera.Parameters>,
    zoomConfig: BaseZoomConfig<Camera.Parameters>,
    advancedFeatureConfig: BaseAdvancedFeatureConfig<Camera.Parameters>
) : CameraConfigImpl<Camera.Parameters>(cameraInfo, videoConfig, basicFeatureConfig, advancedFeatureConfig, sizeConfig, zoomConfig)