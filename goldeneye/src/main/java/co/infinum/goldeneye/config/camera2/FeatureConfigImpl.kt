package co.infinum.goldeneye.config.camera2

import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.config.BaseFeatureConfig
import co.infinum.goldeneye.models.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class FeatureConfigImpl(
    onUpdateCallback: (CameraProperty) -> Unit
) : BaseFeatureConfig<CameraCharacteristics>(onUpdateCallback) {

    override val isTapToFocusSupported: Boolean by lazy {
        characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) ?: 0 > 0
    }

    override val supportedFlashModes: List<FlashMode> by lazy {
        if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == false) {
            return@lazy emptyList<FlashMode>()
        }

        val flashModes = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES)
            ?.map { FlashMode.fromCamera2(it) }
            ?.filter { it != FlashMode.UNKNOWN }
            ?.toMutableList()
        flashModes?.add(FlashMode.TORCH)
        flashModes ?: emptyList<FlashMode>()
    }

    override val supportedFocusModes: List<FocusMode> by lazy {
        characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
            ?.map { FocusMode.fromCamera2(it) }
            ?.filter { it != FocusMode.UNKNOWN }
            ?: emptyList()
    }

    override val supportedWhiteBalanceModes: List<WhiteBalanceMode> by lazy {
        characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES)
            ?.map { WhiteBalanceMode.fromCamera2(it) }
            ?.filter { it != WhiteBalanceMode.UNKNOWN }
            ?: emptyList()
    }

    override val supportedColorEffectModes: List<ColorEffectMode> by lazy {
        characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS)
            ?.map { ColorEffectMode.fromCamera2(it) }
            ?.filter { it != ColorEffectMode.UNKNOWN }
            ?: emptyList()
    }

    override val supportedAntibandingModes: List<AntibandingMode> by lazy {
        characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES)
            ?.map { AntibandingMode.fromCamera2(it) }
            ?.filter { it != AntibandingMode.UNKNOWN }
            ?: emptyList()
    }
}