package co.infinum.goldeneye.extensions

import android.hardware.camera2.CaptureRequest
import android.os.Build
import androidx.annotation.RequiresApi
import co.infinum.goldeneye.config.camera2.Camera2ConfigImpl
import co.infinum.goldeneye.models.FlashMode
import co.infinum.goldeneye.utils.CameraUtils

/**
 * Copy given request builder parameters to [this] request builder.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal fun CaptureRequest.Builder?.copyParamsFrom(other: CaptureRequest.Builder?) {
    if (other == null || this == null) {
        return
    }
    set(CaptureRequest.CONTROL_AF_MODE, other[CaptureRequest.CONTROL_AF_MODE])
    set(CaptureRequest.CONTROL_EFFECT_MODE, other[CaptureRequest.CONTROL_EFFECT_MODE])
    set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, other[CaptureRequest.CONTROL_AE_ANTIBANDING_MODE])
    set(CaptureRequest.CONTROL_AWB_MODE, other[CaptureRequest.CONTROL_AWB_MODE])
    set(CaptureRequest.SCALER_CROP_REGION, other[CaptureRequest.SCALER_CROP_REGION])
    set(CaptureRequest.CONTROL_AE_MODE, other[CaptureRequest.CONTROL_AE_MODE])
    set(CaptureRequest.FLASH_MODE, other[CaptureRequest.FLASH_MODE])
}

/**
 * Create new capture request builder from given config.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal fun CaptureRequest.Builder?.applyConfig(config: Camera2ConfigImpl?) {
    if (this == null || config == null) {
        return
    }

    with(config) {
        if (supportedFocusModes.contains(focusMode)) {
            set(CaptureRequest.CONTROL_AF_MODE, focusMode.toCamera2())
        }

        if (supportedColorEffectModes.contains(colorEffectMode)) {
            set(CaptureRequest.CONTROL_EFFECT_MODE, colorEffectMode.toCamera2())
        }

        if (supportedAntibandingModes.contains(antibandingMode)) {
            set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, antibandingMode.toCamera2())
        }

        if (supportedWhiteBalanceModes.contains(whiteBalanceMode)) {
            set(CaptureRequest.CONTROL_AWB_MODE, whiteBalanceMode.toCamera2())
        }

        if (config.zoom > 100) {
            val zoomRect = CameraUtils.calculateCamera2ZoomRect(this)
            if (zoomRect != null) {
                set(CaptureRequest.SCALER_CROP_REGION, zoomRect)
            }
        } else {
            set(CaptureRequest.SCALER_CROP_REGION, null)
        }

        if (videoStabilizationEnabled) {
            set(
                CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                if (videoStabilizationEnabled) {
                    CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON
                } else {
                    CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF
                }
            )
        }

        /* Awesome Camera2 API workaround to support FlashMode.TORCH */
        if (supportedFlashModes.contains(flashMode)) {
            if (flashMode == FlashMode.TORCH) {
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                set(CaptureRequest.FLASH_MODE, FlashMode.TORCH.toCamera2())
            } else {
                set(CaptureRequest.CONTROL_AE_MODE, flashMode.toCamera2())
                set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
            }
        }
    }
}