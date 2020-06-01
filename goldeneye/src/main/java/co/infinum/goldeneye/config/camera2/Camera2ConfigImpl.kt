package co.infinum.goldeneye.config.camera2

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraCharacteristics.*
import android.os.Build
import androidx.annotation.RequiresApi
import co.infinum.goldeneye.config.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class Camera2ConfigImpl(
    cameraInfo: CameraInfo,
    videoConfig: BaseVideoConfig<CameraCharacteristics>,
    basicFeatureConfig: BaseBasicFeatureConfig<CameraCharacteristics>,
    advancedFeatureConfig: BaseAdvancedFeatureConfig<CameraCharacteristics>,
    sizeConfig: BaseSizeConfig<CameraCharacteristics>,
    zoomConfig: BaseZoomConfig<CameraCharacteristics>
) : CameraConfigImpl<CameraCharacteristics>(cameraInfo, videoConfig, basicFeatureConfig, advancedFeatureConfig, sizeConfig, zoomConfig) {

    fun isHardwareAtLeastLimited() =
        characteristics?.get(INFO_SUPPORTED_HARDWARE_LEVEL) == INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
            || isHardwareAtLeastFull()
            || isHardwareAtLeastLevel3()

    private fun isHardwareAtLeastFull() =
        characteristics?.get(INFO_SUPPORTED_HARDWARE_LEVEL) == INFO_SUPPORTED_HARDWARE_LEVEL_FULL
            || isHardwareAtLeastLevel3()

    private fun isHardwareAtLeastLevel3() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            && characteristics?.get(INFO_SUPPORTED_HARDWARE_LEVEL) == INFO_SUPPORTED_HARDWARE_LEVEL_3
}