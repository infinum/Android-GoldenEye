package co.infinum.goldeneye.config.camera2

import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import androidx.annotation.RequiresApi
import co.infinum.goldeneye.config.BaseBasicFeatureConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.FlashMode
import co.infinum.goldeneye.models.FocusMode

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class BasicFeatureConfig(
    onUpdateCallback: (CameraProperty) -> Unit
) : BaseBasicFeatureConfig<CameraCharacteristics>(onUpdateCallback) {

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
}