package co.infinum.goldeneye.config.camera2

import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import androidx.annotation.RequiresApi
import co.infinum.goldeneye.config.BaseAdvancedFeatureConfig
import co.infinum.goldeneye.models.AntibandingMode
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.ColorEffectMode
import co.infinum.goldeneye.models.WhiteBalanceMode

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class AdvancedFeatureConfigImpl(
    advancedFeaturesEnabled: Boolean,
    onUpdateCallback: (CameraProperty) -> Unit
) : BaseAdvancedFeatureConfig<CameraCharacteristics>(advancedFeaturesEnabled, onUpdateCallback) {

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