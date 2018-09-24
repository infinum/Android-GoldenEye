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

    override val isTapToFocusSupported: Boolean
        get() = characteristics?.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) ?: 0 > 0

    override val supportedFlashModes: List<FlashMode>
        get() {
            if (characteristics?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == false) {
                return emptyList()
            }

            val flashModes = characteristics?.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES)
                ?.map { FlashMode.fromCamera2(it) }
                ?.filter { it != FlashMode.UNKNOWN }
                ?.toMutableList()
            flashModes?.add(FlashMode.TORCH)
            return flashModes ?: emptyList()
        }

    override val supportedFocusModes: List<FocusMode>
        get() = characteristics?.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
            ?.map { FocusMode.fromCamera2(it) }
            ?.filter { it != FocusMode.UNKNOWN }
            ?: emptyList()

    override val supportedWhiteBalanceModes: List<WhiteBalanceMode>
        get() = characteristics?.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES)
            ?.map { WhiteBalanceMode.fromCamera2(it) }
            ?.filter { it != WhiteBalanceMode.UNKNOWN }
            ?: emptyList()

    override val supportedColorEffectModes: List<ColorEffectMode>
        get() = characteristics?.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS)
            ?.map { ColorEffectMode.fromCamera2(it) }
            ?.filter { it != ColorEffectMode.UNKNOWN }
            ?: emptyList()

    override val supportedAntibandingModes: List<AntibandingMode>
        get() = characteristics?.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES)
            ?.map { AntibandingMode.fromCamera2(it) }
            ?.filter { it != AntibandingMode.UNKNOWN }
            ?: emptyList()
}