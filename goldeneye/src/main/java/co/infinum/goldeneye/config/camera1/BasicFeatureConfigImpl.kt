@file:Suppress("DEPRECATION", "ConvertCallChainIntoSequence")

package co.infinum.goldeneye.config.camera1

import android.hardware.Camera
import co.infinum.goldeneye.config.BaseBasicFeatureConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.FlashMode
import co.infinum.goldeneye.models.FocusMode

internal class BasicFeatureConfigImpl(
    onUpdateCallback: (CameraProperty) -> Unit
) : BaseBasicFeatureConfig<Camera.Parameters>(onUpdateCallback) {

    override val isTapToFocusSupported: Boolean by lazy {
        characteristics.maxNumFocusAreas > 0
    }

    override val supportedFlashModes: List<FlashMode> by lazy {
        characteristics.supportedFlashModes
            ?.map { FlashMode.fromCamera1(it) }
            ?.filter { it != FlashMode.UNKNOWN }
            ?: emptyList()
    }

    override val supportedFocusModes: List<FocusMode> by lazy {
        characteristics.supportedFocusModes
            ?.map { FocusMode.fromCamera1(it) }
            ?.filter { it != FocusMode.UNKNOWN }
            ?: emptyList()
    }
}