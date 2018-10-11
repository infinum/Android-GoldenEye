@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.config.camera1

import android.hardware.Camera
import co.infinum.goldeneye.config.BaseAdvancedFeatureConfig
import co.infinum.goldeneye.models.AntibandingMode
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.ColorEffectMode
import co.infinum.goldeneye.models.WhiteBalanceMode

internal class AdvancedFeatureConfigImpl(
    advancedFeaturesEnabled: Boolean,
    onUpdateCallback: (CameraProperty) -> Unit
) : BaseAdvancedFeatureConfig<Camera.Parameters>(advancedFeaturesEnabled, onUpdateCallback) {

    override val supportedWhiteBalanceModes: List<WhiteBalanceMode> by lazy {
        characteristics.supportedWhiteBalance
            ?.map { WhiteBalanceMode.fromCamera1(it) }
            ?.filter { it != WhiteBalanceMode.UNKNOWN }
            ?: emptyList()

    }

    override val supportedColorEffectModes: List<ColorEffectMode> by lazy {
        characteristics.supportedColorEffects
            ?.map { ColorEffectMode.fromCamera1(it) }
            ?.filter { it != ColorEffectMode.UNKNOWN }
            ?: emptyList()

    }

    override val supportedAntibandingModes: List<AntibandingMode> by lazy {
        characteristics.supportedAntibanding
            ?.map { AntibandingMode.fromCamera1(it) }
            ?.filter { it != AntibandingMode.UNKNOWN }
            ?: emptyList()
    }
}