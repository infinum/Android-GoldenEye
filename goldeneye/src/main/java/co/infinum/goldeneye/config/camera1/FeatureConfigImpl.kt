@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.config.camera1

import android.hardware.Camera
import co.infinum.goldeneye.config.BaseFeatureConfig
import co.infinum.goldeneye.models.*

internal class FeatureConfigImpl(
    onUpdateCallback: (CameraProperty) -> Unit
) : BaseFeatureConfig<Camera.Parameters>(onUpdateCallback) {

    override val isTapToFocusSupported: Boolean
        get() = characteristics?.maxNumFocusAreas ?: 0 > 0

    override val supportedFlashModes: List<FlashMode>
        get() = characteristics?.supportedFlashModes
            ?.map { FlashMode.fromCamera1(it) }
            ?.distinct()
            ?.filter { it != FlashMode.UNKNOWN }
            ?: emptyList()

    override val supportedFocusModes: List<FocusMode>
        get() = characteristics?.supportedFocusModes
            ?.map { FocusMode.fromCamera1(it) }
            ?.distinct()
            ?.filter { it != FocusMode.UNKNOWN }
            ?: emptyList()

    override val supportedWhiteBalanceModes: List<WhiteBalanceMode>
        get() = characteristics?.supportedWhiteBalance
            ?.map { WhiteBalanceMode.fromCamera1(it) }
            ?.distinct()
            ?.filter { it != WhiteBalanceMode.UNKNOWN }
            ?: emptyList()

    override val supportedColorEffectModes: List<ColorEffectMode>
        get() = characteristics?.supportedColorEffects
            ?.map { ColorEffectMode.fromCamera1(it) }
            ?.distinct()
            ?.filter { it != ColorEffectMode.UNKNOWN }
            ?: emptyList()

    override val supportedAntibandingModes: List<AntibandingMode>
        get() = characteristics?.supportedAntibanding
            ?.map { AntibandingMode.fromCamera1(it) }
            ?.distinct()
            ?.filter { it != AntibandingMode.UNKNOWN }
            ?: emptyList()
}