@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.config.camera1

import android.hardware.Camera
import co.infinum.goldeneye.config.BaseFeatureConfig
import co.infinum.goldeneye.models.*

internal class FeatureConfigImpl(
    onUpdateListener: (CameraProperty) -> Unit
) : BaseFeatureConfig<Camera.Parameters>(onUpdateListener) {

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

    override val supportedWhiteBalance: List<WhiteBalance>
        get() = characteristics?.supportedWhiteBalance
            ?.map { WhiteBalance.fromCamera1(it) }
            ?.distinct()
            ?.filter { it != WhiteBalance.UNKNOWN }
            ?: emptyList()

    override val supportedSceneModes: List<SceneMode>
        get() = characteristics?.supportedSceneModes
            ?.map { SceneMode.fromCamera1(it) }
            ?.distinct()
            ?.filter { it != SceneMode.UNKNOWN }
            ?: emptyList()

    override val supportedColorEffects: List<ColorEffect>
        get() = characteristics?.supportedColorEffects
            ?.map { ColorEffect.fromCamera1(it) }
            ?.distinct()
            ?.filter { it != ColorEffect.UNKNOWN }
            ?: emptyList()

    override val supportedAntibanding: List<Antibanding>
        get() = characteristics?.supportedAntibanding
            ?.map { Antibanding.fromCamera1(it) }
            ?.distinct()
            ?.filter { it != Antibanding.UNKNOWN }
            ?: emptyList()
}