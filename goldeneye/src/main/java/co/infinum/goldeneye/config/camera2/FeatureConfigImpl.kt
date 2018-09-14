package co.infinum.goldeneye.config.camera2

import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.config.BaseFeatureConfig
import co.infinum.goldeneye.config.FeatureConfig
import co.infinum.goldeneye.models.*
import co.infinum.goldeneye.utils.LogDelegate

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class FeatureConfigImpl(
    val onUpdateListener: (CameraProperty) -> Unit
) : BaseFeatureConfig<CameraCharacteristics>(onUpdateListener) {

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

    override val supportedWhiteBalance: List<WhiteBalance>
        get() = characteristics?.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES)
            ?.map { WhiteBalance.fromCamera2(it) }
            ?.filter { it != WhiteBalance.UNKNOWN }
            ?: emptyList()

    override val supportedSceneModes: List<SceneMode>
        get() = characteristics?.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES)
            ?.map { SceneMode.fromCamera2(it) }
            ?.filter { it != SceneMode.UNKNOWN }
            ?: emptyList()

    override val supportedColorEffects: List<ColorEffect>
        get() = characteristics?.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS)
            ?.map { ColorEffect.fromCamera2(it) }
            ?.filter { it != ColorEffect.UNKNOWN }
            ?: emptyList()

    override val supportedAntibanding: List<Antibanding>
        get() = characteristics?.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES)
            ?.map { Antibanding.fromCamera2(it) }
            ?.filter { it != Antibanding.UNKNOWN }
            ?: emptyList()
}